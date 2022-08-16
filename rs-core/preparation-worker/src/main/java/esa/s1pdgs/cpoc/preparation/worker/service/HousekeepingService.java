package esa.s1pdgs.cpoc.preparation.worker.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.preparation.worker.config.PreparationWorkerProperties;

public class HousekeepingService implements Function<Message<?>, List<Message<IpfExecutionJob>>> {

	static final Logger LOGGER = LogManager.getLogger(HousekeepingService.class);

	private AppCatJobService appCatJobService;

	private InputSearchService inputSearchService;

	private final PreparationWorkerProperties prepProperties;

	private final CommonConfigurationProperties commonProperties;

	public HousekeepingService(final AppCatJobService appCat, final InputSearchService inputSearchService,
			final PreparationWorkerProperties prepProperties, CommonConfigurationProperties commonProperties) {
		this.appCatJobService = appCat;
		this.inputSearchService = inputSearchService;
		this.prepProperties = prepProperties;
		this.commonProperties = commonProperties;
	}

	/**
	 * The responsibility of the HousekeepingService is to make sure, no pending
	 * jobs remain in the system, by forcing a progress when a given job reaches its
	 * timeout. Secondly it also deletes jobs, that already finished and are not
	 * necessary to be kept in the system to prevent duplicated jobs (and products).
	 * Each HousekeepingService is responsible for a PreparationWorkerService and is
	 * provided as a SCDF Source Application.
	 */
	@Override
	public List<Message<IpfExecutionJob>> apply(Message<?> triggerMessage) {
		LOGGER.debug("Start deleting old jobs");

		// Delete finished AppDataJob that reached a maximum keeping time
		deleteOldFinishedJobs();

		LOGGER.debug("Start searching for timeout jobs");
		// Continue jobs, that ran into an timeout and did not receive all products
		return continueTimeoutJobs();
	}

	/**
	 * Find and delete jobs in each state, that reached a configured job age. Makes
	 * sure the database is kept small and no unnecessary jobs are kept.
	 */
	private void deleteOldFinishedJobs() {
		// Calculate time, based on configuration
		cleanJobsByState(AppDataJobState.DISPATCHING);
		cleanJobsByState(AppDataJobState.GENERATING);
		cleanJobsByState(AppDataJobState.WAITING);
		cleanJobsByState(AppDataJobState.TERMINATED);
	}

	private void cleanJobsByState(final AppDataJobState state) {
		final long maxAgeMs = prepProperties.getMaxAgeJobMs().get(state.name().toLowerCase());

		final Date oldJobsDate = new Date(System.currentTimeMillis() - maxAgeMs);
		final List<AppDataJob> oldJobs = appCatJobService.findByStateAndLastUpdateDateLessThan(state, oldJobsDate);

		for (final AppDataJob oldJob : oldJobs) {
			final String logMessage = String.format("Remove %s job %s (%s) for enough time", oldJob.getLevel(),
					oldJob.getId(), state);

			LOGGER.info(logMessage);

			appCatJobService.deleteJob(oldJob.getId());
		}
	}

	/**
	 * Find jobs that reached a timeout and therefore have to be continued in order
	 * to make sure as many products are produced as possible. This mechanism is
	 * needed for jobs, that wait for inputs, that the system is not receiving to
	 * prevent pending jobs in the system. The logic whether or not the job is
	 * aborted or continued is contained in the specific TypeAdapter.
	 * 
	 * @return List of ExecutionJobs, that finished during the timeout process.
	 */
	private List<Message<IpfExecutionJob>> continueTimeoutJobs() {
		List<AppDataJob> timedoutJobs = appCatJobService.findTimeoutJobs(new Date());
		List<IpfExecutionJob> result = new ArrayList<>();

		// Flag jobs, that they are timed out
		for (AppDataJob timedoutJob : timedoutJobs) {
			timedoutJob.setTimedOut(true);
		}

		result = inputSearchService.checkIfJobsAreReady(timedoutJobs);

		// Prevent empty array messages on kafka topic
		if (result.isEmpty()) {
			return null;
		} else {
			return result.stream().map(message -> MessageBuilder.withPayload(message).build())
					.collect(Collectors.toList());
		}
	}

}
