package esa.s1pdgs.cpoc.disseminator;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import esa.s1pdgs.cpoc.disseminator.service.DisseminationException;
import esa.s1pdgs.cpoc.disseminator.service.DisseminationService;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;
import esa.s1pdgs.cpoc.report.message.output.OutboxReportingOutput;

public class DisseminationTriggerListener<E extends AbstractMessage> implements MqiListener<E>{
	private static final Logger LOG = LogManager.getLogger(DisseminationService.class);
	
	// list of all outbox client factories for protocols. For newly implemented protocols, add factory here
	private static final Map<Protocol,OutboxClient.Factory> FACTORIES = new HashMap<>();
	static {
		FACTORIES.put(Protocol.FILE, new LocalOutboxClient.Factory());
		FACTORIES.put(Protocol.FTPS, new FtpsOutboxClient.Factory());
		FACTORIES.put(Protocol.SFTP, new SftpOutboxClient.Factory());
		FACTORIES.put(Protocol.FTP, new FtpOutboxClient.Factory());
	}
	
    private final ObsClient obsClient;
	private final DisseminationProperties properties;
	private final ErrorRepoAppender errorAppender;
	private final Map<String,OutboxClient> clientForTargets = new HashMap<>();

	public static <E extends AbstractMessage> DisseminationTriggerListener<E> valueOf(
			final Class<E> dtoClass,
		    final ObsClient obsClient,
			final DisseminationProperties properties,
			final ErrorRepoAppender errorAppender
	) {
		return new DisseminationTriggerListener<E>(obsClient, properties, errorAppender);
	}
	
	public DisseminationTriggerListener(
		    final ObsClient obsClient,
			final DisseminationProperties properties,
			final ErrorRepoAppender errorAppender
	) {
		this.obsClient = obsClient;
		this.properties = properties;
		this.errorAppender = errorAppender;
	}
	
	@PostConstruct
	public void initListener() {
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
	}
    
	@Override
	public void onMessage(final GenericMessageDto<E> message) {
		final AbstractMessage body = message.getBody();
		LOG.debug("Handling {}", message);
		
		for (final DisseminationTypeConfiguration config : configsFor(body.getProductFamily())) {	
			LOG.trace("Checking if product {} matches {}", body.getKeyObjectStorage(), config.getRegex());
			if (body.getKeyObjectStorage().matches(config.getRegex())) {
				LOG.debug("Found config {} for product {}", config, body.getKeyObjectStorage());
				handleTransferTo(message, config.getTarget());
			}			
		}			
	}
	
    @Override
	public final void onTerminalError(final GenericMessageDto<E> message, final Exception error) {
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

	final void handleTransferTo(final GenericMessageDto<E> message, final String target) {		
		AbstractMessage body = message.getBody();
		
		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.predecessor(body.getUid())				
				.newReporting("Dissemination");	
		
		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(body.getProductFamily(), body.getKeyObjectStorage()),
				new ReportingMessage("Start dissemination of product to outbox {}", target) 
		);
		
		try {
			assertExists(body);
			final OutboxClient outboxClient = clientForTarget(target);
			final String targetUrl = Retries.performWithRetries(
					() -> outboxClient.transfer(new ObsObject(body.getProductFamily(), body.getKeyObjectStorage()), reporting), 
					"Transfer of " + body.getKeyObjectStorage() + " to " + target,
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

	final void assertExists(final AbstractMessage message) throws ObsServiceException, SdkClientException {
		if (!obsClient.prefixExists(new ObsObject(message.getProductFamily(), message.getKeyObjectStorage()))) {
			throw new DisseminationException(
					String.format(
							"OBS file '%s' (%s) does not exist", 
							message.getKeyObjectStorage(), 
							message.getProductFamily()
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
