package esa.s1pdgs.cpoc.disseminator.service;

import java.util.Collections;
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
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.common.utils.Retries;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.DisseminationTypeConfiguration;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration.Protocol;
import esa.s1pdgs.cpoc.disseminator.outbox.FtpOutboxClient;
import esa.s1pdgs.cpoc.disseminator.outbox.FtpsOutboxClient;
import esa.s1pdgs.cpoc.disseminator.outbox.LocalOutboxClient;
import esa.s1pdgs.cpoc.disseminator.outbox.OutboxClient;
import esa.s1pdgs.cpoc.disseminator.outbox.SftpOutboxClient;
import esa.s1pdgs.cpoc.disseminator.path.PathEvaluater;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;
import esa.s1pdgs.cpoc.report.message.output.OutboxReportingOutput;

@Service
public class DisseminationService implements MqiListener<ProductionEvent> {	
	private static final Logger LOG = LogManager.getLogger(DisseminationService.class);
	
	// list of all outbox client factories for protocols. For newly implemented protocols, add factory here
	private static final Map<Protocol,OutboxClient.Factory> FACTORIES = new HashMap<>();
	static {
		FACTORIES.put(Protocol.FILE, new LocalOutboxClient.Factory());
		FACTORIES.put(Protocol.FTPS, new FtpsOutboxClient.Factory());
		FACTORIES.put(Protocol.SFTP, new SftpOutboxClient.Factory());
		FACTORIES.put(Protocol.FTP, new FtpOutboxClient.Factory());
	}
	
	private final GenericMqiClient client;
    private final ObsClient obsClient;
	private final DisseminationProperties properties;
	private final ErrorRepoAppender errorAppender;
	private final Map<String,OutboxClient> clientForTargets = new HashMap<>();
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
    	// Init list of configured outboxes    	
    	for (final Map.Entry<String, OutboxConfiguration> entry : properties.getOutboxes().entrySet()) {	
    		final String target = entry.getKey();
    		final OutboxConfiguration config = entry.getValue();    	
    		final PathEvaluater eval = PathEvaluater.newInstance(config);

    		final OutboxClient outboxClient = FACTORIES.getOrDefault(config.getProtocol(), OutboxClient.Factory.NOT_DEFINED_ERROR)
    				.newClient(obsClient, config, eval);    		
    		LOG.info("Using {} for Outbox target '{}'", outboxClient, target);
    		put(target, outboxClient);
    	}    	
    	
        // Init the list of consumers and start them
    	final ExecutorService service = Executors.newFixedThreadPool(properties.getCategories().size());
    	
    	for (final Map.Entry<ProductCategory, List<DisseminationTypeConfiguration>> entry : properties.getCategories().entrySet()) {	
    		// start consumer for each category
    		LOG.debug("Starting consumer for {}", entry);
    		service.execute(new MqiConsumer<ProductionEvent>(
    				client, 
    				entry.getKey(),
    				this, 
    				properties.getPollingIntervalMs(),
    				0L,
    				appStatus    				
    		));
    	}
    }
    
	@Override
	public void onMessage(final GenericMessageDto<ProductionEvent> message) {
		final ProductionEvent product = message.getBody();
		LOG.debug("Handling {}", message);
		
		for (final DisseminationTypeConfiguration config : configsFor(product.getProductFamily())) {	
			LOG.trace("Checking if product {} matches {}", product.getKeyObjectStorage(), config.getRegex());
			if (product.getKeyObjectStorage().matches(config.getRegex())) {
				LOG.debug("Found config {} for product {}", config, product.getKeyObjectStorage());
				handleTransferTo(message, config.getTarget());
			}			
		}			
	}
	
    @Override
	public final void onTerminalError(final GenericMessageDto<ProductionEvent> message, final Exception error) {
    	LOG.error(error);    	
		errorAppender.send(new FailedProcessingDto(
				properties.getHostname(), 
				new Date(), 
				error.getMessage(), 
				message
		));		
	}

	final List<DisseminationTypeConfiguration> configsFor(final ProductFamily family) {
    	return properties.getCategories().getOrDefault(ProductCategory.of(family), Collections.emptyList());	
    }

	final void handleTransferTo(final GenericMessageDto<ProductionEvent> message, final String target) {		
		final ProductionEvent product = message.getBody();
		
		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.predecessor(product.getUid())				
				.newReporting("Dissemination");	
		
		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(product.getProductFamily(), product.getKeyObjectStorage()),
				new ReportingMessage("Start dissemination of product to outbox {}", target) 
		);
		
		try {
			assertExists(product);
			final OutboxClient outboxClient = clientForTarget(target);
			final String targetUrl = Retries.performWithRetries(
					() -> outboxClient.transfer(new ObsObject(product.getProductFamily(), product.getKeyObjectStorage()), reporting), 
					"Transfer of " + product.getKeyObjectStorage() + " to " + target,
					properties.getMaxRetries(), 
					properties.getTempoRetryMs()
			);
			reporting.end(
					new OutboxReportingOutput(targetUrl),
					new ReportingMessage("End dissemination of product to outbox {}", target)
			);
		} catch (final Exception e) {					
			final String messageString = errorMessageFor(e, target);			
			reporting.error(new ReportingMessage(messageString));																	
			throw new RuntimeException(messageString, e);
		} 
	}	

	final void assertExists(final ProductionEvent product) throws ObsServiceException, SdkClientException {
		if (!obsClient.prefixExists(new ObsObject(product.getProductFamily(), product.getKeyObjectStorage()))) {
			throw new DisseminationException(
					String.format(
							"OBS file '%s' (%s) does not exist", 
							product.getKeyObjectStorage(), 
							product.getProductFamily()
					)
			);
		}
	}
    
	final void put(final String target, final OutboxClient outboxClient) {
		clientForTargets.put(target, outboxClient);		
	}
	
	final OutboxClient clientForTarget(final String target) {
		final OutboxClient outboxClient = clientForTargets.get(target);
		
		// assert there is an outbox configured for the given target
		if (outboxClient == null) {
			throw new DisseminationException(String.format(
					"No outbox configured for '%s'. Available are: %s", 
					target, 
					clientForTargets.keySet()
			));
		}
		return outboxClient;
	}
	
	private final String errorMessageFor(final Exception e, final String target) {
		final String errMessage = (e instanceof DisseminationException) ? e.getMessage() : LogUtils.toString(e); 
		return String.format(
				"Error on dissemination of product to outbox %s: %s", 
				target, 
				errMessage
		);
	}

}

