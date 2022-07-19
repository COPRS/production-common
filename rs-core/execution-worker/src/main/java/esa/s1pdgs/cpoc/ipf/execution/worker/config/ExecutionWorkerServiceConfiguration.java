package esa.s1pdgs.cpoc.ipf.execution.worker.config;

import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.ipf.execution.worker.service.ExecutionWorkerService;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

@Configuration
public class ExecutionWorkerServiceConfiguration {
	
	@Autowired
	private CommonConfigurationProperties commonProperties;
	
	@Autowired
	private AppStatus appStatus;
	
	@Autowired
	private ApplicationProperties applicationProperties;
	
	@Autowired
	private DevProperties devProperties;
	
	@Autowired
	private ObsClient obsClient;
	
	@Bean
	public Function<IpfExecutionJob, List<Message<CatalogJob>>> executeJob() {
		return new ExecutionWorkerService(commonProperties, appStatus, applicationProperties, devProperties, obsClient);
	}
}
