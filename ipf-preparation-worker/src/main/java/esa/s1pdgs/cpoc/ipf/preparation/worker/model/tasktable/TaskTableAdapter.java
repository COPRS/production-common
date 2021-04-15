package esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.converter.TaskTableToJobOrderConverter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.metadata.SearchMetadataResult;
import esa.s1pdgs.cpoc.ipf.preparation.worker.query.QueryUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.query.QueryUtils.TaskAndInput;
import esa.s1pdgs.cpoc.metadata.client.SearchMetadataQuery;
import esa.s1pdgs.cpoc.metadata.model.AbstractMetadata;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInputFile;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderTimeInterval;
import esa.s1pdgs.cpoc.xml.model.joborder.enums.JobOrderFileNameType;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableDynProcParam;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTablePool;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableTask;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableInputOrigin;

public class TaskTableAdapter {
	private final File file;
	private final TaskTable taskTable;
	private final ElementMapper elementMapper;
	private final ProductMode productMode;
	
	public TaskTableAdapter(final File file, final TaskTable taskTable, final ElementMapper elementMapper, final ProductMode productMode) {
		this.file = file;
		this.taskTable = taskTable;
		this.elementMapper = elementMapper;
		this.productMode = productMode;
	}
	
	public final File file() {
		return file;
	}
	
	public final TaskTable taskTable() {
		return taskTable;
	}
	
	public final ProductMode mode() {
		return productMode;
	}

	public Iterable<TaskTablePool> pools() {
		return taskTable.getPools();
	}
	
	public final String getTypeForParameterName(final String name) {
		for (final TaskTableDynProcParam param : taskTable.getDynProcParams()) {
			if (param.getName().equals(name)) {
				return param.getType();
			}
		}
		return "String";
	}
	
	public final List<List<String>> buildTasks() {
		final List<List<String>> tasks = new ArrayList<>();
		
		taskTable.getPools().forEach(pool -> tasks.add(pool.tasks()
				.map(TaskTableTask::getFileName)
				.collect(toList())));
		return tasks;
	}
	
	/**
	 * Creates a map of all tasks inside of the tasktable
	 * @return
	 */
	public Map<String, TaskTableTask> getTasks() {
		final Map<String, TaskTableTask> tasks = new HashMap<>();
		int poolNumber = 0;
		for (final TaskTablePool pool : pools()) {
			int taskNumber = 0;
			for (final TaskTableTask task : pool.getTasks()) {
				final String reference = String.format("P%sT%s:%s-%s",
						poolNumber, taskNumber, task.getName(), task.getVersion());
				tasks.put(reference, task);

				taskNumber++;
			}
			poolNumber++;
		}
		return tasks;
	}
	
	public final Map<String, TaskTableInput> taskTableInputs() {
		return QueryUtils.inputsMappedTo(Collections::singletonMap, this).stream()
				.flatMap(map -> map.entrySet().stream())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
	
	@SafeVarargs
	public final Optional<TaskTableInputAdapter> firstInputContainingOneOf(final String ... type) {
		final List<String> types = CollectionUtil.toList(type);
		for (final AppDataJobTaskInputs taskInputs : QueryUtils.buildInitialInputs(this)) {
			for (final AppDataJobInput input : taskInputs.getInputs()) {				
				
				final Optional<TaskAndInput> optionalTask = QueryUtils
						.getTaskForReference(input.getTaskTableInputReference(), this);
				
				if (!optionalTask.isPresent()) {
					continue;
				}
				final TaskAndInput ti = optionalTask.get();
				final TaskTableInput ttInput = ti.getInput();
							
				for (final TaskTableInputAlternative alternative : ttInput.getAlternatives()) {
					if (types.contains(alternative.getFileType())) {
						return Optional.of(new TaskTableInputAdapter(input.getTaskTableInputReference(), ttInput));
					}
				}	
			}
		}
		return Optional.empty();
	}
	
	public final JobOrder newJobOrder(final ProcessSettings settings, final ProductMode mode) {
		// Build from task table
		final TaskTableToJobOrderConverter converter = new TaskTableToJobOrderConverter(mode);
		final JobOrder jobOrderTemplate = converter.apply(taskTable);

		// Update values from configuration file
		jobOrderTemplate.getConf().getProcParams().forEach(item -> elementMapper.getParameterValue(item.getName())
			.ifPresent(item::setValue));
		jobOrderTemplate.getConf().setStdoutLogLevel(settings.getLoglevelstdout());
		jobOrderTemplate.getConf().setStderrLogLevel(settings.getLoglevelstderr());
		jobOrderTemplate.getConf().setProcessingStation(settings.getProcessingstation());

		// Update outputs from configuration file
		jobOrderTemplate.getProcs().stream()
			.filter(proc -> !proc.getOutputs().isEmpty())
			.flatMap(proc -> proc.getOutputs().stream())
			.filter(output -> output.getFileNameType() == JobOrderFileNameType.REGEXP)
			.forEach(output -> output.setFileName(elementMapper.getRegexFor(output.getFileType())));

		// Update the output family according configuration file
		jobOrderTemplate.getProcs().stream()
			.filter(proc -> !proc.getOutputs().isEmpty())
			.flatMap(proc -> proc.getOutputs().stream())
			.forEach(output -> output.setFamily(elementMapper.outputFamilyOf(output.getFileType())));
		
		return new JobOrder(jobOrderTemplate, settings.getLevel());
	}

	// FIXME this method does two things: 1. find input 2. convert to JobOrderInput -> job order conversion should be done
	// as last step when all inputs are present (in AppDataJob)
	// this would also remove the mutable behaviour of the jobOrder
	// TODO at least return AppDataJobInput here
	// TODO consider moving this method to AuxQuery
	public final JobOrderInput findInput(final AppDataJob job, final TaskTableInput input, final Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataResult> results) {
		JobOrderInput result = null;

		for (final TaskTableInputAlternative alt : input.alternativesOrdered().collect(toList())) {
			// We ignore input not DB
			if (alt.getOrigin() == TaskTableInputOrigin.DB) {				
				// dirty workaround to prevent NPE here if there had been problems with the query				
				final SearchMetadataResult queryRes = results.get(alt.getTaskTableInputAltKey());				
				final List<SearchMetadata> queryResults;
				if (queryRes == null) {
					queryResults = Collections.emptyList();
				} 
				else {
					queryResults = queryRes.getResult();
				}
				// has queries defined?
				if (!CollectionUtils.isEmpty(queryResults)) {
					final JobOrderFileNameType type = getFileNameTypeFor(alt);
					// Retrieve family										
					final ProductFamily family = elementMapper.inputFamilyOf(alt.getFileType());
					
					final List<JobOrderInputFile> jobOrderInputFiles = queryResults.stream()
							.map(file -> new JobOrderInputFile(file.getProductName(), file.getKeyObjectStorage()))
							.collect(toList());

					final List<JobOrderTimeInterval> jobOrderTimeIntervals = queryResults.stream()
						.map(this::newJobOrderTimeIntervalFor)
						.collect(toList());
					
					return new JobOrderInput(
							alt.getFileType(), 
							type,
							jobOrderInputFiles, 
							jobOrderTimeIntervals, 
							family
					);
				}
			// is PROC input?
			} else {
				final String startDate = convertDateToJobOrderFormat(
						job.getStartTime()
				);
				final String stopDate = convertDateToJobOrderFormat(
						job.getStopTime()
				);											
				final String fileType = elementMapper.mappedFileType(alt.getFileType());
				result = new JobOrderInput(
						alt.getFileType(), // not clear, why this is used and not the mapped fileType
						JobOrderFileNameType.REGEXP,
						Collections.singletonList(new JobOrderInputFile(fileType, "")),
						Collections.singletonList(new JobOrderTimeInterval(
								startDate, 
								stopDate, 
								fileType
						)),
						ProductFamily.BLANK
				);
				// continue with the loop as we still might find a DB alternative
				// no idea, why this was done this way but we keep it for the time being and I've created
				// S1PRO-1273 to cover this observation
			}
		}
		return result;
	}

	/**
	 * Create a list of all alternatives of the tasktable
	 * @return list of all alternatives
	 */
	public List<TaskTableInputAlternative> getAllAlternatives() {
		return taskTable.getPools().stream()
				.flatMap(TaskTablePool::tasks)
				.flatMap(TaskTableTask::inputs)
				.flatMap(TaskTableInput::alternativesOrdered)
				.collect(toList());
	}
	
	public Map<TaskTableInputAlternative.TaskTableInputAltKey, List<TaskTableInputAlternative>> allTaskTableInputAlternatives() {
		return getAllAlternatives().stream()
				.filter(alt -> alt.getOrigin() == TaskTableInputOrigin.DB)
				.collect(groupingBy(TaskTableInputAlternative::getTaskTableInputAltKey));
	}

	private JobOrderFileNameType getFileNameTypeFor(final TaskTableInputAlternative alt) {
		switch (alt.getFileNameType()) {
			case PHYSICAL:
				return JobOrderFileNameType.PHYSICAL;
			case DIRECTORY:
				return JobOrderFileNameType.DIRECTORY;
			case REGEXP:
				return JobOrderFileNameType.REGEXP;
			default:
				// fall through
		}
		return JobOrderFileNameType.BLANK;
	}

	public final Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataQuery> buildMetadataSearchQuery() {
		final Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataQuery> metadataQueryTemplate = new HashMap<>();

		allTaskTableInputAlternatives().forEach((inputAltKey, alternatives) -> {
			metadataQueryTemplate.put(inputAltKey, metadataSearchQueryFor(alternatives.get(0))); //TODO
		});
		return metadataQueryTemplate;
	}

	public SearchMetadataQuery metadataSearchQueryFor(final TaskTableInputAlternative alternative) {
		final TaskTableInputAlternative.TaskTableInputAltKey inputAltKey = alternative.getTaskTableInputAltKey();
		final String fileType = elementMapper.mappedFileType(inputAltKey.getFileType());
		final ProductFamily family = elementMapper.inputFamilyOf(fileType);
		return new SearchMetadataQuery(
				0,
				inputAltKey.getRetrievalMode(),
				inputAltKey.getDeltaTime0(),
				inputAltKey.getDeltaTime1(),
				fileType,
				family
		);
	}

	private JobOrderTimeInterval newJobOrderTimeIntervalFor(final SearchMetadata searchMetadata) {
		return new JobOrderTimeInterval(
				convertDateToJobOrderFormat(searchMetadata.getValidityStart()),
				convertDateToJobOrderFormat(searchMetadata.getValidityStop()),
				searchMetadata.getProductName()
		);
	}
	
	public static String convertDateToJobOrderFormat(final String metadataFormat) {
		return DateUtils.convertToAnotherFormat(
				metadataFormat,
				AbstractMetadata.METADATA_DATE_FORMATTER,
				JobOrderTimeInterval.DATE_FORMATTER
		);
	}

	@Override
	public final String toString() {
		return String.format(
				"Tasktable %s %s (%s)", 
				taskTable.getProcessorName(), 
				taskTable.getVersion(),
				file.getName()
		);
	}
}
