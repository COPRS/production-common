package esa.s1pdgs.cpoc.ipf.preparation.worker.type.s3;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.io.File;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.appcat.AppCatJobService;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.S3TypeAdapterSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.S3TypeAdapterSettings.MPCSearchSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.generator.DiscardedException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.TimeInterval;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.ElementMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableFactory;
import esa.s1pdgs.cpoc.ipf.preparation.worker.query.QueryUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.xml.model.joborder.AbstractJobOrderProc;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInputFile;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderTimeInterval;
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
	public List<AppDataJob> createAppDataJobs(IpfPreparationJob job) {
		AppDataJob appDataJob = AppDataJob.fromPreparationJob(job);
		if (processSettings.getProcessingGroup() != null) {
			appDataJob.setProcessingGroup(processSettings.getProcessingGroup());
		}

		// Add more metadata to AppDataJob
		appDataJob.getProduct().getMetadata().putAll(job.getEventMessage().getBody().getMetadata());

		// Create tasktable Adapter for tasktable defined by Job
		final TaskTableAdapter ttAdapter = getTTAdapterForTaskTableName(appDataJob.getTaskTableName());

		String productType = appDataJob.getProductName().substring(4, 15);

		/*
		 * Change time interval for job when range search is active. This is needed for
		 * VISCAL (SLSTR Calibration)
		 */
		if (settings.isRangeSearchActiveForProductType(ttAdapter.taskTable().getProcessorName(), productType)) {
			S3TypeAdapterSettings.RangeCoverSettings rangeSettings = settings.getRangeSearch()
					.get(ttAdapter.taskTable().getProcessorName());

			MultipleProductCoverSearch mpcSearch = new MultipleProductCoverSearch(ttAdapter, elementMapper,
					metadataClient, workerSettings);

			try {
				TimeInterval range = mpcSearch.getIntersectingANXRange(appDataJob.getProductName(),
						rangeSettings.getAnxOffsetInS(), rangeSettings.getRangeLengthInS());

				if (range != null) {
					appDataJob.setStartTime(DateUtils.formatToMetadataDateTimeFormat(range.getStart()));
					appDataJob.setStopTime(DateUtils.formatToMetadataDateTimeFormat(range.getStop()));
				} else {
					// Discard Job
					LOGGER.info("Product not in range. Skip creating AppDataJob.");
					return Collections.emptyList();
				}
			} catch (MetadataQueryException e) {
				LOGGER.error("Error while determining viscal range, skip changing interval for AppDataJob", e);
			}
		}

		// Default case
		return Collections.singletonList(appDataJob);
	}

	@Override
	public Product mainInputSearch(final AppDataJob job, final TaskTableAdapter tasktableAdapter)
			throws IpfPrepWorkerInputsMissingException {
		final S3Product returnValue = S3Product.of(job);

		// Discard logic for OLCI Calibration
		if (settings.getOlciCalibration().contains(tasktableAdapter.taskTable().getProcessorName())) {
			LOGGER.debug("Use additional logic 'OLCICalibrationFilter' for tasktable processor {}",
					tasktableAdapter.taskTable().getProcessorName());
			final OLCICalibrationFilter olciCalFilter = new OLCICalibrationFilter(metadataClient, elementMapper);
			try {
				final boolean discardJob = olciCalFilter.checkIfJobShouldBeDiscarded(job.getProductName(),
						tasktableAdapter.taskTable().getProcessorName());
				if (discardJob) {
					LOGGER.info("Discard job {} because of 'OLCICalibrationFilter'", job.getId());
					// Skip the rest of the mainInputSearch - job is discarded anyways
					throw new DiscardedException(
							String.format("Discard job %s because of 'OLCICalibrationFilter'", job.getId()));
				}
			} catch (final MetadataQueryException me) {
				LOGGER.error("Error on query execution, retrying next time", me);
			}
		}

		// Get inputs of tasks
		List<AppDataJobTaskInputs> tasks;
		if (isEmpty(job.getAdditionalInputs())) {
			tasks = QueryUtils.buildInitialInputs(tasktableAdapter);
		} else {
			tasks = job.getAdditionalInputs();
		}

		// Extract a list of all inputs without complete result sets
		final List<AppDataJobInput> inputsWithNoResults = tasks.stream()
				.flatMap(taskInputs -> taskInputs.getInputs().stream().filter(input -> !input.getHasResults()))
				.collect(toList());

		// Create list of alternatives of those inputs
		final List<TaskTableInputAlternative> alternatives = QueryUtils.alternativesOf(inputsWithNoResults,
				tasktableAdapter, workerSettings.getProductMode());

		// Loop over alternatives to execute additional logic per product type
		for (final TaskTableInputAlternative alternative : alternatives) {
			try {
				if (settings.isMPCSearchActiveForProductType(tasktableAdapter.taskTable().getProcessorName(),
						alternative.getFileType())) {
					LOGGER.debug("Use additional logic 'MultipleProductCoverSearch (MarginTT)' for product type {}",
							alternative.getFileType());

					MPCSearchSettings mpcSettings = settings.getMpcSearch()
							.get(tasktableAdapter.taskTable().getProcessorName());

					final MultipleProductCoverSearch mpcSearch = new MultipleProductCoverSearch(tasktableAdapter,
							elementMapper, metadataClient, workerSettings, mpcSettings.isDisableFirstLastWaiting());
					tasks = mpcSearch.updateTaskInputs(tasks, alternative, returnValue.getSatelliteId(),
							returnValue.getStartTime(), returnValue.getStopTime(), alternative.getDeltaTime0(),
							alternative.getDeltaTime1(), "NRT");
				}

				if (settings.isRangeSearchActiveForProductType(tasktableAdapter.taskTable().getProcessorName(),
						alternative.getFileType())) {
					LOGGER.debug("Use additional logic 'MultipleProductCoverSearch (Viscal)' for product type {}",
							alternative.getFileType());
					final MultipleProductCoverSearch mpcSearch = new MultipleProductCoverSearch(tasktableAdapter,
							elementMapper, metadataClient, workerSettings);
					tasks = mpcSearch.updateTaskInputs(tasks, alternative, returnValue.getSatelliteId(),
							job.getStartTime(), job.getStopTime(), "NRT");

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
	public void customJobOrder(final AppDataJob job, final JobOrder jobOrder) {
		TaskTableAdapter taskTableAdapter = getTTAdapterForTaskTableName(job.getTaskTableName());

		LOGGER.debug("Fill dynamic process parameters based on tasktable {}", job.getTaskTableName());
		/*
		 * For each dynamic process parameter defined in the tasktable do the following:
		 * 
		 * 1. Extract the default value 2. If we have a static configuration for this
		 * parameter name in the s3 type settings, use that value 3. If the parameter
		 * name is part of the main product metadata, use the value of the metadata
		 * 
		 * If the resulting value is not null, write the parameter on the job order
		 */
		taskTableAdapter.taskTable().getDynProcParams().forEach(dynProcParam -> {
			LOGGER.trace("Handle dynamic process parameter \"{}\"", dynProcParam.getName());
			String result = dynProcParam.getDefaultValue();

			if (this.settings.getDynProcParams().containsKey(dynProcParam.getName())) {
				result = this.settings.getDynProcParams().get(dynProcParam.getName());
			}

			if (job.getProduct().getMetadata().containsKey(dynProcParam.getName())) {
				result = job.getProduct().getMetadata().get(dynProcParam.getName()).toString();
			}

			if (result != null) {
				LOGGER.trace("Dynamic process parameter got value {}", result);
				updateProcParam(jobOrder, dynProcParam.getName(), result);
			}
		});

		/*
		 * Move the main input on the first proc to the first position
		 */
		if (!jobOrder.getProcs().isEmpty()) {
			AbstractJobOrderProc proc = jobOrder.getProcs().get(0);
			JobOrderInput firstInput = proc.getInputs().get(0);
			
			int index = 0;
			for (int i = 0; i < firstInput.getNbFilenames(); i++) {
				if (firstInput.getFilenames().get(i).getFilename().matches(".*" + job.getProductName())) {
					// this product needs to be the first
					index = i;
					break;
				}
			}
			
			// If product isn't already the first, move it there
			if (index > 0) {
				LOGGER.debug("Move main input to first position");
				JobOrderInputFile file = firstInput.getFilenames().get(index);
				JobOrderTimeInterval interval = firstInput.getTimeIntervals().get(index);
				
				firstInput.getFilenames().remove(index);
				firstInput.getTimeIntervals().remove(index);
				
				firstInput.getFilenames().add(0, file);
				firstInput.getTimeIntervals().add(0, interval);
			}
		}
		
		/*
		 * Remove optional outputs from last proc, except for configured additional
		 * outputs
		 */
		if (!jobOrder.getProcs().isEmpty()) {
			AbstractJobOrderProc proc = jobOrder.getProcs().get(jobOrder.getProcs().size() - 1);
			List<String> additionalOutputs = settings
					.getOptionalOutputsForTaskTable(taskTableAdapter.taskTable().getProcessorName());

			proc.setOutputs(proc.getOutputs().stream()
					.filter(output -> output.isMandatory() || additionalOutputs.contains(output.getFileType()))
					.collect(toList()));
		}
	}

	/**
	 * Remove Inputs of ExecutionJob that are referring to a
	 * TaskTableInputAlternative of origin PROC.
	 * 
	 * If those aren't removed, the InputDownloader of the ExecutionWorker will try
	 * to download a file from the OBS but exits with an IllegalArgumentException
	 */
	@Override
	public void customJobDto(AppDataJob job, IpfExecutionJob dto) {
		// Nothing to do currently
	}

	@Override
	public void validateInputSearch(final AppDataJob job, final TaskTableAdapter taskTableAdapter)
			throws IpfPrepWorkerInputsMissingException {
		// Check if timeout is reached -> start job with current input
		if (workerSettings.getWaitprimarycheck().getMaxTimelifeS() != 0) {
			final long startTime = job.getGeneration().getCreationDate().toInstant().toEpochMilli();
			final long timeoutTime = startTime + (workerSettings.getWaitprimarycheck().getMaxTimelifeS() * 1000);

			if (Instant.now().toEpochMilli() > timeoutTime) {
				// Timeout reached
				LOGGER.info("Timeout reached for job {}. Continue without missing products...", job.getId());
				setHasResultsToTrueForTimeoutReached(job);
				return;
			}
		}

		// Extract a list of all inputs from the tasks
		final List<AppDataJobInput> inputsWithNoResults = job.getAdditionalInputs().stream()
				.flatMap(taskInputs -> taskInputs.getInputs().stream().filter(input -> !input.getHasResults()))
				.collect(toList());

		// Create list of alternatives
		final List<TaskTableInputAlternative> alternatives = QueryUtils.alternativesOf(inputsWithNoResults,
				taskTableAdapter, workerSettings.getProductMode());

		// Check if there is an alternative which should have been filled by additional
		// logic
		final Map<String, String> missingAlternatives = new HashMap<>();
		for (final TaskTableInputAlternative alternative : alternatives) {
			if (settings.isMPCSearchActiveForProductType(taskTableAdapter.taskTable().getProcessorName(),
					alternative.getFileType())
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
		final String productType = job.getProductName().substring(4, 15);

		/*
		 * For VISCAL check if there already exists a job, that is responsible for the
		 * given interval
		 */
		if (settings.isRangeSearchActiveForProductType(ttAdapter.taskTable().getProcessorName(), productType)) {
			LOGGER.debug("Look for existing job for productType {} and tasktable {}", productType,
					job.getTaskTableName());
			final Optional<List<AppDataJob>> jobsInDatabase = appCat.findJobsForProductType(productType);

			if (jobsInDatabase.isPresent()) {
				final TimeInterval newJobRange = new TimeInterval(DateUtils.parse(job.getStartTime()),
						DateUtils.parse(job.getStopTime()));

				for (final AppDataJob jobInDatabase : jobsInDatabase.get()) {
					final TimeInterval databaseJobRange = new TimeInterval(
							DateUtils.parse(jobInDatabase.getStartTime()),
							DateUtils.parse(jobInDatabase.getStopTime()));

					if (jobInDatabase.getTaskTableName().equals(job.getTaskTableName())
							&& newJobRange.intersects(databaseJobRange)) {
						return Optional.of(jobInDatabase);
					}
				}
			}
			LOGGER.debug("No existing job for productType {} and tasktable {} found", productType,
					job.getTaskTableName());
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
	private void setHasResultsToTrueForTimeoutReached(final AppDataJob job) {
		for (final AppDataJobTaskInputs task : job.getAdditionalInputs()) {
			for (final AppDataJobInput input : task.getInputs()) {
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
	private TaskTableAdapter getTTAdapterForTaskTableName(final String taskTable) {
		final File ttFile = new File(workerSettings.getDiroftasktables(), taskTable);
		final TaskTableAdapter tasktableAdapter = new TaskTableAdapter(ttFile,
				ttFactory.buildTaskTable(ttFile, processSettings.getLevel()), elementMapper,
				workerSettings.getProductMode());

		return tasktableAdapter;
	}
}
