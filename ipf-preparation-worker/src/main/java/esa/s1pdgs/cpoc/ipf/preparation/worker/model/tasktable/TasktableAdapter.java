package esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.enums.TaskTableInputOrigin;
import esa.s1pdgs.cpoc.metadata.client.SearchMetadataQuery;
import esa.s1pdgs.cpoc.metadata.model.AbstractMetadata;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;

public class TasktableAdapter {
	private final File file;
	private final TaskTable taskTable;
	private final ElementMapper elementMapper;
	
	public TasktableAdapter(final File file, final TaskTable taskTable, final ElementMapper elementMapper) {
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
		
		taskTable.getPools().forEach(pool -> {
			tasks.add(pool.getTasks().stream()
					.map(TaskTableTask::getFileName)
					.collect(Collectors.toList()));
		});		
		return tasks;
	}
	
	public final JobOrder newJobOrder(final ProcessSettings settings) {
		// Build from task table
		final TaskTableToJobOrderConverter converter = new TaskTableToJobOrderConverter();
		final JobOrder jobOrderTemplate = converter.apply(taskTable);

		// Update values from configuration file
		jobOrderTemplate.getConf().getProcParams().forEach(item -> {
			elementMapper.getParameterValue(item.getName())
				.ifPresent(val -> item.setValue(val));
		});
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
	
	public final Map<Integer, SearchMetadataQuery> buildMetadataSearchQuery() {
		final AtomicInteger counter = new AtomicInteger(0);
	    final Map<Integer, SearchMetadataQuery> metadataQueryTemplate =  new HashMap<>();
		
	    taskTable.getPools().stream()
			.filter(pool -> !CollectionUtils.isEmpty(pool.getTasks()))
			.flatMap(pool -> pool.getTasks().stream())
			.filter(task -> !CollectionUtils.isEmpty(task.getInputs()))
			.flatMap(task -> task.getInputs().stream())
			.filter(input -> !CollectionUtils.isEmpty(input.getAlternatives()))
			.flatMap(input -> alternatives(input))
			.filter(alt -> alt.getOrigin() == TaskTableInputOrigin.DB)
			.collect(Collectors.groupingBy(TaskTableInputAlternative::getTaskTableInputAltKey))
			.forEach((k, v) -> {
				final int queryId = counter.incrementAndGet();
				final String fileType = elementMapper.mappedFileType(k.getFileType());
				final ProductFamily family = elementMapper.inputFamilyOf(fileType);				
				final SearchMetadataQuery query = new SearchMetadataQuery(
						queryId,
						k.getRetrievalMode(), 
						k.getDeltaTime0(), 
						k.getDeltaTime1(), 
						fileType, 
						family
				);
				metadataQueryTemplate.put(queryId, query);
				// FIXME: It's a very bad idea to rely on getting the original objects here provided and everything
				// to be written through properly. Furthermore, altering the tasktable after having it read is also
				// pretty error-prone and will eventually break at some point in the future (apart from all the WTFs 
				// this will cause on encountering this logic)
				// Hence, this should be changed in order to create the queryId uniquely on the alternative once when
				// reading the tasktable and here, this value can be used as the query id (if no better structure for
				// storing the queries is found).	
				// Apart from that, it's sad to see how functional programming is used to mutate the state of a
				// data structure that doesn't change but here... :(
				v.forEach(alt -> alt.setIdSearchMetadataQuery(queryId));
			});
	    return metadataQueryTemplate;
	}
	
	
	public final JobOrderInput findInput(final JobGen job, final TaskTableInput input) {
		JobOrderInput result = null;
		
		for (final TaskTableInputAlternative alt : alternatives(input).collect(Collectors.toList())) {
			// We ignore input not DB
			if (alt.getOrigin() == TaskTableInputOrigin.DB) {							
				final List<SearchMetadata> queryResults = job.getQueryResultFor(alt).getResult();
				
				// has queries defined?
				if (!CollectionUtils.isEmpty(queryResults)) {
					final JobOrderFileNameType type = getFileNameTypeFor(alt);
					// Retrieve family										
					final ProductFamily family = elementMapper.inputFamilyOf(alt.getFileType());
					
					final List<JobOrderInputFile> jobOrderInputFiles = queryResults.stream()
							.map(file -> new JobOrderInputFile(file.getProductName(), file.getKeyObjectStorage()))
							.collect(Collectors.toList());

					final List<JobOrderTimeInterval> jobOrderTimeIntervals = queryResults.stream()
						.map(m -> newJobOrderTimeIntervalFor(m))
						.collect(Collectors.toList());
					
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
				final String startDate = convertDateToJoborderFormat(
						job.job().getProduct().getStartTime()
				);
				final String stopDate = convertDateToJoborderFormat(
						job.job().getProduct().getStopTime()
				);											
				final String fileType = elementMapper.mappedFileType(alt.getFileType());
				result = new JobOrderInput(
						alt.getFileType(), // not clear, why this is used and not the mapped filetype 
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
	

	private final Stream<TaskTableInputAlternative> alternatives(final TaskTableInput input) {
		return input.getAlternatives().stream().sorted(TaskTableInputAlternative.ORDER);
	}
	
	private final JobOrderFileNameType getFileNameTypeFor(final TaskTableInputAlternative alt) {
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
	
	

	private final JobOrderTimeInterval newJobOrderTimeIntervalFor(final SearchMetadata searchMetadata) {
		return new JobOrderTimeInterval(
				convertDateToJoborderFormat(searchMetadata.getValidityStart()),
				convertDateToJoborderFormat(searchMetadata.getValidityStop()),
				searchMetadata.getProductName()
		);
	}
	
	private final String convertDateToJoborderFormat(final String metadataFormat) {
		return DateUtils.convertToAnotherFormat(
				metadataFormat,
				AbstractMetadata.METADATA_DATE_FORMATTER,
				JobOrderTimeInterval.DATE_FORMATTER
		);
	}

}
