package esa.s1pdgs.cpoc.ipf.preparation.worker.type.s3;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.io.File;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.appcat.AppCatJobService;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.S3TypeAdapterSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.ElementMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableFactory;
import esa.s1pdgs.cpoc.ipf.preparation.worker.query.QueryUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative;

public class S3TypeAdapter extends AbstractProductTypeAdapter implements ProductTypeAdapter {

	private static final Logger LOGGER = LogManager.getLogger(S3TypeAdapter.class);

	private MetadataClient metadataClient;
	private TaskTableFactory ttFactory;
	private ElementMapper elementMapper;
	private ProcessSettings processSettings;
	private IpfPreparationWorkerSettings workerSettings;
	private S3TypeAdapterSettings settings;

	public S3TypeAdapter(final MetadataClient metadataClient, final TaskTableFactory ttFactory,
			final ElementMapper elementMapper, final ProcessSettings processSettings,
			final IpfPreparationWorkerSettings workerSettings, final S3TypeAdapterSettings settings) {
		this.metadataClient = metadataClient;
		this.ttFactory = ttFactory;
		this.elementMapper = elementMapper;
		this.processSettings = processSettings;
		this.workerSettings = workerSettings;
		this.settings = settings;
	}

	@Override
	public Product mainInputSearch(AppDataJob job) throws IpfPrepWorkerInputsMissingException {
		S3Product returnValue = S3Product.of(job);

		// Create tasktable Adapter for tasktable defined by Job
		final TaskTableAdapter tasktableAdapter = getTTAdapterForTaskTableName(job.getTaskTableName());

		// Discard logic for OLCI Calibration
		if (settings.getOlciCalibration().contains(tasktableAdapter.taskTable().getProcessorName())) {
			LOGGER.debug("Use additional logic 'OLCICalibrationFilter' for tasktable processor {}",
					tasktableAdapter.taskTable().getProcessorName());
			OLCICalibrationFilter olciCalFilter = new OLCICalibrationFilter(metadataClient, elementMapper);
			try {
				boolean discardJob = olciCalFilter.checkIfJobShouldBeDiscarded(job.getProductName(),
						tasktableAdapter.taskTable().getProcessorName());
				if (discardJob) {
					LOGGER.info("Discard job {} because of 'OLCICalibrationFilter'", job.getId());
					// Skip the rest of the mainInputSearch - job is discarded anyways
					returnValue.setDiscardJob(discardJob);
					return returnValue;
				}
			} catch (final MetadataQueryException me) {
				LOGGER.error("Error on query execution, retrying next time", me);
			}
		}

		// Get inputs of tasks
		List<AppDataJobTaskInputs> tasks;
		if (isEmpty(job.getAdditionalInputs())) {
			tasks = QueryUtils.buildInitialInputs(workerSettings.getProductMode(), tasktableAdapter);
		} else {
			tasks = job.getAdditionalInputs();
		}

		// Extract a list of all inputs without complete result sets
		List<AppDataJobInput> inputsWithNoResults = tasks.stream()
				.flatMap(taskInputs -> taskInputs.getInputs().stream().filter(input -> !input.getHasResults()))
				.collect(toList());

		// Create list of alternatives of those inputs
		List<TaskTableInputAlternative> alternatives = QueryUtils.alternativesOf(inputsWithNoResults, tasktableAdapter,
				workerSettings.getProductMode());

		// Loop over alternatives to execute additional logic per product type
		for (TaskTableInputAlternative alternative : alternatives) {
			try {
				if (settings.getMpcSearch(tasktableAdapter.taskTable().getProcessorName())
						.contains(alternative.getFileType())) {
					LOGGER.debug("Use additional logic 'MultipleProductCoverSearch (MarginTT)' for product type {}",
							alternative.getFileType());
					MultipleProductCoverSearch mpcSearch = new MultipleProductCoverSearch(tasktableAdapter,
							elementMapper, metadataClient, workerSettings);
					tasks = mpcSearch.updateTaskInputsByAlternative(tasks, alternative, returnValue);
				}

				if (settings.isRangeSearchActiveForProductType(tasktableAdapter.taskTable().getProcessorName(),
						alternative.getFileType())) {
					LOGGER.debug("Use additional logic 'MultipleProductCoverSearch (Viscal)' for product type {}",
							alternative.getFileType());
					MultipleProductCoverSearch mpcSearch = new MultipleProductCoverSearch(tasktableAdapter,
							elementMapper, metadataClient, workerSettings);
					tasks = mpcSearch.updateTaskInputsForViscal(tasks, alternative, job, returnValue);

					/*
					 * In a following step the start and stop time of the job will be set to the
					 * start and stop time of the product. That step is ruining the
					 * RangeSearch-logic so we anticipate that update and preemptively set the
					 * products start and stop time to the ones from the job
					 * 
					 * @see AppDataJobService#updateProduct()
					 */
					returnValue.setStartTime(job.getStartTime());
					returnValue.setStopTime(job.getStopTime());
				}
			} catch (final MetadataQueryException me) {
				LOGGER.error("Error on query execution, retrying next time", me);
			}
		}

		returnValue.setAdditionalInputs(tasks);
		return returnValue;
	}

	@Override
	public void customAppDataJob(AppDataJob job) {
		// Create tasktable Adapter for tasktable defined by Job
		final TaskTableAdapter ttAdapter = getTTAdapterForTaskTableName(job.getTaskTableName());

		String productType = job.getProductName().substring(4, 15);

		/*
		 * Change time interval for job when range search is active This is needed for
		 * VISCAL (SLSTR Calibration)
		 */
		if (settings.isRangeSearchActiveForProductType(ttAdapter.taskTable().getProcessorName(), productType)) {
			S3TypeAdapterSettings.RangeCoverSettings rangeSettings = settings.getRangeSearch()
					.get(ttAdapter.taskTable().getProcessorName());

			MultipleProductCoverSearch mpcSearch = new MultipleProductCoverSearch(ttAdapter, elementMapper,
					metadataClient, workerSettings);

			try {
				MultipleProductCoverSearch.Range range = mpcSearch.getIntersectingANXRange(job.getProductName(),
						rangeSettings.getAnxOffsetInS(), rangeSettings.getRangeLengthInS());

				if (range != null) {
					job.setStartTime(DateUtils.formatToMetadataDateTimeFormat(range.getStart()));
					job.setStopTime(DateUtils.formatToMetadataDateTimeFormat(range.getStop()));
				} else {
					// Discard Job
					job.setState(AppDataJobState.TERMINATED);
				}
			} catch (MetadataQueryException e) {
				LOGGER.error("Error while determining viscal range, skip changing interval for AppDataJob", e);
			}
		}
	}

	@Override
	public void customJobOrder(AppDataJob job, JobOrder jobOrder) {
		// Nothing to do currently
	}

	@Override
	public void customJobDto(AppDataJob job, IpfExecutionJob dto) {
		// Nothing to do currently
	}

	@Override
	public void validateInputSearch(AppDataJob job) throws IpfPrepWorkerInputsMissingException {
		// Check if timeout is reached -> start job with current input
		if (workerSettings.getWaitprimarycheck().getMaxTimelifeS() != 0) {
			long startTime = job.getGeneration().getCreationDate().toInstant().toEpochMilli();
			long timeoutTime = startTime + (workerSettings.getWaitprimarycheck().getMaxTimelifeS() * 1000);

			if (Instant.now().toEpochMilli() > timeoutTime) {
				// Timeout reached
				LOGGER.info("Timeout reached for job {}. Continue without missing products...", job.getId());
				setHasResultsToTrueForTimeoutReached(job);
				return;
			}
		}

		// Check that each Input which should have results contains results or that
		// timeout is expired
		final TaskTableAdapter taskTableAdapter = getTTAdapterForTaskTableName(job.getTaskTableName());

		// Extract a list of all inputs from the tasks
		List<AppDataJobInput> inputsWithNoResults = job.getAdditionalInputs().stream()
				.flatMap(taskInputs -> taskInputs.getInputs().stream().filter(input -> !input.getHasResults()))
				.collect(toList());

		// Create list of alternatives
		List<TaskTableInputAlternative> alternatives = QueryUtils.alternativesOf(inputsWithNoResults, taskTableAdapter,
				workerSettings.getProductMode());

		// Check if there is an alternative which should have been filled by additional
		// logic
		Map<String, String> missingAlternatives = new HashMap<>();
		for (TaskTableInputAlternative alternative : alternatives) {
			if (settings.getMpcSearch(taskTableAdapter.taskTable().getProcessorName())
					.contains(alternative.getFileType())
					|| settings.isRangeSearchActiveForProductType(taskTableAdapter.taskTable().getProcessorName(),
							alternative.getFileType())) {
				missingAlternatives.put(alternative.getFileType(),
						"Incomplete result set for " + alternative.getFileType());
			}
		}

		// If there is at least one input incomplete, try again
		if (!missingAlternatives.isEmpty()) {
			throw new IpfPrepWorkerInputsMissingException(missingAlternatives);
		}
	}

	@Override
	public Optional<AppDataJob> findAssociatedJobFor(final AppCatJobService appCat, final CatalogEventAdapter catEvent,
			final AppDataJob job) throws AbstractCodedException {
		// Create tasktable Adapter for tasktable defined by Job
		final TaskTableAdapter ttAdapter = getTTAdapterForTaskTableName(job.getTaskTableName());
		String productType = job.getProductName().substring(4, 15);

		/*
		 * For VISCAL check if there already exists a job, that is responsible for the
		 * given interval
		 */
		if (settings.isRangeSearchActiveForProductType(ttAdapter.taskTable().getProcessorName(), productType)) {
			LOGGER.debug("Look for existing job for productType {}", productType);
			Optional<List<AppDataJob>> jobsInDatabase = appCat.findJobsForProductType(productType);

			if (jobsInDatabase.isPresent()) {
				MultipleProductCoverSearch.Range newJobRange = new MultipleProductCoverSearch.Range(
						DateUtils.parse(job.getStartTime()), DateUtils.parse(job.getStopTime()));

				for (AppDataJob jobInDatabase : jobsInDatabase.get()) {
					MultipleProductCoverSearch.Range databaseJobRange = new MultipleProductCoverSearch.Range(
							DateUtils.parse(jobInDatabase.getStartTime()),
							DateUtils.parse(jobInDatabase.getStopTime()));

					if (jobInDatabase.getTaskTableName().equals(job.getTaskTableName())
							&& newJobRange.intersects(databaseJobRange)) {
						return Optional.of(jobInDatabase);
					}
				}
			}
		}

		return Optional.empty();
	}

	/**
	 * When the timeout was reached set the boolean "hasResults" for all input to
	 * true, that contain at least one product. The boolean "hasResults" is kept at
	 * false from the additional logic, to determine whether or not the list of
	 * products is enough to satisfy the additional constraints
	 * 
	 * @param job job to update
	 */
	private void setHasResultsToTrueForTimeoutReached(AppDataJob job) {
		for (AppDataJobTaskInputs task : job.getAdditionalInputs()) {
			for (AppDataJobInput input : task.getInputs()) {
				if (!isEmpty(input.getFiles())) {
					input.setHasResults(true);
				}
			}
		}
	}

	/**
	 * Create a TaskTableAdapter for the given tasktable
	 * 
	 * @param taskTable name of the taskTable
	 * @return TaskTableAdapter to access the tasktable information
	 */
	private TaskTableAdapter getTTAdapterForTaskTableName(String taskTable) {
		final File ttFile = new File(workerSettings.getDiroftasktables(), taskTable);
		final TaskTableAdapter tasktableAdapter = new TaskTableAdapter(ttFile,
				ttFactory.buildTaskTable(ttFile, processSettings.getLevel()), elementMapper);

		return tasktableAdapter;
	}
}
