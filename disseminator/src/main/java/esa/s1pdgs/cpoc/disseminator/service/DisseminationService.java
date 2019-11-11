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
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.FilenameReportingInput;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.OutboxReportingOutput;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;

@Service
public class DisseminationService implements MqiListener<ProductDto> {	
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
    		service.execute(new MqiConsumer<ProductDto>(client, entry.getKey(),this, properties.getPollingIntervalMs()));
    	}
    }
    
	@Override
	public void onMessage(final GenericMessageDto<ProductDto> message) {
		final ProductDto product = message.getBody();
		LOG.debug("Handling {}", message);
		
		for (final DisseminationTypeConfiguration config : configsFor(product.getFamily())) {	
			LOG.trace("Checking if product {} matches {}", product.getProductName(), config.getRegex());
			if (product.getProductName().matches(config.getRegex())) {
				LOG.debug("Found config {} for product {}", config, product.getProductName());
				handleTransferTo(message, config.getTarget());
			}			
		}			
	}
	
    final List<DisseminationTypeConfiguration> configsFor(final ProductFamily family) {
    	return properties.getCategories().getOrDefault(ProductCategory.of(family), Collections.emptyList());	
    }

	final void handleTransferTo(final GenericMessageDto<ProductDto> message, final String target) {		
		final ProductDto product = message.getBody();
		
		final Reporting.Factory rf = new LoggerReporting.Factory("Dissemination");
		final Reporting reporting = rf.newReporting(0);
		
		String targetUrl = "";
		
		reporting.begin(
				new FilenameReportingInput(product.getProductName()),
				new ReportingMessage("Start dissemination of product to outbox {}", target) 
		);
		try {
			assertExists(product);
			final OutboxClient outboxClient = clientForTarget(target);

			final Reporting reportingDl = rf.newReporting(1);
			reportingDl.begin(new ReportingMessage("Start downloading file from OBS {} to {}", product.getKeyObjectStorage(), target));
			try {
				targetUrl = Retries.performWithRetries(
						() -> outboxClient.transfer(new ObsObject(product.getFamily(), product.getKeyObjectStorage())), 
						"Transfer of " + product.getKeyObjectStorage() + " to " + target,
						properties.getMaxRetries(), 
						properties.getTempoRetryMs()
				);
				reportingDl.end(new ReportingMessage("End downloading file from OBS {} to {}", product.getKeyObjectStorage(), target));
			} catch (Exception e) {
				reportingDl.error(new ReportingMessage("Error downloading file from OBS {} to {}: {} ", 
						product.getKeyObjectStorage(), target, LogUtils.toString(e)));
				throw e;
			}							
		} catch (Exception e) {					
			final String errMessage = (e instanceof DisseminationException) ? e.getMessage() : LogUtils.toString(e); 
			final String messageString = String.format(
					"Error on dissemination of product to outbox %s: %s", 
					target, 
					errMessage
			);
			LOG.error(messageString,e);
			reporting.error(new ReportingMessage(messageString));									
			errorAppender.send(new FailedProcessingDto(
					properties.getHostname(), 
					new Date(), 
					messageString, 
					message
			));									
			throw new RuntimeException(messageString, e);
		} 
		reporting.end(
				new OutboxReportingOutput(targetUrl),
				new ReportingMessage("End dissemination of product to outbox {}", target)
		);
	}

	final void assertExists(final ProductDto product) throws ObsServiceException, SdkClientException {
		if (!obsClient.prefixExists(new ObsObject(product.getFamily(), product.getKeyObjectStorage()))) {
			throw new DisseminationException(
					String.format(
							"OBS file '%s' (%s) does not exist", 
							product.getKeyObjectStorage(), 
							product.getFamily()
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
}

