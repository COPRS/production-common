package esa.s1pdgs.cpoc.disseminator;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
import esa.s1pdgs.cpoc.disseminator.path.PathEvaluator;
import esa.s1pdgs.cpoc.disseminator.report.OverpassCoverageCheckReportingOutput;
import esa.s1pdgs.cpoc.disseminator.service.DisseminationException;
import esa.s1pdgs.cpoc.disseminator.service.DisseminationService;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.NullMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingFactory;
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
	private final MetadataClient metadataClient;
	private final ErrorRepoAppender errorAppender;
	private final Map<String,OutboxClient> clientForTargets = new HashMap<>();
	private final Pattern overpassCoverageCheckPattern; 
	
	public static <E extends AbstractMessage> DisseminationTriggerListener<E> valueOf(
			final Class<E> dtoClass,
		    final ObsClient obsClient,
		    final MetadataClient metadataClient,
			final DisseminationProperties properties,
			final ErrorRepoAppender errorAppender
	) {
		return new DisseminationTriggerListener<E>(obsClient, metadataClient, properties, errorAppender);
	}
	
	public DisseminationTriggerListener(
		    final ObsClient obsClient,
		    final MetadataClient metadataClient,
			final DisseminationProperties properties,
			final ErrorRepoAppender errorAppender
	) {
		this.obsClient = obsClient;
		this.metadataClient = metadataClient;
		this.properties = properties;
		this.errorAppender = errorAppender;
		this.overpassCoverageCheckPattern = Pattern.compile(properties.getOverpassCoverageCheckPattern());
	}
	
	public void initListener() {
		// Init list of configured outboxes    	
    	for (final Map.Entry<String, OutboxConfiguration> entry : properties.getOutboxes().entrySet()) {	
    		final String target = entry.getKey();
    		final OutboxConfiguration config = entry.getValue();    	
    		final PathEvaluator eval = PathEvaluator.newInstance(config);

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
					return new MqiPublishingJob<NullMessage>(Collections.emptyList());
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
		ProductCategory pc = ProductCategory.of(family);
		
		final List<DisseminationTypeConfiguration> hits = properties.getCategories().getOrDefault(pc, Collections.emptyList());
		LOG.debug("Family '{}' was resolved to category '{}' finding {} configurations",family, pc, hits.size());
    	return 	hits;
    }

	final void handleTransferTo(final GenericMessageDto<E> message, final String target) {		
		final AbstractMessage body = message.getBody();
		
		final Reporting reporting = ReportingUtils.newReportingBuilder(MissionId.fromFileName(body.getKeyObjectStorage()))
				.predecessor(body.getUid())				
				.newReporting("Dissemination");	
		
		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(body.getProductFamily(), body.getKeyObjectStorage()),
				new ReportingMessage("Start dissemination of product to outbox {}", target) 
		);
		
		boolean acceptFile = true;
		boolean disableOverpassCheck = true;

		try {
			assertExists(body);

			if (properties.isDisableOverpassCheck()) {
				LOG.trace("Overpass check is disabled");
			} else {
				acceptFile = isAllowedByOverpassCheck(body.getProductFamily(), body.getKeyObjectStorage(), reporting);
			}
			
			if (acceptFile) {
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
			} else{
				if (!disableOverpassCheck) {
					LOG.warn("Ignoring file {} of ProductFamily of {} because it is not over overpass", body.getKeyObjectStorage(), body.getProductFamily());
					reporting.end(new ReportingMessage("File %s of ProductFamily of %s is ignored because it is not over overpass", body.getKeyObjectStorage(), body.getProductFamily()));
				} else {
					LOG.warn("Ignoring file {} of ProductFamily of {}", body.getKeyObjectStorage(), body.getProductFamily());
					reporting.end(new ReportingMessage("File %s of ProductFamily of %s is ignored", body.getKeyObjectStorage(), body.getProductFamily()));
				}
			}
		} catch (final Exception e) {					
			final String messageString = errorMessageFor(e, target);			
			reporting.error(new ReportingMessage(messageString));																	
			throw new RuntimeException(messageString, e);
		} 
	}	

	private final boolean isAllowedByOverpassCheck(final ProductFamily family, final String productName, final ReportingFactory reporting) throws Exception {
        if (overpassCoverageCheckPattern.matcher(productName).matches()) {
	        if (!(family == ProductFamily.L1_SLICE || family == ProductFamily.L1_ACN)) {
	        	throw new RuntimeException(String.format("Unsupported ProductFamily %s for overpass check", family));
	        } else {
	        	final Reporting overpassReport = reporting.newReporting("OverpassCoverageCheck");
			    try {
			    	overpassReport.begin(
			    			ReportingUtils.newFilenameReportingInputFor(family, productName),
			    			new ReportingMessage("Checking overpass coverage")
					);
					if (metadataClient.getOverpassCoverage(family, productName) >= properties.getMinOverpassCoveragePercentage()) {
						overpassReport.end(
								new OverpassCoverageCheckReportingOutput(true), 
								new ReportingMessage("Product %s is over overpass", productName)
						);
						return true;
					} else {
						overpassReport.end(
								new OverpassCoverageCheckReportingOutput(false), 
								new ReportingMessage("Product %s is not over overpass", productName)
			    		);
						return false;
					}			
				} catch (final Exception e) {
					overpassReport.error(new ReportingMessage("OverpassCoverage check failed: %s", LogUtils.toString(e)));
					throw e;
				}
	        }
        } else {
        	LOG.trace("Skipping overpass check. Product {} does not match pattern {}", productName, overpassCoverageCheckPattern.pattern());
        	return true;
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
