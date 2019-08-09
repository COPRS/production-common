package esa.s1pdgs.cpoc.disseminator;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.DisseminationTypeConfiguration;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration.Protocol;
import esa.s1pdgs.cpoc.disseminator.outbox.FtpsOutboxClient;
import esa.s1pdgs.cpoc.disseminator.outbox.LocalOutboxClient;
import esa.s1pdgs.cpoc.disseminator.outbox.OutboxClient;
import esa.s1pdgs.cpoc.disseminator.outbox.SftpOutboxClient;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;

public class DisseminationService {	
	private static final Logger LOG = LogManager.getLogger(DisseminationService.class);
	
	// list of all outbox client factories for protocols. For newly implemented protocols, add factory here
	private static final Map<Protocol,OutboxClient.Factory> FACTORIES = new HashMap<>();
	static {
		FACTORIES.put(Protocol.FILE, new LocalOutboxClient.Factory());
		FACTORIES.put(Protocol.FTPS, new FtpsOutboxClient.Factory());
		FACTORIES.put(Protocol.SFTP, new SftpOutboxClient.Factory());
	}
	
	private final GenericMqiClient client;
    private final ObsClient obsClient;
	private final DisseminationProperties properties;
	private final ErrorRepoAppender errorAppender;
	private final Map<String,OutboxClient> clientForTargets;
   
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
		this.clientForTargets = new HashMap<>();
	}
	
    @PostConstruct
    public void startConsumers() {    	
    	// Init list of configured outboxes    	
    	for (final Map.Entry<String, OutboxConfiguration> entry : properties.getOutboxes().entrySet()) {	
    		final String target = entry.getKey();
    		final OutboxConfiguration config = entry.getValue();    		
    		final OutboxClient.Factory factory = FACTORIES.get(config.getProtocol());
    		if (factory == null) {
    			throw new RuntimeException(String.format("No OutboxClient.Factory exists for protocol %s", config.getProtocol()));
    		}    	
    		final OutboxClient outboxClient = factory.newClient(obsClient, config);    		
    		LOG.debug("Defining {} for target {}", outboxClient, target);
    		clientForTargets.put(target, outboxClient);
    	}    	
    	
        // Init the list of consumers and start them
    	final ExecutorService service = Executors.newFixedThreadPool(properties.getCategories().size());
    	
    	for (final Map.Entry<ProductCategory, List<DisseminationTypeConfiguration>> entry : properties.getCategories().entrySet()) {	
    		// start consumer for each category
    		LOG.debug("Starting consumer for {}", entry);
    		service.execute(new MqiConsumer<ProductDto>(
    				client, 
    				entry.getKey(), 
    				newMqiListener(entry.getValue()),    				
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
			final OutboxClient outboxClient = clientForTargets.get(target);
			if (outboxClient == null) {
				throw new RuntimeException(String.format("No outbox configured for '%s'. Available are: %s", target, 
						clientForTargets.keySet()));
			}
			final Reporting reporting = reportingFactory.newReporting(1);
			reporting.reportStart("Start downloading file from OBS " + keyObjectStorage + " to " + target);
			try {
				outboxClient.transfer(family, keyObjectStorage);
				reporting.reportStop("End downloading file from OBS " + keyObjectStorage + " to " + target);
			} catch (Exception e) {
				reporting.reportError("Error downloading file from OBS {} to {}: {} ", keyObjectStorage, target, LogUtils.toString(e));
				throw e;
			}	
		} catch (Exception e) {
			throw new DisseminationException(
					String.format("Error downloading file %s (%s) from OBS: %s", keyObjectStorage, family, LogUtils.toString(e))
			);
		}		
	}
	
    final MqiListener<ProductDto> newMqiListener(final Iterable<DisseminationTypeConfiguration> typeConfig) {
    	return mess -> {      		
			final ProductDto product = mess.getBody();
			for (final DisseminationTypeConfiguration config : typeConfig) {			
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
						LOG.error(message,e);
						reporting.reportError(message);									
						errorAppender.send(new FailedProcessingDto(
								properties.getHostname(), 
								new Date(), 
								message, 
								mess
						));									
						throw new RuntimeException(message, e);
					} 
					reporting.reportStop("End dissemination of product to outbox " + config.getTarget());    							
				}			
			}
		};
    }
    
}

