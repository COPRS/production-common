package esa.s1pdgs.cpoc.disseminator;

import java.io.InputStream;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import esa.s1pdgs.cpoc.common.errors.obs.ObsUnknownObject;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.DisseminationTypeConfiguration;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration.Protocol;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;

@Service
public class DisseminationService {	
	private static final Logger LOG = LogManager.getLogger(DisseminationService.class);
	
	private static Set<Protocol> REMOTE_PROTOCOLS = EnumSet.of(Protocol.FTPS, Protocol.SFTP);
	
	private final GenericMqiClient client;
    private final ObsClient obsClient;
	private final DisseminationProperties properties;
	private final ErrorRepoAppender errorAppender;
	
	private ExecutorService service;
   
	@Autowired
	public DisseminationService(
			final GenericMqiClient client,
		    final ObsClient obsClient,
			final DisseminationProperties properties,
			final ErrorRepoAppender errorAppender
	) {
		this.client = client;
		this.obsClient = obsClient;
		this.properties = properties;
		this.errorAppender = errorAppender;
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
    							final Reporting.Factory rf = new LoggerReporting.Factory(LOG, "Dissemination");
    							final Reporting reporting = rf.product(product.getFamily().toString(), product.getProductName())
    									.newReporting(0);
    							reporting.reportStart("Start dissemination of product to outbox " + config.getTarget());
    							try {
									transferTo(product.getFamily(), product.getKeyObjectStorage(), config.getTarget(), rf);								
    							} catch (Exception e) {
    								final String errMessage = (e instanceof DisseminationException) ? e.getMessage() : LogUtils.toString(e); 
    								final String message = String.format(
    										"Error on dissemination of product to outbox %s: %s", 
    										config.getTarget(), 
    										errMessage
    								);
									reporting.reportError(message);									
									errorAppender.send(new FailedProcessingDto(
											properties.getHostname(), 
											new Date(), 
											message, 
											dto
									));									
									throw new RuntimeException(message, e);
								} 
    							reporting.reportStop("End dissemination of product to outbox " + config.getTarget());    							
    						}			
    					}
    				},    				
    				properties.getPollingIntervalMs()
    		));
    	}
    }
    
	final void transferTo(final ProductFamily family, String keyObjectStorage, String target, final Reporting.Factory reportingFactory) 
			throws DisseminationException {		
		try {
			if (!obsClient.exist(family, keyObjectStorage)) {
				throw new RuntimeException(String.format("OBS file %s (%s) does not exist", keyObjectStorage, family));
			}			
			final Reporting reporting = reportingFactory.newReporting(1);
			reporting.reportStart("Start downloading file from OBS " + keyObjectStorage + " to " + target);
			try {					
				transferToOutbox(family, keyObjectStorage, getOutboxConfigurationFor(target));	
				reporting.reportStop("End downloading file from OBS " + keyObjectStorage + " to " + target);
			} catch (SdkClientException | ObsException e) {
				reporting.reportError("Error downloading file from OBS {} to {}: {} ", keyObjectStorage, target, LogUtils.toString(e));
				throw e;
			}	
		} catch (SdkClientException | ObsException e) {
			throw new DisseminationException(
					String.format("Error downloading file %s (%s) from OBS: %s", keyObjectStorage, family, LogUtils.toString(e))
			);
		}		
	}
	
	final void transferToOutbox(final ProductFamily family, final String keyObjectStorage, final OutboxConfiguration config)
			throws ObsException, ObsUnknownObject, SdkClientException {
		if (config.getProtocol() == Protocol.FILE) {
			obsClient.downloadFile(family, keyObjectStorage, config.getPath());
		} else if (REMOTE_PROTOCOLS.contains(config.getProtocol())) {				
			transferToRemoteOutbox(
					obsClient.getAllAsInputStream(family, keyObjectStorage), 
					keyObjectStorage, 
					config
			);
		} else {
			throw new UnsupportedOperationException(String.format("Protocol %s not implemented", config.getProtocol()));
		}
	}
	
	final void transferToRemoteOutbox(final Map<String, InputStream> obsContent, final String keyObjectStorage, final OutboxConfiguration config) {
		// TODO
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

