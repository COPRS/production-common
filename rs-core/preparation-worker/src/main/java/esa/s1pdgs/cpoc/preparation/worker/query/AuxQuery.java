package esa.s1pdgs.cpoc.preparation.worker.query;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobPreselectedInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.client.SearchMetadataQuery;
import esa.s1pdgs.cpoc.metadata.model.AbstractMetadata;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.DiscardedException;
import esa.s1pdgs.cpoc.preparation.worker.model.metadata.SearchMetadataResult;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;
import esa.s1pdgs.cpoc.preparation.worker.timeout.InputTimeoutChecker;
import esa.s1pdgs.cpoc.preparation.worker.util.QueryUtils;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInputFile;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderTimeInterval;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableMandatoryEnum;

public class AuxQuery {
	private static final Logger LOGGER = LogManager.getLogger(AuxQuery.class);

	private final MetadataClient metadataClient;
	private final AppDataJob job;
	private final ProductMode mode;
	private final TaskTableAdapter taskTableAdapter;
	private final InputTimeoutChecker timeoutChecker;

	public AuxQuery(final MetadataClient metadataClient, final AppDataJob job, final ProductMode mode,
			 final TaskTableAdapter ttAdapter, final InputTimeoutChecker timeoutChecker) {
		this.metadataClient = metadataClient;
		this.job = job;
		this.mode = mode;
		this.taskTableAdapter = ttAdapter;
		this.timeoutChecker = timeoutChecker;
	}

	public final List<AppDataJobTaskInputs> queryAux() throws DiscardedException {
		LOGGER.debug("Searching required AUX for job {} (product: {})", job.getId(), job.getProductName());
		LOGGER.debug("job: {}", job);
		LOGGER.debug("taskTableAdapter: {}", taskTableAdapter);
		LOGGER.debug("mode: {}", mode);
		List<AppDataJobInput> inputsWithoutResult = inputsWithoutResultsOf(job);
		LOGGER.debug("inputsWithoutResult: {}", inputsWithoutResult);
		List<TaskTableInputAlternative> alternatives = QueryUtils.alternativesOf(inputsWithoutResult, taskTableAdapter, mode);
		LOGGER.debug("alternatives: {}", alternatives);
		final Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataResult> results = performAuxQueriesFor(
				alternatives);
		LOGGER.info("Distributing required AUX for job {} (product: {})", job.getId(), job.getProductName());
		return distributeResults(results);
	}

	private String inputDescription(final String reference, final TaskTableInput input) {
		if (input.toLogMessage() == null) {
			return reference;
		}
		return reference + " (id: " + input.toLogMessage() + ")";
	}

	public final void validate(final AppDataJob job) throws IpfPrepWorkerInputsMissingException {
		final List<AppDataJobInput> missingInputs = inputsWithoutResultsOf(job);
		final Map<String, TaskTableInput> taskTableInputs = taskTableAdapter.taskTableInputs();
		final List<AppDataJobInput> timedOutInputs = new ArrayList<>();

		final Map<String, String> missingMetadata = new HashMap<>();

		for (final AppDataJobInput missingInput : missingInputs) {
			final String ref = missingInput.getTaskTableInputReference();

			final TaskTableInput taskTableInput = taskTableInputs.get(ref);
			final String inputDescription = inputDescription(ref, taskTableInput);

			if (missingInput.isMandatory()) {
				LOGGER.warn("Mandatory Input {} is not available", inputDescription);
				missingMetadata.put(ref + " is missing", taskTableInput.toLogMessage());
			} else {
				// optional input

				// if the timeout is not expired, we want to continue waiting. To do that,
				// a IpfPrepWorkerInputsMissingException needs to be thrown. Otherwise,
				// we log that timeout is expired and we continue anyway behaving as if
				// the input was there
				if (timeoutChecker.isTimeoutExpiredFor(job, taskTableInput)) {
					LOGGER.info("Non-Mandatory Input {} is not available. Continue without it...", inputDescription);
					timedOutInputs.add(missingInput);
				} else {
					LOGGER.info("Waiting for Non-Mandatory Input {} ...", inputDescription);
					missingMetadata.put(inputDescription + " is missing", taskTableInput.toLogMessage());
				}
			}
		}

		if (!missingMetadata.isEmpty()) {
			throw new IpfPrepWorkerInputsMissingException(missingMetadata);
		}

		if(!timedOutInputs.isEmpty()) {
			// remove timed out inputs from job
			LOGGER.info("removing timed out inputs from job {}; {}", job.getId(), timedOutInputs);
			job.getAdditionalInputs().forEach(taskInputs -> {
				final List<AppDataJobInput> inputs = new ArrayList<>(taskInputs.getInputs());
				inputs.removeAll(timedOutInputs);
				taskInputs.setInputs(inputs);
			});
		}

	}

	/**
	 * Calculates when the next timeout for a still missing non-mandatory input is
	 * reached, so we don't wait inifinetly on those.
	 * 
	 * @param job AppDataJob, will get its timeoutDate updated in this method
	 * @param taskTableAdapter responsible taskTableAdapter for the job
	 */
	public void updateTimeout(AppDataJob job, TaskTableAdapter taskTableAdapter) {
		final List<AppDataJobInput> missingInputs = inputsWithoutResultsOf(job);
		final Map<String, TaskTableInput> taskTableInputs = taskTableAdapter.taskTableInputs();
		
		Date nextTimeoutDate = null;
		
		for (final AppDataJobInput missingInput : missingInputs) {
			final String ref = missingInput.getTaskTableInputReference();
			final TaskTableInput taskTableInput = taskTableInputs.get(ref);
			
			// There is no timeout for mandatory products
			if (!missingInput.isMandatory()) {
				LocalDateTime inputTimeoutLocalDateTime = timeoutChecker.timeoutFor(job, taskTableInput);
				if (inputTimeoutLocalDateTime != null) {
					Date inputTimeoutDate = Date.from(inputTimeoutLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
					if (nextTimeoutDate == null || nextTimeoutDate.after(inputTimeoutDate)) {
						nextTimeoutDate = inputTimeoutDate;
					}
				}
			}
		}
		
		// null means all non-mandatory inputs are already timed out, only mandatory ones remain unfilled
		job.setTimeoutDate(nextTimeoutDate);
	}

	private List<AppDataJobInput> inputsWithoutResultsOf(final AppDataJob job) {
		return inputsOf(job).stream()
				.flatMap(taskInputs -> taskInputs.getInputs().stream().filter(input -> !input.getHasResults()))
				.collect(toList());
	}

	private List<AppDataJobTaskInputs> inputsOf(final AppDataJob job) {
		if (isEmpty(job.getAdditionalInputs())) {
			if(job.getPreselectedInputs() != null && !job.getPreselectedInputs().isEmpty()) {
				LOGGER.info("for job {} {} adding preselected inputs with following references {}",
						job.getId(),
						job.getProductName(),
						job.getPreselectedInputs().stream().map(AppDataJobPreselectedInput::getTaskTableInputReference).collect(joining(", ")));
			}

			return QueryUtils.includingPreselected(QueryUtils.buildInitialInputs(taskTableAdapter), job.getPreselectedInputs());
		}

		return job.getAdditionalInputs();
	}

	private Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataResult> toQueries(
			final Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataQuery> metadataQueriesTemplate) {
		return metadataQueriesTemplate.entrySet().stream().collect(
				toMap(Map.Entry::getKey, e -> new SearchMetadataResult(new SearchMetadataQuery(e.getValue()))));
	}

	private Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataResult> performAuxQueriesFor(
			final List<TaskTableInputAlternative> alternatives) {
		final Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataResult> queries = toQueries(
				queryTemplatesFor(alternatives));
		LOGGER.debug("Query inputs for queries {}", queries);
		for (final SearchMetadataResult result : queries.values()) {
			if (result.hasResult()) {
				continue;
			}

			LOGGER.debug("Get query from result {}", result);
			final SearchMetadataQuery query = result.getQuery();
			try {
				LOGGER.debug("Querying input product of type {}, AppJobId {}: {}", query.getProductType(), job.getId(),
						query);

				final List<SearchMetadata> results = queryAux(query);
				// save query results
				// this means, only if query has found something, the result is set
				// otherwise query again later
				// so the same behaviour can be achieved by simply passing result and change
				// getResult()
				// to check null and isEmpty()
				if (!results.isEmpty()) {
					result.setResult(results);
				}
			} catch (final MetadataQueryException me) {
				LOGGER.warn("Exception occurred when searching alternative {} for job {} with product {}: {}",
						query.toLogMessage(), job.getId(), job.getProductName(), me.getMessage());
			}
		}

		return queries;
	}

	private Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataQuery> queryTemplatesFor(
			final List<TaskTableInputAlternative> alternatives) {
		return alternatives.stream().collect(toMap(TaskTableInputAlternative::getTaskTableInputAltKey,
				taskTableAdapter::metadataSearchQueryFor, (existing, replacement) -> existing));
	}

	private List<AppDataJobTaskInputs> distributeResults(
			final Map<TaskTableInputAlternative.TaskTableInputAltKey, SearchMetadataResult> metadataQueries) {
		final Map<String, AppDataJobInput> referenceInputs = new HashMap<>();
		final Map<String, TaskTableInput> taskTableInputs = taskTableAdapter.taskTableInputs();
		final Map<String, String> unsatisfiedReferences = new HashMap<>();

		final List<AppDataJobInput> futureInputs = new ArrayList<>();
		taskTableInputs.forEach((reference, taskTableInput) -> {
			if (StringUtils.isEmpty(taskTableInput.getReference())) {
				// check if input mode matches the product mode
				if (mode.isCompatibleWithTaskTableMode(taskTableInput.getMode())) {
					// returns null, if not found
					final AppDataJobInput foundInput = convert(
							taskTableAdapter.findInput(job, taskTableInput, metadataQueries),
							reference,
							taskTableInput.getMandatory()
					);

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
				if (!addReferredInputFor(reference, taskTableInput.getReference(), referenceInputs, futureInputs)) {
					unsatisfiedReferences.put(reference, taskTableInput.getReference());
				}
			}
		});

		// handle unsatisfied references because of ref input handled before origin
		// input
		unsatisfiedReferences
				.forEach((newInputReference, referredInputReference) -> addReferredInputFor(newInputReference,
						referredInputReference, referenceInputs, futureInputs));

		return mergeInto(futureInputs, inputsOf(job));
	}

	private boolean addReferredInputFor(final String reference, final String referredInputReference,
			final Map<String, AppDataJobInput> references, final List<AppDataJobInput> futureInputs) {
		if (references.containsKey(referredInputReference)) {
			final AppDataJobInput referredInput = references.get(referredInputReference);
			final AppDataJobInput inputForReference = new AppDataJobInput(reference, referredInput);
			LOGGER.info("adding {} referencing {}:{} for job {}", reference, referredInputReference,
					referredInput.getTaskTableInputReference(), job.getId());
			futureInputs.add(inputForReference);
			return true;
		}
		return false;
	}

	private List<AppDataJobTaskInputs> mergeInto(final List<AppDataJobInput> inputsWithResults,
			final List<AppDataJobTaskInputs> jobTaskInputs) {
		final Map<String, AppDataJobInput> newInputs = inputsWithResults.stream()
				.collect(toMap(AppDataJobInput::getTaskTableInputReference, input -> input));

		final List<AppDataJobTaskInputs> mergedJobTaskInputs = new ArrayList<>();

		for (final AppDataJobTaskInputs jobTaskInput : jobTaskInputs) {

			mergedJobTaskInputs.add(new AppDataJobTaskInputs(jobTaskInput.getTaskName(), jobTaskInput.getTaskVersion(),
					mergeInputs(newInputs, jobTaskInput)));
		}

		LOGGER.trace("merging inputs {} into job inputs {} result {}", inputsWithResults, job.getAdditionalInputs(),
				mergedJobTaskInputs);

		return mergedJobTaskInputs;
	}

	private List<AppDataJobInput> mergeInputs(final Map<String, AppDataJobInput> newInputs,
			final AppDataJobTaskInputs jobTaskInput) {
		final List<AppDataJobInput> mergedInputs = new ArrayList<>();

		for (final AppDataJobInput jobInput : jobTaskInput.getInputs()) {
			mergedInputs.add(newInputs.getOrDefault(jobInput.getTaskTableInputReference(), jobInput));
		}
		return mergedInputs;
	}

	// TODO TaskTableAdapter by itself should not return a JobOrderInput but a more
	// generic
	// structure which solely holds the reference between task table input and
	// search meta data result
	// as long as this is not changed the JobOrderInput has to be converted to
	// AppDataJobInput here
	private AppDataJobInput convert(final JobOrderInput input, final String inputReference,
			final TaskTableMandatoryEnum mandatory) {
		if (input == null) {
			return null;
		}

		// TODO there is not check if fileNames and intervals are consistent
		final Map<String, JobOrderInputFile> fileNames = input.getFilenames().stream()
				.collect(toMap(JobOrderInputFile::getFilename, fn -> fn));
		return new AppDataJobInput(inputReference, input.getFileType(), input.getFileNameType().toString(),
				TaskTableMandatoryEnum.YES.equals(mandatory), input.getTimeIntervals().stream()
						.map(ti -> merge(fileNames.get(ti.getFileName()), ti, input.getT0_pdgs_date())).collect(toList()));
	}

	private AppDataJobFile merge(final JobOrderInputFile file, final JobOrderTimeInterval interval, final Date t0) {
		return new AppDataJobFile(
				file.getFilename(), 
				file.getKeyObjectStorage(), 
				interval.getStart(),
				interval.getStop(),
				t0
		);
	}

	private List<SearchMetadata> queryAux(final SearchMetadataQuery query) throws MetadataQueryException {
		final AppDataJobProductAdapter productAdapter = new AppDataJobProductAdapter(job.getProduct());
		
		// S1PRO-2111: log query attempts to MDC
		LOGGER.info("Querying product for type '{}' using startTime '{}' and stopTime '{}' using '{}'", 
				query.getProductType(), job.getStartTime(), job.getStopTime(), 
				query.getRetrievalMode());
		
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
		if ("AUX_ECE".equalsIgnoreCase(productType)) {
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
		return DateUtils.convertToAnotherFormat(metadataFormat, AppDataJobProduct.TIME_FORMATTER,
				AbstractMetadata.METADATA_DATE_FORMATTER);
	}
}
