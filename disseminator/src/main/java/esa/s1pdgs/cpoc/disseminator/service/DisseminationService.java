package esa.s1pdgs.cpoc.disseminator.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.DisseminationTypeConfiguration;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration.Protocol;
import esa.s1pdgs.cpoc.mqi.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

@Service
public class DisseminationService {
	private static final Logger LOG = LogManager.getLogger(DisseminationService.class);
	
	private final GenericMqiClient client;
    private final ObsClient obsClient;
	private final DisseminationProperties properties;
	private ExecutorService service;
   
	@Autowired
	public DisseminationService(
			final GenericMqiClient client,
		    final ObsClient obsClient,
			final DisseminationProperties properties
	) {
		this.client = client;
		this.obsClient = obsClient;
		this.properties = properties;
	}
	
    @PostConstruct
    public void startConsumers() {
        // Init the list of consumers
    	service = Executors.newFixedThreadPool(properties.getCategories().size());
    	
    	for (final Map.Entry<ProductCategory, List<DisseminationTypeConfiguration>> entry : properties.getCategories().entrySet()) {	
    		// start consumer foreach category
    		LOG.debug("Starting consumer for {}", entry);
    		service.execute(new MqiConsumer<ProductDto>(
    				client, 
    				entry.getKey(), 
    				dto -> {      		
    					final ProductDto product = dto.getBody();
    					for (final DisseminationTypeConfiguration config : entry.getValue()) {			
    						if (product.getProductName().matches(config.getRegex())) {				
    							transferTo(product.getFamily(), product.getKeyObjectStorage(), config.getTarget());
    						}			
    					}
    				},    				
    				properties.getPollingIntervalMs()
    		));
    	}
    }
    
	public final void transferTo(final ProductFamily family, String keyObjectStorage, String target) {		
		try {
			if (!obsClient.exist(family, keyObjectStorage)) {
				throw new RuntimeException(String.format("OBS file %s (%s) does not exist", keyObjectStorage, family));
			}
			final OutboxConfiguration config = getOutboxConfigurationFor(target);	
			if (config.getProtocol() == Protocol.FILE) {				
				obsClient.downloadFile(family, keyObjectStorage, config.getPath());
			} else {
				throw new UnsupportedOperationException(String.format("Protocol %s not implemented", config.getProtocol()));
			}		
		} catch (ObsException e) {
			throw new RuntimeException(
					String.format("Error downloading file %s (%s) from OBS: %s", keyObjectStorage, family, LogUtils.toString(e))
			);
		}		
	}
	
	final OutboxConfiguration getOutboxConfigurationFor(final String target) {
		final OutboxConfiguration result = properties.getOutboxes().get(target);
		if (result == null) {
			throw new RuntimeException(
					String.format("Missing outbox configuration %s. Available are: %s", target , properties.getOutboxes().keySet())
			);
		}
		return result;
	}
}

