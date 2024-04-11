/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.preparation.worker.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
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
import esa.s1pdgs.cpoc.preparation.worker.config.PreparationWorkerProperties;
import esa.s1pdgs.cpoc.preparation.worker.config.ProcessProperties;
import esa.s1pdgs.cpoc.preparation.worker.report.TaskTableLookupReportingOutput;
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

	private InputSearchService inputSearchService;

	private final CommonConfigurationProperties commonProperties;

	private PreparationWorkerProperties workerProperties;

	public PreparationWorkerService(final TaskTableMapperService taskTableService, final ProductTypeAdapter typeAdapter,
			final ProcessProperties properties, final AppCatJobService appCat,
			final InputSearchService inputSearchService, final CommonConfigurationProperties commonProperties,
			final PreparationWorkerProperties workerProperties) {
		this.taskTableService = taskTableService;
		this.typeAdapter = typeAdapter;
		this.processProperties = properties;
		this.appCatJobService = appCat;
		this.inputSearchService = inputSearchService;
		this.commonProperties = commonProperties;
		this.workerProperties = workerProperties;
	}

	@Override
	public List<Message<IpfExecutionJob>> apply(CatalogEvent catalogEvent) {
		final Reporting reporting = ReportingUtils
				.newReportingBuilder(MissionId.valueOf((String) catalogEvent.getMetadata().get(MissionId.FIELD_NAME)))
				.rsChainName(commonProperties.getRsChainName()).rsChainVersion(commonProperties.getRsChainVersion())
				.predecessor(catalogEvent.getUid()).newReporting("ProductionTrigger");

		List<IpfExecutionJob> result = new ArrayList<>();

		try {
			// Map event to tasktables
			List<IpfPreparationJob> preparationJobs = taskTableService.mapEventToTaskTables(catalogEvent, reporting);

			// Create new Jobs
			for (IpfPreparationJob preparationJob : preparationJobs) {
				dispatch(preparationJob);
			}

			// Finish reporting
			if (preparationJobs.size() > 0) {
				reporting.end(null, new ReportingMessage("IpfPreparationJobs for product %s created",
						catalogEvent.getProductName()));
			}
		} catch (Exception e) {
			LOGGER.error("Preparation worker failed: {}", LogUtils.toString(e));
			reporting.error(new ReportingMessage("Error on handling CatalogEvent: %s", LogUtils.toString(e)));

			throw new RuntimeException(e);
		}

		try {
			// Find matching jobs
			String triggerString = catalogEvent.getMetadataProductType();
			for (Entry<String, String> entry : workerProperties.getMapTypeMeta().entrySet()) {
				if (triggerString.matches(entry.getValue())) {
					triggerString = entry.getKey();
					break;
				}
			}

			List<AppDataJob> appDataJobs = appCatJobService.findByTriggerProduct(triggerString);

			// Check if jobs are ready
			result = inputSearchService.checkIfJobsAreReady(appDataJobs);
		} catch (Exception e) {
			LOGGER.error("Preparation worker failed: {}", LogUtils.toString(e));
			throw new RuntimeException(e);
		}

		LOGGER.info("End preparation of new execution jobs");

		// Prevent empty array messages on kafka topic
		if (result.isEmpty()) {
			return null;
		} else {
			// Wrap each ExecutionJob into a KafkaMessage so they are sent individually to
			// execution workers
			return result.stream().map(job -> MessageBuilder.withPayload(job).build()).collect(Collectors.toList());
		}
	}

	public final List<AppDataJob> dispatch(final IpfPreparationJob preparationJob) throws Exception {

		final List<AppDataJob> jobs = typeAdapter.createAppDataJobs(preparationJob);

		List<AppDataJob> result = new ArrayList<>();
		try {
			if (CollectionUtil.isNotEmpty(jobs)) {
				final AppDataJob firstJob = jobs.get(0);

				final String tasktableFilename = firstJob.getTaskTableName();

				LOGGER.trace("Got TaskTable {}", tasktableFilename);

				result = handleJobs(preparationJob, jobs, tasktableFilename);
			}
		} catch (Exception e) {
			LOGGER.error("Error handling PreparationJob {}: {}", preparationJob.getUid().toString(),
					LogUtils.toString(e));
		}

		return result;
	}

	// This needs to be synchronized to avoid duplicate jobs
	private final synchronized List<AppDataJob> handleJobs(final IpfPreparationJob preparationJob,
			final List<AppDataJob> jobsFromMessage, final String tasktableFilename) throws AbstractCodedException {
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
					job.setReportingId(preparationJob.getUid());
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
