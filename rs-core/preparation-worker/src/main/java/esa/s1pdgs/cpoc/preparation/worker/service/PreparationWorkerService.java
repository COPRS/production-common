package esa.s1pdgs.cpoc.preparation.worker.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.preparation.worker.config.ProcessProperties;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.AppCatalogJobNotFoundException;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.JobStateTransistionFailed;
import esa.s1pdgs.cpoc.preparation.worker.report.TaskTableLookupReportingOutput;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class PreparationWorkerService implements Function<CatalogEvent, List<Message<IpfExecutionJob>>> {

	static final Logger LOGGER = LogManager.getLogger(PreparationWorkerService.class);
	
	private TaskTableMapperService taskTableService;

	private ProductTypeAdapter typeAdapter;

	private ProcessProperties processProperties;

	private AppCatJobService appCatJobService;

	private Map<String, TaskTableAdapter> taskTableAdapters;

	private InputSearchService inputSearchService;

	private JobCreationService jobCreationService;
	
	private final CommonConfigurationProperties commonProperties;

	public PreparationWorkerService(final TaskTableMapperService taskTableService, final ProductTypeAdapter typeAdapter,
			final ProcessProperties properties, final AppCatJobService appCat,
			final Map<String, TaskTableAdapter> taskTableAdapters, final InputSearchService inputSearchService,
			final JobCreationService jobCreationService, final CommonConfigurationProperties commonProperties) {
		this.taskTableService = taskTableService;
		this.typeAdapter = typeAdapter;
		this.processProperties = properties;
		this.appCatJobService = appCat;
		this.taskTableAdapters = taskTableAdapters;
		this.inputSearchService = inputSearchService;
		this.jobCreationService = jobCreationService;
		this.commonProperties = commonProperties;
	}

	@Override
	public List<Message<IpfExecutionJob>> apply(CatalogEvent catalogEvent) {
		final Reporting reporting = ReportingUtils
				.newReportingBuilder(MissionId
						.valueOf((String) catalogEvent.getMetadata().get(MissionId.FIELD_NAME)))
				.rsChainName(commonProperties.getRsChainName())
				.rsChainVersion(commonProperties.getRsChainVersion())
				.predecessor(catalogEvent.getUid())
				.newReporting("PreparationWorkerService");

		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(catalogEvent.getProductFamily(),
						catalogEvent.getProductName()),
				new ReportingMessage("Check if any jobs can be finalized for the IPF"));

		List<IpfExecutionJob> result = new ArrayList<>();

		try {
			// Map event to tasktables
			List<IpfPreparationJob> preparationJobs = taskTableService.mapEventToTaskTables(catalogEvent, reporting);

			// Create new Jobs
			for (IpfPreparationJob preparationJob : preparationJobs) {
				dispatch(preparationJob);
			}

			// Find matching jobs
			List<AppDataJob> appDataJobs = appCatJobService.findByTriggerProduct(catalogEvent.getMetadataProductType());

			// Check if jobs are ready
			result = checkIfJobsAreReady(appDataJobs);

		} catch (Exception e) {
			reporting.error(new ReportingMessage("Preparation worker failed: %s", LogUtils.toString(e)));
			throw new RuntimeException(e);
		}

		reporting.end(null, new ReportingMessage("End preparation of new execution jobs"));

		// Prevent empty array messages on kafka topic
		if (result.isEmpty()) {
			return null;
		} else {
			// Wrap each ExecutionJob into a KafkaMessage so they are sent individually to execution workers
			return result.stream().map(job -> MessageBuilder.withPayload(job).build()).collect(Collectors.toList());
		}
	}

	public final List<AppDataJob> dispatch(final IpfPreparationJob preparationJob) throws Exception {

		MissionId mission = MissionId
				.valueOf((String) preparationJob.getCatalogEvent().getMetadata().get(MissionId.FIELD_NAME));

		final Reporting reporting = ReportingUtils.newReportingBuilder(mission)
				.rsChainName(commonProperties.getRsChainName())
				.rsChainVersion(commonProperties.getRsChainVersion())
				.predecessor(preparationJob.getUid())
				.newReporting("TaskTableLookup");

		final List<AppDataJob> jobs = typeAdapter.createAppDataJobs(preparationJob);

		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(preparationJob.getProductFamily(),
						preparationJob.getKeyObjectStorage()),
				new ReportingMessage("Start associating TaskTables to created AppDataJobs"));

		List<AppDataJob> result = new ArrayList<>();
		try {
			if (CollectionUtil.isNotEmpty(jobs)) {
				final AppDataJob firstJob = jobs.get(0);

				final String tasktableFilename = firstJob.getTaskTableName();

				LOGGER.trace("Got TaskTable {}", tasktableFilename);

				result = handleJobs(preparationJob, jobs, reporting.getUid(), tasktableFilename);
			}
		} catch (Exception e) {
			LOGGER.error("Error handling PreparationJob {}: {}", preparationJob.getUid().toString(),
					LogUtils.toString(e));
			reporting.error(
					new ReportingMessage("Error associating TaskTables to AppDataJobs: %s", LogUtils.toString(e)));
		}

		reporting.end(new TaskTableLookupReportingOutput(Collections.singletonList(preparationJob.getTaskTableName())),
				new ReportingMessage("End associating TaskTables to AppDataJobs"));

		return result;
	}

	// This needs to be synchronized to avoid duplicate jobs
	private final synchronized List<AppDataJob> handleJobs(final IpfPreparationJob preparationJob,
			final List<AppDataJob> jobsFromMessage, final UUID reportingUid, final String tasktableFilename)
			throws AbstractCodedException {
		final AppDataJob firstJob = jobsFromMessage.get(0);

		final CatalogEvent firstEvent = firstJob.getCatalogEvents().get(0);
		final List<AppDataJob> jobForMess = appCatJobService.findByCatalogEventsUid(firstEvent.getUid());

		List<AppDataJob> dispatchedJobs = new ArrayList<>();

		// there is already a job for this message --> possible restart scenario -->
		// just update the pod name
		if (!jobForMess.isEmpty() && getJobMatchingTasktable(jobForMess, tasktableFilename) != null) {
			final AppDataJob job = getJobMatchingTasktable(jobForMess, tasktableFilename);
			LOGGER.warn("Found job {} already associated to catalogEvent {}. Ignoring new message ...", job.getId(),
					firstEvent.getUid());
		} else {
			// no job yet associated to this message --> check special cases otherwise
			// create and persist
			final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(firstEvent);
			for (final AppDataJob job : jobsFromMessage) {
				final Optional<AppDataJob> specificJob = typeAdapter.findAssociatedJobFor(appCatJobService,
						eventAdapter, job);

				if (specificJob.isPresent()) {
					final AppDataJob existingJob = specificJob.get();
					LOGGER.info("Found job {} already being handled. Appending new event {} ...", existingJob.getId(),
							firstEvent.getUid());
					appCatJobService.appendCatalogEvent(existingJob.getId(), firstEvent);
				} else {
					LOGGER.debug("Persisting new job for preparation job {} (catalog event {}) ...",
							preparationJob.getUid(), firstEvent.getUid());
					final Date now = new Date();
					final AppDataJobGeneration gen = new AppDataJobGeneration();
					gen.setState(AppDataJobGenerationState.INITIAL);
					gen.setTaskTable(tasktableFilename);
					gen.setNbErrors(0);
					gen.setCreationDate(now);
					gen.setLastUpdateDate(now);

					job.setGeneration(gen);
					job.setPrepJob(preparationJob);
					job.setReportingId(reportingUid);
					job.setState(AppDataJobState.GENERATING); // will activate that this request can be polled
					job.setPod(processProperties.getHostname());

					LOGGER.info("Try to save new job in MongoDB...");
					final AppDataJob newlyCreatedJob = appCatJobService.newJob(job);
					LOGGER.info("dispatched job {}", newlyCreatedJob.getId());
					dispatchedJobs.add(newlyCreatedJob);
				}
			}
		}

		return dispatchedJobs;
	}

	public synchronized List<IpfExecutionJob> checkIfJobsAreReady(List<AppDataJob> appDataJobs) {
		
		List<IpfExecutionJob> executionJobs = new ArrayList<>();
		
		for (AppDataJob job : appDataJobs) {
			// Continue to process jobs, when one results in an unexpected error
			try {
				if (job.getGeneration().getState() == AppDataJobGenerationState.INITIAL) {
					try {
						LOGGER.info("Start main input search for AppDataJob {}", job.getId());
						job = inputSearchService.mainInputSearch(job, taskTableAdapters.get(job.getTaskTableName()));
					} catch (JobStateTransistionFailed e) {
						LOGGER.info("Main input search did not complete successfully: {}", e.getMessage());
					}
				}
	
				if (job.getGeneration().getState() == AppDataJobGenerationState.PRIMARY_CHECK) {
					try {
						LOGGER.info("Start aux input search for AppDataJob {}", job.getId());
						job = inputSearchService.auxInputSearch(job, taskTableAdapters.get(job.getTaskTableName()));
					} catch (JobStateTransistionFailed e) {
						LOGGER.info("Aux input search did not complete successfully: {}", e.getMessage());
					}
				}
	
				if (job.getGeneration().getState() == AppDataJobGenerationState.READY) {
					try {
						LOGGER.info("Start generating IpfExecutionJob for AppDataJob {}", job.getId());
						
						IpfExecutionJob executionJob = jobCreationService.createExecutionJob(job, taskTableAdapters.get(job.getTaskTableName()));
						if (executionJob != null) {
							executionJobs.add(executionJob);
						} else {
							// TODO: Improve Error Handling
							LOGGER.error("Could not generate ExecutionJob for AppDataJob {}", job.getId());
						}
					} catch (JobStateTransistionFailed e) {
						LOGGER.info("Generation of IpfExecutionJob did not complete successfully: {}", e.getMessage());
					}
				}
	
				if (job.getGeneration().getState() == AppDataJobGenerationState.SENT) {
					// TODO: This step was mandatory before to make sure no duplicate jobs are created. Is this still necessary?
	//				terminate();
				}
	
				// Update Job in Mongo
				appCatJobService.updateJob(job);
			} catch (AppCatalogJobNotFoundException e) {
				LOGGER.error("Error while saving new state of AppDataJob {}: {}", job.getId(), e.getMessage());
			} catch (Exception e) {
				LOGGER.error("An unexpected exception occured while processing AppDataJob {}: {}", job.getId(), e.getMessage());
			}
		}

		return executionJobs;
	}

	/**
	 * Returns the job of the list with the matching tasktable name. Returns null if
	 * no matching job was found
	 */
	private AppDataJob getJobMatchingTasktable(final List<AppDataJob> jobs, final String taskTableName) {
		for (final AppDataJob job : jobs) {
			if (job.getTaskTableName().equals(taskTableName)) {
				return job;
			}
		}
		return null;
	}
}
