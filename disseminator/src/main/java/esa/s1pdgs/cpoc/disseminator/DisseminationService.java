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
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;

public class DisseminationService implements MqiListener<ProductDto> {	
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
	private final Map<String,OutboxClient> clientForTargets = new HashMap<>();

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
    public void initService() {    	
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
    		service.execute(new MqiConsumer<ProductDto>(client, entry.getKey(),this, properties.getPollingIntervalMs()));
    	}
    }
    
	@Override
	public void onMessage(final GenericMessageDto<ProductDto> message) {
		final ProductDto product = message.getBody();
		
		for (final DisseminationTypeConfiguration config : configsFor(product.getFamily())) {			
			if (product.getProductName().matches(config.getRegex())) {		
				final Reporting.Factory rf = new LoggerReporting.Factory(LOG, "Dissemination");
				final Reporting reporting = rf.product(product.getFamily().toString(), product.getProductName())
						.newReporting(0);
				reporting.reportStart("Start dissemination of product to outbox " + config.getTarget());
				try {
					// assert source object exists in OBS
					if (!obsClient.exist(product.getFamily(), product.getKeyObjectStorage())) {
						throw new DisseminationException(
								String.format(
										"OBS file %s (%s) does not exist", 
										product.getKeyObjectStorage(), 
										product.getFamily()
								)
						);
					}
					final OutboxClient outboxClient = clientForTargets.get(config.getTarget());
					
					// assert there is an outbox configured for the given target
					if (outboxClient == null) {
						throw new DisseminationException(
								String.format(
										"No outbox configured for '%s'. Available are: %s", 
										config.getTarget(), 
										clientForTargets.keySet()
								));
					}
					final Reporting reportingDl = rf.newReporting(1);
					reportingDl.reportStart("Start downloading file from OBS " + product.getKeyObjectStorage() + 
							" to " + config.getTarget());
					try {
						outboxClient.transfer(product.getFamily(), product.getKeyObjectStorage());
						reportingDl.reportStop("End downloading file from OBS " + product.getKeyObjectStorage() + 
								" to " + config.getTarget());
					} catch (Exception e) {
						reportingDl.reportError("Error downloading file from OBS {} to {}: {} ", 
								product.getKeyObjectStorage(), config.getTarget(), LogUtils.toString(e));
						throw e;
					}							
				} catch (Exception e) {					
					final String errMessage = (e instanceof DisseminationException) ? e.getMessage() : LogUtils.toString(e); 
					final String messageString = String.format(
							"Error on dissemination of product to outbox %s: %s", 
							config.getTarget(), 
							errMessage
					);
					LOG.error(messageString,e);
					reporting.reportError(messageString);									
					errorAppender.send(new FailedProcessingDto(
							properties.getHostname(), 
							new Date(), 
							messageString, 
							message
					));									
					throw new RuntimeException(messageString, e);
				} 
				reporting.reportStop("End dissemination of product to outbox " + config.getTarget());    							
			}			
		}			
	}
    
    final Iterable<DisseminationTypeConfiguration> configsFor(final ProductFamily family) {
    	return properties.getCategories().get(ProductCategory.of(family));	
    }
}

