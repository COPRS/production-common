package esa.s1pdgs.cpoc.preparation.worker.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.Message;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.preparation.worker.config.ProcessProperties;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.ProductTypeAdapter;

public class HousekeepingService implements Supplier<List<Message<IpfExecutionJob>>> {

	static final Logger LOGGER = LogManager.getLogger(HousekeepingService.class);
	
	private TaskTableMapperService taskTableService;

	private ProductTypeAdapter typeAdapter;

	private ProcessProperties processProperties;

	private AppCatJobService appCatJobService;

	private Map<String, TaskTableAdapter> taskTableAdapters;

	private InputSearchService inputSearchService;

	private JobCreationService jobCreationService;
	
	private final CommonConfigurationProperties commonProperties;
	
	public HousekeepingService(final TaskTableMapperService taskTableService, final ProductTypeAdapter typeAdapter,
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
	public List<Message<IpfExecutionJob>> get() {
		// Stuff todo:
		//  - Check if any currently pending AppDataJobs are still waiting for input, even though they reached a configured timeout
		//  - Delete finished AppDataJob that reached a maximum keeping time
		
		
		// Calculate Timeout-Date based on configuration
		List<AppDataJob> timeoutJobs = appCatJobService.findTimeoutJobs(new Date());	
		
		return null;
	}
}
