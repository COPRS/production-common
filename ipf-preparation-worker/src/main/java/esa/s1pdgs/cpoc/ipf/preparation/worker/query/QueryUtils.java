package esa.s1pdgs.cpoc.ipf.preparation.worker.query;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobPreselectedInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableTask;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableInputMode;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableInputOrigin;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableMandatoryEnum;

/**
 * Utility class containing methods around querying of products in the
 * mainInputSearches and the auxSearch to reduce duplicating of code
 * 
 * @author Julian Kaping
 *
 */
public class QueryUtils {

	private static final Logger LOG = LoggerFactory.getLogger(QueryUtils.class);

	/**
	 * Creates a list of alternatives for the given list of inputs
	 * 
	 * @param inputs           list of inputs for which the alternatives should be
	 *                         extracted
	 * @param taskTableAdapter adapter to tasktable
	 * @param mode             mode of the alternatives that should be filtered for
	 * @return list of alternatives
	 */
	public static List<TaskTableInputAlternative> alternativesOf(final List<AppDataJobInput> inputs,
			final TaskTableAdapter taskTableAdapter, final ProductMode mode) {

		final List<String> inputReferences = inputs.stream().map(AppDataJobInput::getTaskTableInputReference)
				.collect(toList());

		final Map<String, List<TaskTableInputAlternative>> taskTableAlternativesMappedToReferences = inputsMappedTo(
				(reference, input) -> singletonMap(reference, input.getAlternatives()), taskTableAdapter).stream()
						.flatMap(map -> map.entrySet().stream()).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		return taskTableAlternativesMappedToReferences.entrySet().stream()
				.filter(entry -> inputReferences.contains(entry.getKey())).flatMap(entry -> entry.getValue().stream())
				.filter(alt -> alt.getOrigin() == TaskTableInputOrigin.DB).distinct().collect(toList());
	}

	/**
	 * Creates a list of AppDataJobTaskInputs used in AppDataJobs. The list is based
	 * on the specific tasktable accessible through the TaskTableAdapter.
	 * 
	 * @param ttAdapter Adapter to access the contents of the TaskTable
	 * @return list of all inputs generated from the task table
	 */
	public static List<AppDataJobTaskInputs> buildInitialInputs(final TaskTableAdapter ttAdapter) {
		return taskTableTasksAndInputsMappedTo(
				(jobInputList, task) -> new AppDataJobTaskInputs(task.getName(), task.getVersion(), jobInputList),
				(reference, input) -> new AppDataJobInput(reference, "", "",
						TaskTableMandatoryEnum.YES.equals(input.getMandatory()), emptyList()),
				ttAdapter);
	}

	/**
	 *
	 * @param originalInputs originalInputs inputs with empty results yet
	 * @param preselectedInputs preselected inputs coming from main input search
	 * @return complete of inputs (with and without preselected result)
	 */
	public static List<AppDataJobTaskInputs> includingPreselected(final List<AppDataJobTaskInputs> originalInputs, final List<AppDataJobPreselectedInput> preselectedInputs) {
		if(preselectedInputs == null) {
			return originalInputs;
		}

		final Map<String, AppDataJobPreselectedInput> mappedByTaskInputReference
				= preselectedInputs.stream().collect(toMap(AppDataJobPreselectedInput::getTaskTableInputReference, input -> input));

		return originalInputs.stream().map(taskInputs -> maybeReplace(taskInputs, mappedByTaskInputReference)).collect(toList());
	}

	private static AppDataJobTaskInputs maybeReplace(final AppDataJobTaskInputs original, final Map<String, AppDataJobPreselectedInput> preselectedInputs) {
		final List<AppDataJobInput> inputsIncludingPreselectedInputs = original.getInputs().stream().map(input -> maybeReplace(input, preselectedInputs)).collect(toList());

		return new AppDataJobTaskInputs(original.getTaskName(), original.getTaskVersion(), inputsIncludingPreselectedInputs);
	}

	private static AppDataJobInput maybeReplace(final AppDataJobInput input, final Map<String, AppDataJobPreselectedInput> preselectedInputs) {
		if (preselectedInputs.containsKey(input.getTaskTableInputReference())) {

			final AppDataJobPreselectedInput preselectedInput = preselectedInputs.get(input.getTaskTableInputReference());
			final String preselectedFiles = preselectedInput.getFiles().stream()
					.map(AppDataJobFile::getFilename)
					.collect(joining(", "));

			LOG.debug("replacing input {} {} with preselected results {}", input.getTaskTableInputReference(), input.getFileType(), preselectedFiles);

			return new AppDataJobInput(
					input.getTaskTableInputReference(),
					preselectedInput.getFileType(),
					preselectedInput.getFileNameType(),
					input.isMandatory(),
					preselectedInput.getFiles());
		}

		return input;
	}

	/**
	 * Creates a list of type T of length number of inputs in taskTable. The
	 * inputMapFunction is used to create the entries of the list
	 * 
	 * @param <T>              result type of inputMapFunction, type of entries for
	 *                         the list used as first parameter in taskMapFunction
	 * @param inputMapFunction creates entries for list
	 * @param taskTableAdapter adapter to access contents of tasktable
	 * @return list (length = number of inputs in tasks of tasktable) with entries
	 *         based on inputMapFunction
	 */
	public static <T> List<T> inputsMappedTo(final BiFunction<String, TaskTableInput, T> inputMapFunction,
			final TaskTableAdapter taskTableAdapter) {
		return taskTableTasksAndInputsMappedTo(
				(list, task) -> list,
				inputMapFunction,
				taskTableAdapter).stream()
				.flatMap(Collection::stream).collect(toList());
	}

	/**
	 * Returns the TaskTableInputMode of the TaskTableInput. If the input contains a
	 * reference to a different input, the mode of the reference is returned
	 * 
	 * @param input        TaskTableInput which mode should be determined
	 * @param inputsWithId Map of all inputs inside the tasktable
	 * @return TaskTableInputMode of the input
	 */
	public static TaskTableInputMode modeOfInputOrReference(final TaskTableInput input,
			final Map<String, TaskTableInput> inputsWithId) {
		if (StringUtils.isEmpty(input.getReference())) {
			return input.getMode();
		}

		final TaskTableInput reference = inputsWithId.get(input.getReference());

		if (reference == null) {
			throw new RuntimeException("no input in taskTable with id " + input.getReference());
		}

		return reference.getMode();
	}

	public static Optional<TaskAndInput> getTaskForReference(final String inputReference, final TaskTableAdapter adapter) {

		final BiFunction<List<Optional<TaskTableInput>>, TaskTableTask, Optional<TaskAndInput>> toTask = (list, task) -> {
			if (list.stream().anyMatch(Optional::isPresent)) {
				return Optional.of(new TaskAndInput(task, list.stream().filter(Optional::isPresent).findAny().get().get()));
			}

			return Optional.empty();
		};

		final BiFunction<String, TaskTableInput, Optional<TaskTableInput>> toDo = (reference, input) -> {
			if (reference.equals(inputReference)) {
				return Optional.of(input);
			}

			return Optional.empty();
		};

		final List<Optional<TaskAndInput>> optionals = taskTableTasksAndInputsMappedTo(toTask, toDo, adapter);
		return optionals.stream().filter(Optional::isPresent).map(Optional::get).findAny();
	}

	// dirty workaround class until this all get refactored
	public static final class TaskAndInput {
		private final TaskTableTask task;
		private final TaskTableInput input;

		public TaskAndInput(final TaskTableTask task, final TaskTableInput input) {
			this.task = task;
			this.input = input;
		}
		
		public final String getName() {
			return task.getName();
		}
		
		public final String getVersion() {
			return task.getVersion();
		}
		
		public final TaskTableInput getInput() {
			return input;
		}
	}

	/**
	 * Creates a list (length = number of tasks in taskTable) of type MAPPED_TASK using the
	 * given functions.
	 *
	 * Traverses through all tasks and inputs of a taskTable. For each taskTable input the inputMapFunction
	 * is applied resulting in a MAPPED_INPUT. Then a List<MAPPED_INPUT> is applied to the taskMapFunction resulting in
	 * a MAPPED_TASK. After all a List<MAPPED_TASK> is returned.
	 *
	 * While traversing the Pools / Task / Input number is increased to create a reference String to identify an input
	 * by it`s position in the taskTable. This reference is passed to the inputMapFunction
	 * 
	 * @param <MAPPED_INPUT>   result type of inputMapFunction, type of entries for
	 *                         the list used as first parameter in taskMapFunction
	 * @param <MAPPED_TASK>    result type of taskMapFunction, type of entries of
	 *                         the result map
	 * @param taskMapFunction  creates entries for result list
	 * @param inputMapFunction creates entries for list used as first parameter of
	 *                         taskMapFunction (size = number of inputs for task in
	 *                         tasktable)
	 * @param ttAdapter        adapter to access contents of tasktable
	 * @return list (length = number of tasks in tasktable) with entries based on
	 *         taskMapFunction
	 */
	public static <MAPPED_INPUT, MAPPED_TASK> List<MAPPED_TASK> taskTableTasksAndInputsMappedTo(
			final BiFunction<List<MAPPED_INPUT>, TaskTableTask, MAPPED_TASK> taskMapFunction,
			final BiFunction<String, TaskTableInput, MAPPED_INPUT> inputMapFunction,
			final TaskTableAdapter ttAdapter) {

		final Map<String, TaskTableInput> inputsWithId = collectInputsWithId(ttAdapter);

		final List<MAPPED_TASK> mappedTasks = new ArrayList<>();

		for (final Map.Entry<String, TaskTableTask> taskEntry : ttAdapter.getTasks().entrySet()) {

			final String taskReference = taskEntry.getKey();
			final List<TaskTableInput> taskInputs = taskEntry.getValue().getInputs();

			final List<MAPPED_INPUT> mappedInputs = taskInputsMappedTo(
					taskReference,
					taskInputs,
					ttAdapter.mode(),
					inputMapFunction,
					inputsWithId);

			mappedTasks.add(taskMapFunction.apply(mappedInputs, taskEntry.getValue()));
		}

		return mappedTasks;
	}

	private static Map<String, TaskTableInput> collectInputsWithId(final TaskTableAdapter ttAdapter) {
		return ttAdapter.getTasks().values().stream()
				.flatMap(task -> task.getInputs().stream()).filter(input -> !StringUtils.isEmpty(input.getId()))
				.collect(toMap(TaskTableInput::getId, i -> i));
	}

	private static <MAPPED_INPUT> List<MAPPED_INPUT> taskInputsMappedTo(
			final String taskReference,
			final List<TaskTableInput> inputs,
			final ProductMode mode,
			final BiFunction<String, TaskTableInput, MAPPED_INPUT> inputMapFunction,
			final Map<String, TaskTableInput> inputsWithId) {

		final List<MAPPED_INPUT> mappedInputs = new ArrayList<>();
		int inputNumber = 0;
		for (final TaskTableInput input : inputs) {
			if (mode.isCompatibleWithTaskTableMode(modeOfInputOrReference(input, inputsWithId))) {
				final String reference = String.format("%sI%s", taskReference, inputNumber);
				mappedInputs.add(inputMapFunction.apply(reference, input));
			}
			inputNumber++;
		}
		return mappedInputs;
	}
}
