package esa.s1pdgs.cpoc.preparation.worker.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.preparation.worker.config.ProcessProperties;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.AppCatJobUpdateFailed;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.AppCatalogJobNotFoundException;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.DiscardedException;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.JobStateTransistionFailed;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.TimedOutException;
import esa.s1pdgs.cpoc.preparation.worker.model.generator.ThrowingRunnable;
import esa.s1pdgs.cpoc.preparation.worker.query.AuxQuery;
import esa.s1pdgs.cpoc.preparation.worker.query.AuxQueryHandler;
import esa.s1pdgs.cpoc.preparation.worker.report.TaskTableLookupReportingOutput;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class PreparationWorkerService implements Function<CatalogEvent, List<IpfExecutionJob>> {

	static final Logger LOGGER = LogManager.getLogger(PreparationWorkerService.class);

	private TaskTableMapperService taskTableService;

	private ProductTypeAdapter typeAdapter;

	private ProcessProperties processProperties;

	private AppCatJobService appCatJobService;

	private Map<String, TaskTableAdapter> taskTableAdapters;

	private AuxQueryHandler auxQueryHandler;

	public PreparationWorkerService(final TaskTableMapperService taskTableService, final ProductTypeAdapter typeAdapter,
			final ProcessProperties properties, final AppCatJobService appCat,
			final Map<String, TaskTableAdapter> taskTableAdapters, final AuxQueryHandler auxQueryHandler) {
		this.taskTableService = taskTableService;
		this.typeAdapter = typeAdapter;
		this.processProperties = properties;
		this.appCatJobService = appCat;
		this.taskTableAdapters = taskTableAdapters;
		this.auxQueryHandler = auxQueryHandler;
	}

	@Override
	public List<IpfExecutionJob> apply(CatalogEvent catalogEvent) {
		final Reporting reporting = ReportingUtils
				.newReportingBuilder(MissionId.fromFileName(catalogEvent.getKeyObjectStorage()))
				.predecessor(catalogEvent.getUid()).newReporting("PreparationWorkerService");

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
			List<AppDataJob> appDataJobs = appCatJobService.findByTriggerProduct(catalogEvent.getProductType());

			// Check if jobs are ready
			result = checkIfJobsAreReady(appDataJobs);

		} catch (Exception e) {
			reporting.error(new ReportingMessage("Preparation worker failed: %s", LogUtils.toString(e)));
			throw new RuntimeException(e);
		}

		reporting.end(null, new ReportingMessage("End preparation of new execution jobs"));

		return result;
	}

	public final List<AppDataJob> dispatch(final IpfPreparationJob preparationJob) throws Exception {

		MissionId mission = MissionId
				.valueOf((String) preparationJob.getCatalogEvent().getMetadata().get(MissionId.FIELD_NAME));

		final Reporting reporting = ReportingUtils.newReportingBuilder(mission).predecessor(preparationJob.getUid())
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

		for (AppDataJob job : appDataJobs) {
			if (job.getGeneration().getState() == AppDataJobGenerationState.INITIAL) {
				try {
					LOGGER.info("Start main input search for AppDataJob {}", job.getId());
					job = mainInputSearch(job, taskTableAdapters.get(job.getTaskTableName()));
				} catch (JobStateTransistionFailed e) {
					LOGGER.info("Main input search did not complete successfully: ", e.getMessage());
				}
			}

			if (job.getGeneration().getState() == AppDataJobGenerationState.PRIMARY_CHECK) {
				try {
					LOGGER.info("Start aux input search for AppDataJob {}", job.getId());
					job = auxInputSearch(job, taskTableAdapters.get(job.getTaskTableName()));
				} catch (JobStateTransistionFailed e) {
					LOGGER.info("Aux input search did not complete successfully: ", e.getMessage());
				}
			}

			if (job.getGeneration().getState() == AppDataJobGenerationState.READY) {
//				send();
			}

			if (job.getGeneration().getState() == AppDataJobGenerationState.SENT) {
//				terminate();
			}

			// Update Job in Mongo
			try {
				appCatJobService.updateJob(job);
			} catch (AppCatalogJobNotFoundException e) {
				LOGGER.error("Error while saving new state of AppDataJob {}: {}", job.getId(), e.getMessage());
			}
		}

		return new ArrayList<>();
	}

	public AppDataJob mainInputSearch(AppDataJob job, TaskTableAdapter taskTableAdapter)
			throws JobStateTransistionFailed {
		AppDataJobGenerationState newState = job.getGeneration().getState();

		final AtomicBoolean timeout = new AtomicBoolean(false);
		Product queried = null;
		try {
			queried = perform(() -> typeAdapter.mainInputSearch(job, taskTableAdapter),
					"querying input " + job.getProductName());
			job.setProduct(queried.toProduct());
			job.setAdditionalInputs(queried.overridingInputs());
			// FIXME dirty workaround warning, the product above is still altered in
			// validate by modifying
			// the start stop time for segments
			performVoid(() -> {
				try {
					typeAdapter.validateInputSearch(job, taskTableAdapter);
				} catch (final TimedOutException e) {
					timeout.set(true);
				}
			}, "validating availability of input products for " + job.getProductName());
			newState = AppDataJobGenerationState.PRIMARY_CHECK;
		} finally {
			updateJobMainInputSearch(job, queried, newState);
		}

		return job;
	}

	public AppDataJob auxInputSearch(AppDataJob job, TaskTableAdapter taskTableAdapter)
			throws JobStateTransistionFailed {
		AppDataJobGenerationState newState = job.getGeneration().getState();
		final AuxQuery auxQuery = auxQueryHandler.queryFor(job, taskTableAdapter);

		List<AppDataJobTaskInputs> queried = Collections.emptyList();

		try {
			queried = perform(() -> auxQuery.queryAux(), "querying required AUX");
			job.setAdditionalInputs(queried);
			performVoid(() -> auxQuery.validate(job), "validating availability of AUX for " + job.getProductName());
			newState = AppDataJobGenerationState.READY;
		} finally {
			updateJobAuxSearch(job, queried, newState);
		}

		return job;
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

	private final void performVoid(final ThrowingRunnable command, final String name) throws JobStateTransistionFailed {
		perform(() -> {
			command.run();
			return null;
		}, name);
	}

	private final <E> E perform(final Callable<E> command, final String name) throws JobStateTransistionFailed {
		try {
			return command.call();
		}
		// expected on failed transition
		catch (final IpfPrepWorkerInputsMissingException e) {
			// TODO once there is some time for refactoring, cleanup the created error
			// message of
			// IpfPrepWorkerInputsMissingException to be more descriptive
			throw new JobStateTransistionFailed(e.getLogMessage());
		}
		// expected on updating AppDataJob in persistence -> simply retry next time
		catch (final AppCatJobUpdateFailed e) {
			throw new JobStateTransistionFailed(
					String.format("Error on persisting change of '%s': %s", name, Exceptions.messageOf(e)), e);
		}
		// expected on discard scenarios -> terminate job
		catch (final DiscardedException e) {
			throw e;
		} catch (final Exception e) {
			throw new RuntimeException(String.format("Fatal error on %s: %s", name, Exceptions.messageOf(e)), e);
		}
	}
	
	private void updateJobMainInputSearch(AppDataJob job, Product queried, AppDataJobGenerationState newState) {
		if (queried != null) {
			final AppDataJobProduct prod = queried.toProduct();
			job.setProduct(prod);						
			job.setAdditionalInputs(queried.overridingInputs());
			job.setPreselectedInputs(queried.preselectedInputs());
			
			// dirty workaround for segment and session scenario
			final AppDataJobProductAdapter productAdapter = new AppDataJobProductAdapter(prod);
			job.setStartTime(productAdapter.getStartTime());
			job.setStopTime(productAdapter.getStopTime());
		}
		
		// Before updating the state -> save last state
		job.getGeneration().setPreviousState(job.getGeneration().getState());
		
		// no transition?
		if (job.getGeneration().getState() == newState) {
			// don't update jobs last modified date here to enable timeout, just update the generations 
			// last update time
			job.getGeneration().setLastUpdateDate(new Date());		
			job.getGeneration().setNbErrors(job.getGeneration().getNbErrors()+1);
		}
		else {
			job.getGeneration().setState(newState);
			job.setLastUpdateDate(new Date());
		}
	}
	
	private void updateJobAuxSearch(AppDataJob job, List<AppDataJobTaskInputs> queried, AppDataJobGenerationState newState) {
		if (!queried.isEmpty()) {
			job.setAdditionalInputs(queried);	
		}
		
		// Before updating the state -> save last state
		job.getGeneration().setPreviousState(job.getGeneration().getState());
		
		// no transition?
		if (job.getGeneration().getState() == newState) {
			// don't update jobs last modified date here to enable timeout, just update the generation time
			job.getGeneration().setLastUpdateDate(new Date());
			job.getGeneration().setNbErrors(job.getGeneration().getNbErrors()+1);
		}
		else {
			job.getGeneration().setState(newState);
			job.setLastUpdateDate(new Date());
		}
	}

}
