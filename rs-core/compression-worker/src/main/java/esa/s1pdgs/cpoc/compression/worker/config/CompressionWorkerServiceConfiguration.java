package esa.s1pdgs.cpoc.compression.worker.config;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.compression.worker.service.CompressProcessor;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionEvent;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

@Configuration
public class CompressionWorkerServiceConfiguration {
	
	@Autowired
	private AppStatus appStatus;
	
	@Autowired
	private ApplicationProperties properties;
	
	@Autowired
	private ObsClient obsClient;
	
	@Bean
	public Function<CatalogEvent, Message<CompressionEvent>> compress() {
		return new CompressProcessor(appStatus, properties, obsClient);
	}
	
	

}
