package esa.s1pdgs.cpoc.preparation.worker.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.preparation.worker.config.PreparationWorkerProperties;
import esa.s1pdgs.cpoc.preparation.worker.config.ProcessProperties;
import esa.s1pdgs.cpoc.preparation.worker.type.ProductTypeAdapter;

public class HousekeepingService implements Supplier<List<Message<IpfExecutionJob>>> {

	static final Logger LOGGER = LogManager.getLogger(HousekeepingService.class);

	private TaskTableMapperService taskTableService;

	private ProductTypeAdapter typeAdapter;

	private ProcessProperties processProperties;

	private AppCatJobService appCatJobService;

	private InputSearchService inputSearchService;

	private final PreparationWorkerProperties prepProperties;

	private final CommonConfigurationProperties commonProperties;

	public HousekeepingService(final TaskTableMapperService taskTableService, final ProductTypeAdapter typeAdapter,
			final ProcessProperties properties, final AppCatJobService appCat,
			final InputSearchService inputSearchService, final PreparationWorkerProperties prepProperties,
			CommonConfigurationProperties commonProperties) {
		this.taskTableService = taskTableService;
		this.typeAdapter = typeAdapter;
		this.processProperties = properties;
		this.appCatJobService = appCat;
		this.inputSearchService = inputSearchService;
		this.prepProperties = prepProperties;
		this.commonProperties = commonProperties;
	}

	@Override
	public List<Message<IpfExecutionJob>> get() {
		// - Delete finished AppDataJob that reached a maximum keeping time
		deleteOldFinishedJobs();

		// - Continue jobs, that ran into an timeout and did not receive all products
		return continueTimeoutJobs();
	}

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
			// Wrap each ExecutionJob into a KafkaMessage so they are sent individually to
			// execution workers
			return result.stream().map(job -> MessageBuilder.withPayload(job).build()).collect(Collectors.toList());
		}
	}

}
