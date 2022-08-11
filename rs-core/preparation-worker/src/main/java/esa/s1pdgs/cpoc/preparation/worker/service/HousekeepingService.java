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
import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.preparation.worker.config.ProcessProperties;
import esa.s1pdgs.cpoc.preparation.worker.type.ProductTypeAdapter;

public class HousekeepingService implements Supplier<List<Message<IpfExecutionJob>>> {

	static final Logger LOGGER = LogManager.getLogger(HousekeepingService.class);

	private TaskTableMapperService taskTableService;

	private ProductTypeAdapter typeAdapter;

	private ProcessProperties processProperties;

	private AppCatJobService appCatJobService;

	private InputSearchService inputSearchService;

	private final CommonConfigurationProperties commonProperties;

	public HousekeepingService(final TaskTableMapperService taskTableService, final ProductTypeAdapter typeAdapter,
			final ProcessProperties properties, final AppCatJobService appCat,
			final InputSearchService inputSearchService, final CommonConfigurationProperties commonProperties) {
		this.taskTableService = taskTableService;
		this.typeAdapter = typeAdapter;
		this.processProperties = properties;
		this.appCatJobService = appCat;
		this.inputSearchService = inputSearchService;
		this.commonProperties = commonProperties;
	}

	@Override
	public List<Message<IpfExecutionJob>> get() {
		// Stuff todo:
		// - Check if any currently pending AppDataJobs are still waiting for input,
		// even though they reached a configured timeout
		// - Delete finished AppDataJob that reached a maximum keeping time

		// Calculate Timeout-Date based on configuration
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
