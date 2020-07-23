package esa.s1pdgs.cpoc.ipf.preparation.worker.query;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.metadata.SearchMetadataResult;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.timeout.InputTimeoutChecker;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.client.SearchMetadataQuery;
import esa.s1pdgs.cpoc.metadata.model.AbstractMetadata;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInputFile;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderTimeInterval;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTablePool;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableTask;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableInputOrigin;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableMandatoryEnum;

public class AuxQuery {
	private static final Logger LOGGER = LogManager.getLogger(AuxQuery.class);
	
	private final MetadataClient metadataClient;
	private final AppDataJob job;
	private final ProductMode mode;
	private final InputTimeoutChecker timeoutChecker;
	private final TaskTableAdapter taskTableAdapter;

	public AuxQuery(
			final MetadataClient metadataClient,
			final AppDataJob job,
			final ProductMode mode,
			final InputTimeoutChecker timeoutChecker,
			final TaskTableAdapter ttAdapter) {
		this.metadataClient = metadataClient;
		this.job = job;
		this.mode = mode; 
		this.timeoutChecker = timeoutChecker;
		this.taskTableAdapter = ttAdapter;
	}

	public final List<AppDataJobTaskInputs>  queryAux() {
		LOGGER.debug("Searching required AUX for job {} (product: {})", job.getId(), job.getProductName());
		final Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataResult> results =
				performAuxQueriesFor(alternativesOf(inputsWithoutResultsOf(job)));
		LOGGER.info("Distributing required AUX for job {} (product: {})", job.getId(), job.getProductName());
		return distributeResults(results);
	}

	public final void validate(final AppDataJob job) throws IpfPrepWorkerInputsMissingException {
		final List<AppDataJobInput> missingInputs = inputsWithoutResultsOf(job);
		final Map<String, TaskTableInput> taskTableInputs = taskTableInputs();

		final Map<String, String> missingMetadata = new HashMap<>();

		for (final AppDataJobInput missingInput : missingInputs) {
			final TaskTableInput taskTableInput = taskTableInputs.get(missingInput.getTaskTableInputReference());
			if (missingInput.isMandatory()) {
				missingMetadata.put(						
						missingInput.getTaskTableInputReference() + " is missing",
						taskTableInput.toLogMessage()
				);
			} else {
				// optional input

				// if the timeout is not expired, we want to continue waiting. To do that,
				// a IpfPrepWorkerInputsMissingException needs to be thrown. Otherwise,
				// we log that timeout is expired and we continue anyway behaving as if
				// the input was there
				if (timeoutChecker.isTimeoutExpiredFor(job, taskTableInput)) {
					LOGGER.info("Non-Mandatory Input {} is not available. Continue without it...",
							taskTableInput.toLogMessage());
				}
				else {
					missingMetadata.put(
							missingInput.getTaskTableInputReference() + " is missing",
							taskTableInput.toLogMessage()						
					);
				}
			}
		}

		if(!missingInputs.isEmpty()) {
			throw new IpfPrepWorkerInputsMissingException(missingMetadata);
		}
	}

	private List<AppDataJobInput> inputsWithoutResultsOf(final AppDataJob job) {
		return inputsOf(job).stream()
				.flatMap(taskInputs -> taskInputs.getInputs().stream().filter(input -> !input.hasResults()))
				.collect(toList());
	}

	private List<TaskTableInputAlternative> alternativesOf(
			final List<AppDataJobInput> inputsWithoutResults) {

		final List<String> inputWithoutResultReferences =
				inputsWithoutResults.stream().map(AppDataJobInput::getTaskTableInputReference).collect(toList());

		final Map<String, List<TaskTableInputAlternative>> taskTableAlternativesMappedToReferences =
				inputsMappedTo((reference, input) -> singletonMap(reference, input.getAlternatives()))
						.stream()
						.flatMap(map -> map.entrySet().stream())
						.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		return taskTableAlternativesMappedToReferences.entrySet().stream()
				.filter(entry -> inputWithoutResultReferences.contains(entry.getKey()))
				.flatMap(entry -> entry.getValue().stream())
				.filter(alt -> alt.getOrigin() == TaskTableInputOrigin.DB)
				.distinct()
				.collect(toList());
	}

	private List<AppDataJobTaskInputs> inputsOf(final AppDataJob job) {
		if(isEmpty(job.getAdditionalInputs())) {
			return buildInitialInputs();
		}

		return job.getAdditionalInputs();
	}

	private <T, U> List<U> taskTableTasksAndInputsMappedTo(final BiFunction<String, TaskTableInput, T> inputMapFunction,
														   final BiFunction<List<T>, TaskTableTask, U> taskMapFunction) {
		final List<U> mappedTasks = new ArrayList<>();

		for (final Map.Entry<String, TaskTableTask> taskEntry : taskTableTasks().entrySet()) {
			final List<T> mappedInputs = new ArrayList<>();
			int inputNumber = 0;
			for (final TaskTableInput input : taskEntry.getValue().getInputs()) {
				final String reference = String.format("%sI%s", taskEntry.getKey(), inputNumber++);
				mappedInputs.add(inputMapFunction.apply(reference, input));
			}
			mappedTasks.add(taskMapFunction.apply(mappedInputs, taskEntry.getValue()));
		}

		return mappedTasks;
	}

	private <T> List<T> inputsMappedTo(final BiFunction<String, TaskTableInput, T> inputMapFunction) {
		return taskTableTasksAndInputsMappedTo(inputMapFunction, (list, task) -> list)
				.stream().flatMap(Collection::stream).collect(toList());
	}

	private Map<String, TaskTableInput> taskTableInputs() {
		return inputsMappedTo(Collections::singletonMap).stream().flatMap(map -> map.entrySet().stream())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private Map<String, TaskTableTask> taskTableTasks() {
		final Map<String, TaskTableTask> tasks = new HashMap<>();
		int poolNumber = 0; // I wish Java had list.stream((index, entry) -> ...)
		for (final TaskTablePool pool : taskTableAdapter.pools()) {
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


	private List<AppDataJobTaskInputs> buildInitialInputs() {
		return taskTableTasksAndInputsMappedTo(
				(reference, input) -> new AppDataJobInput(
						reference,
						"",
						"",
						TaskTableMandatoryEnum.YES.equals(input.getMandatory()),
						emptyList()),
				(jobInputList, task) -> new AppDataJobTaskInputs(task.getName(), task.getVersion(), jobInputList));
	}

	private Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataResult> toQueries(final Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataQuery> metadataQueriesTemplate) {
		return metadataQueriesTemplate.entrySet().stream().collect(
				toMap(
						Map.Entry::getKey,
						e -> new SearchMetadataResult(new SearchMetadataQuery(e.getValue())))
		);
	}

	private Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataResult> performAuxQueriesFor(final List<TaskTableInputAlternative> alternatives) {
		final Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataResult> queries = toQueries(queryTemplatesFor(alternatives));

		for (final SearchMetadataResult result : queries.values()) {
			if (result.hasResult()) {
				continue;
			}

			final SearchMetadataQuery query = result.getQuery();
			try {				
				LOGGER.debug("Querying input product of type {}, AppJobId {}: {}", 
						query.getProductType(), job.getId(), query);

				final List<SearchMetadata> results = queryAux(query);
				// save query results
				// this means, only if query has found something, the result is set
				// otherwise query again later
				// so the same behaviour can be achieved by simply passing result and change getResult()
				// to check null and isEmpty()
				if (!results.isEmpty()) {
					result.setResult(results);
				}
			} catch (final MetadataQueryException me) {
				LOGGER.warn("Exception occurred when searching alternative {} for job {} with product {}: {}",
						query.toLogMessage(),
						job.getId(), 
						job.getProductName(), 
						me.getMessage()
				);
			}
		}

		return queries;
	}

	private Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataQuery> queryTemplatesFor(final List<TaskTableInputAlternative> alternatives) {
		return alternatives.stream()
				.collect(toMap(
						TaskTableInputAlternative::getTaskTableInputAltKey,
						taskTableAdapter::metadataSearchQueryFor));
	}

	private List<AppDataJobTaskInputs> distributeResults(final Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataResult> metadataQueries) {
		final Map<String, AppDataJobInput> referenceInputs = new HashMap<>();
		final Map<String, TaskTableInput> taskTableInputs = taskTableInputs();
		final Map<String, String> unsatisfiedReferences = new HashMap<>();

		final List<AppDataJobInput> futureInputs = new ArrayList<>();
		taskTableInputs.forEach((reference, taskTableInput) -> {
			if (StringUtils.isEmpty(taskTableInput.getReference())) {
				//check if input mode matches the product mode
				if (mode.isCompatibleWithTaskTableMode(taskTableInput.getMode())) {
					// returns null, if not found
					final AppDataJobInput foundInput = convert(
							taskTableAdapter.findInput(job, taskTableInput, metadataQueries), reference, taskTableInput.getMandatory());

					if (foundInput != null) {
						LOGGER.info("found input {} for job {}", foundInput, job.getId());
						foundInput.setHasResults(true);
						futureInputs.add(foundInput);
						if (!StringUtils.isEmpty(taskTableInput.getId())) {
							referenceInputs.put(taskTableInput.getId(), foundInput);
						}
					}
				}
			} else {
				// We shall add inputs of the reference
				if(!addReferredInputFor(reference, taskTableInput.getReference(), referenceInputs, futureInputs)) {
					unsatisfiedReferences.put(reference, taskTableInput.getReference());
				}
			}
		});

		//handle unsatisfied references because of ref input handled before origin input
		unsatisfiedReferences.forEach((newInputReference, referredInputReference)
				-> addReferredInputFor(newInputReference, referredInputReference, referenceInputs, futureInputs));

		return mergeInto(futureInputs, inputsOf(job));
	}

	private boolean addReferredInputFor(final String reference, final String referredInputReference, final Map<String, AppDataJobInput> references, final List<AppDataJobInput> futureInputs) {
		if (references.containsKey(referredInputReference)) {
			final AppDataJobInput referredInput = references.get(referredInputReference);
			final AppDataJobInput inputForReference = new AppDataJobInput(reference, referredInput);
			LOGGER.info("adding {} referencing {}:{} for job {}", reference, referredInputReference,  referredInput.getTaskTableInputReference(), job.getId());
			futureInputs.add(inputForReference);
			return true;
		}
		return false;
	}

	private List<AppDataJobTaskInputs> mergeInto(final List<AppDataJobInput> inputsWithResults, final List<AppDataJobTaskInputs> jobTaskInputs) {
		final Map<String, AppDataJobInput> newInputs
				= inputsWithResults.stream().collect(toMap(AppDataJobInput::getTaskTableInputReference, input -> input));

		final List<AppDataJobTaskInputs> mergedJobTaskInputs = new ArrayList<>();

		for (AppDataJobTaskInputs jobTaskInput : jobTaskInputs) {

			mergedJobTaskInputs.add
					(new AppDataJobTaskInputs(jobTaskInput.getTaskName(), jobTaskInput.getTaskVersion(), mergeInputs(newInputs, jobTaskInput)));
		}

		LOGGER.debug("merging inputs {} into job inputs {} result {}", inputsWithResults, job.getAdditionalInputs(), mergedJobTaskInputs);

		return mergedJobTaskInputs;
	}

	private List<AppDataJobInput> mergeInputs(Map<String, AppDataJobInput> newInputs, AppDataJobTaskInputs jobTaskInput) {
		final List<AppDataJobInput> mergedInputs = new ArrayList<>();

		for (AppDataJobInput jobInput : jobTaskInput.getInputs()) {
			mergedInputs.add(newInputs.getOrDefault(jobInput.getTaskTableInputReference(), jobInput));
		}
		return mergedInputs;
	}

	// TODO TaskTableAdapter by itself should not return a JobOrderInput but a more generic
	// structure which solely holds the reference between task table input and search meta data result
	// as long as this is not changed the JobOrderInput has to be converted to AppDataJobInput here
	private AppDataJobInput convert(final JobOrderInput input, final String inputReference, final TaskTableMandatoryEnum mandatory) {
		if(input == null) {
			return null;
		}

		//TODO there is not check if fileNames and intervals are consistent
		final Map<String, JobOrderInputFile> fileNames = input.getFilenames().stream().collect(toMap(JobOrderInputFile::getFilename, fn -> fn));
		return new AppDataJobInput(inputReference, input.getFileType(), input.getFileNameType().toString(), TaskTableMandatoryEnum.YES.equals(mandatory),
				input.getTimeIntervals().stream().map(ti -> merge(fileNames.get(ti.getFileName()), ti)).collect(toList()));
	}

	private AppDataJobFile merge(final JobOrderInputFile file, final JobOrderTimeInterval interval) {
		return new AppDataJobFile(
				file.getFilename(), file.getKeyObjectStorage(),
				interval.getStart(), interval.getStop());
	}

	private List<SearchMetadata> queryAux(final SearchMetadataQuery query) throws MetadataQueryException {
		final AppDataJobProductAdapter productAdapter = new AppDataJobProductAdapter(job.getProduct());
		
		return metadataClient.search(
				query,
				sanitizeDateString(job.getStartTime()),
				sanitizeDateString(job.getStopTime()),
				productAdapter.getSatelliteId(),
				productAdapter.getInsConfId(),
				productAdapter.getProcessMode(), 
				polarisationFor(query.getProductType())
		);
	}

	
	// S1PRO-707: only "AUX_ECE" requires to query polarisation
	private String polarisationFor(final String productType) {
		if ("AUX_ECE".equals(productType.toUpperCase())) {
			final AppDataJobProductAdapter productAdapter = new AppDataJobProductAdapter(job.getProduct());		
			
			final String polarisation = productAdapter.getStringValue("polarisation", "NONE").toUpperCase();
			if (polarisation.equals("SV") || polarisation.equals("DV")) {
				return "V";
			} else if (polarisation.equals("SH") || polarisation.equals("DH")) {
				return "H";
			}
			return "NONE";
		}
		return null;
	}
		
	private String sanitizeDateString(final String metadataFormat) {
		return DateUtils.convertToAnotherFormat(
				metadataFormat,
				AppDataJobProduct.TIME_FORMATTER,
				AbstractMetadata.METADATA_DATE_FORMATTER
		);
	}
}
