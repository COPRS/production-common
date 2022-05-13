package esa.s1pdgs.cpoc.evictionmanagement.worker.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.evictionmanagement.worker.service.EvictionManagementService;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

@ConditionalOnProperty(value = "scheduling.enable", havingValue="true", matchIfMissing = true)
@EnableScheduling
@Configuration
public class SchedulingConfiguration {
	
	@Autowired
	private DataLifecycleMetadataRepository metadataRepo;
	
	@Autowired
	private ObsClient obsClient;
	
	@Bean
	public EvictionManagementService evictionManagement() {
		return new EvictionManagementService(metadataRepo, obsClient);
		
	}
	

}
