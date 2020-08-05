package esa.s1pdgs.cpoc.disseminator;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.NullMessage;
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
	public MqiMessageEventHandler onMessage(final GenericMessageDto<E> message) {		
		return new MqiMessageEventHandler.Builder<NullMessage>(ProductCategory.UNDEFINED)
				.publishMessageProducer(() -> {
					final AbstractMessage body = message.getBody();
					LOG.debug("Handling {}", message);
					
					for (final DisseminationTypeConfiguration config : configsFor(body.getProductFamily())) {	
						LOG.trace("Checking if product {} matches {}", body.getKeyObjectStorage(), config.getRegex());
						if (body.getKeyObjectStorage().matches(config.getRegex())) {
							LOG.debug("Found config {} for product {}", config, body.getKeyObjectStorage());
							handleTransferTo(message, config.getTarget());
						}			
					}	
					return Collections.emptyList();
				})
				.newResult();
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
		ProductCategory pc = null;
		if (family == ProductFamily.PLAN_AND_REPORT) {
			/* I am so sorry, this is a hotfix: Due to a product category mismatch
			 * the disiminator is not able to resolve its configuration. The event
			 * being consumed is a CatalogEvent, however it will resolve here as a PLAN_AND_REPORT
			 * instead, not able to resolve it. Using this category instead will however consume
			 * IngestEvent as DTO not able to consume it anymore. After analyze of the problem
			 * this workaround seems to be best place with less impact for a hotfix.
			 * 
			 * We simply map the PC to CATALOG_EVENT if the product family is PLAN_AND_REPORT.
			 * All others are handled as usual.
			 */
			pc = ProductCategory.CATALOG_EVENT;
		} else {
			pc = ProductCategory.of(family);	
		}		
		
		final List<DisseminationTypeConfiguration> hits = properties.getCategories().getOrDefault(pc, Collections.emptyList());
		LOG.debug("Family '{}' was resolved to category '{}' finding {} configurations",family, pc, hits.size());
    	return 	hits;
    }

	final void handleTransferTo(final GenericMessageDto<E> message, final String target) {		
		final AbstractMessage body = message.getBody();
		
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
