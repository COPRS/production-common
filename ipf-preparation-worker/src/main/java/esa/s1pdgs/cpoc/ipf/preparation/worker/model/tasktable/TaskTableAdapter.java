package esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.converter.TaskTableToJobOrderConverter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderInputFile;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderTimeInterval;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.enums.JobOrderFileNameType;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.metadata.SearchMetadataResult;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.enums.TaskTableInputOrigin;
import esa.s1pdgs.cpoc.metadata.model.AbstractMetadata;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;

public class TaskTableAdapter {
	private final File file;
	private final TaskTable taskTable;
	private final ElementMapper elementMapper;
	
	public TaskTableAdapter(final File file, final TaskTable taskTable, final ElementMapper elementMapper) {
		this.file = file;
		this.taskTable = taskTable;
		this.elementMapper = elementMapper;
	}
	
	public final File file() {
		return file;
	}
	
	public final TaskTable taskTable() {
		return taskTable;
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
	
	public final JobOrder newJobOrder(final ProcessSettings settings) {
		// Build from task table
		final TaskTableToJobOrderConverter converter = new TaskTableToJobOrderConverter();
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
	
	public final JobOrderInput findInput(final JobGen job, final TaskTableInput input, Map<Integer, SearchMetadataResult> results) {
		JobOrderInput result = null;

		for (final TaskTableInputAlternative alt : input.alternativesOrdered().collect(toList())) {
			// We ignore input not DB
			if (alt.getOrigin() == TaskTableInputOrigin.DB) {							
				final List<SearchMetadata> queryResults = results.get(alt.getIdSearchMetadataQuery()).getResult();
				
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
						job.job().getProduct().getStartTime()
				);
				final String stopDate = convertDateToJobOrderFormat(
						job.job().getProduct().getStopTime()
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

	public Map<TaskTableInputAlternative.TaskTableInputAltKey, List<TaskTableInputAlternative>> allTaskTableInputs() {
		return taskTable.getPools().stream()
				.flatMap(TaskTablePool::tasks)
				.flatMap(TaskTableTask::inputs)
				.flatMap(TaskTableInput::alternativesOrdered)
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
	
	private JobOrderTimeInterval newJobOrderTimeIntervalFor(final SearchMetadata searchMetadata) {
		return new JobOrderTimeInterval(
				convertDateToJobOrderFormat(searchMetadata.getValidityStart()),
				convertDateToJobOrderFormat(searchMetadata.getValidityStop()),
				searchMetadata.getProductName()
		);
	}
	
	private String convertDateToJobOrderFormat(final String metadataFormat) {
		return DateUtils.convertToAnotherFormat(
				metadataFormat,
				AbstractMetadata.METADATA_DATE_FORMATTER,
				JobOrderTimeInterval.DATE_FORMATTER
		);
	}
}
