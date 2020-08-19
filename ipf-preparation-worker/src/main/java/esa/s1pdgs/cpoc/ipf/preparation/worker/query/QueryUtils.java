package esa.s1pdgs.cpoc.ipf.preparation.worker.query;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.springframework.util.StringUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableTask;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableInputMode;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableMandatoryEnum;

/**
 * Utility class containing methods around querying of products in the
 * mainInputSearches and the auxSearch to reduce duplicating of code
 * 
 * @author Julian Kaping
 *
 */
public class QueryUtils {

	/**
	 * Creates a list of AppDataJobTaskInputs used in AppDataJobs. The list is based
	 * on the specific tasktable accessible through the TaskTableAdapter.
	 * 
	 * @param mode      ProductMode, used to filter not used inputs of the TaskTable
	 * @param ttAdapter Adapter to access the contents of the TaskTable
	 * @return
	 */
	public static List<AppDataJobTaskInputs> buildInitialInputs(ProductMode mode, TaskTableAdapter ttAdapter) {
		return taskTableTasksAndInputsMappedTo(
				(reference, input) -> new AppDataJobInput(reference, "", "",
						TaskTableMandatoryEnum.YES.equals(input.getMandatory()), emptyList()),
				(jobInputList, task) -> new AppDataJobTaskInputs(task.getName(), task.getVersion(), jobInputList), mode,
				ttAdapter);
	}

	/**
	 * Creates a list (length = number of tasks in tasktable) of type U using the
	 * given functions.
	 * 
	 * @param <T>              result type of inputMapFunction, type of entries for
	 *                         the list used as first parameter in taskMapFunction
	 * @param <U>              result type of taskMapFunction, type of entries of
	 *                         the result map
	 * @param inputMapFunction creates entries for list used as first parameter of
	 *                         taskMapFunction (size = number of inputs for task in
	 *                         tasktable)
	 * @param taskMapFunction  creates entries for result list
	 * @param mode             specifies acceptable mode of tasktable inputs
	 *                         {@link #modeOfInputOrReference}
	 * @param ttAdapter        adapter to access contents of tasktable
	 * @return list (length = number of tasks in tasktable) with entries based on
	 *         taskMapFunction
	 */
	public static <T, U> List<U> taskTableTasksAndInputsMappedTo(
			final BiFunction<String, TaskTableInput, T> inputMapFunction,
			final BiFunction<List<T>, TaskTableTask, U> taskMapFunction, final ProductMode mode,
			final TaskTableAdapter ttAdapter) {
		final List<U> mappedTasks = new ArrayList<>();

		final Map<String, TaskTableInput> inputsWithId = ttAdapter.getTasks().values().stream()
				.flatMap(task -> task.getInputs().stream()).filter(input -> !StringUtils.isEmpty(input.getId()))
				.collect(toMap(TaskTableInput::getId, i -> i));

		for (final Map.Entry<String, TaskTableTask> taskEntry : ttAdapter.getTasks().entrySet()) {
			final List<T> mappedInputs = new ArrayList<>();
			int inputNumber = 0;
			for (final TaskTableInput input : taskEntry.getValue().getInputs()) {
				if (mode.isCompatibleWithTaskTableMode(modeOfInputOrReference(input, inputsWithId))) {
					final String reference = String.format("%sI%s", taskEntry.getKey(), inputNumber);
					mappedInputs.add(inputMapFunction.apply(reference, input));
				}
				inputNumber++;
			}
			mappedTasks.add(taskMapFunction.apply(mappedInputs, taskEntry.getValue()));
		}

		return mappedTasks;
	}

	/**
	 * Returns the TaskTableInputMode of the TaskTableInput. If the input contains a
	 * reference to a different input, the mode of the reference is returned
	 * 
	 * @param input        TaskTableInput which mode should be determined
	 * @param inputsWithId Map of all inputs inside the tasktable
	 * @return TaskTableInputMode of the input
	 */
	public static TaskTableInputMode modeOfInputOrReference(TaskTableInput input,
			Map<String, TaskTableInput> inputsWithId) {
		if (StringUtils.isEmpty(input.getReference())) {
			return input.getMode();
		}

		final TaskTableInput reference = inputsWithId.get(input.getReference());

		if (reference == null) {
			throw new RuntimeException("no input in taskTable with id " + input.getReference());
		}

		return reference.getMode();
	}

}
