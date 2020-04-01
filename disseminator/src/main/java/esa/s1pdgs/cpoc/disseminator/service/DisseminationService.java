package esa.s1pdgs.cpoc.disseminator.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.disseminator.DisseminationTriggerListener;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.DisseminationTypeConfiguration;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

@Service
public class DisseminationService {	
	private static final Logger LOG = LogManager.getLogger(DisseminationService.class);
	
	private final GenericMqiClient client;
    private final ObsClient obsClient;
	private final DisseminationProperties properties;
	private final ErrorRepoAppender errorAppender;
	private final AppStatus appStatus;
	
	@Autowired
	public DisseminationService(
			final GenericMqiClient client,
		    final ObsClient obsClient,
			final DisseminationProperties properties,
			final ErrorRepoAppender errorAppender,
			final AppStatus appStatus
	) {
		this.client = client;
		this.obsClient = obsClient;
		this.properties = properties;
		this.errorAppender = errorAppender;
		this.appStatus = appStatus;
	}
	
    @PostConstruct
    public void initService() {
        // Init the list of consumers and start them
    	final ExecutorService service = Executors.newFixedThreadPool(properties.getCategories().size());
    	
    	DisseminationTriggerListener disseminationTriggerListenerForProductionEvents = newDisseminationTriggerListenerFor(ProductCategory.PRODUCTION_EVENT);
    	
    	for (final Map.Entry<ProductCategory, List<DisseminationTypeConfiguration>> entry : properties.getCategories().entrySet()) {	
    		// start consumer for each category
    		LOG.debug("Starting consumer for {}", entry);
    		service.execute(new MqiConsumer<ProductionEvent>(
    				client, 
    				entry.getKey(),
    				disseminationTriggerListenerForProductionEvents, 
    				properties.getPollingIntervalMs(),
    				0L,
    				appStatus    				
    		));
    	}
    }

    final List<DisseminationTypeConfiguration> configsFor(final ProductFamily family) {
    	return properties.getCategories().getOrDefault(ProductCategory.of(family), Collections.emptyList());	
    }

	private final DisseminationTriggerListener newDisseminationTriggerListenerFor(final ProductCategory cat) {
		LOG.debug("Creating DisseminationTriggerListener for category {}", cat);
		if (cat == ProductCategory.PRODUCTION_EVENT) {
			return new DisseminationTriggerListener(
					obsClient,
					properties,
					errorAppender
			);
		}
		throw new IllegalArgumentException(
				String.format(
						"Invalid product category %s. Available are %s", 
						cat, 
						Arrays.asList(ProductCategory.PRODUCTION_EVENT)
				)
		);
	}
}

