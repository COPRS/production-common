package esa.s1pdgs.cpoc.compression.trigger.service;

import java.util.Collections;
import java.util.Date;
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
import esa.s1pdgs.cpoc.compression.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.compression.trigger.config.TriggerConfigurationProperties;
import esa.s1pdgs.cpoc.compression.trigger.config.TriggerConfigurationProperties.CategoryConfig;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionDirection;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

@Service
public class CompressionTriggerService implements MqiListener<ProductionEvent> {

	private static final Logger LOGGER = LogManager.getLogger(CompressionTriggerService.class);
	
	private static final String SUFFIX_ZIPPRODUCTFAMILY = "_ZIP";
	private static final String SUFFIX_ZIPPPRODUCTFILE = ".zip";

	private final GenericMqiClient mqiClient;
	private final TriggerConfigurationProperties properties;
	private final List<MessageFilter> messageFilter;
	private final ErrorRepoAppender errorAppender;
	private final ProcessConfiguration settings;
	
	private final AppStatus appStatus;

	@Autowired
	public CompressionTriggerService(
			final GenericMqiClient mqiClient,
			final List<MessageFilter> messageFilter,
			final AppStatus appStatus,
			final TriggerConfigurationProperties properties,
			final ErrorRepoAppender errorAppender,
			final ProcessConfiguration settings
	) {
		this.mqiClient = mqiClient;
		this.messageFilter = messageFilter;
		this.appStatus = appStatus;
		this.properties = properties;
		this.errorAppender = errorAppender;
		this.settings = settings;
	}
	
	@PostConstruct
	public void initService() {
		LOGGER.info("Setting up product event listeners");
		final Map<ProductCategory, CategoryConfig> entries = properties.getProductCategories();
		final ExecutorService service = Executors.newFixedThreadPool(entries.size());
		
		for (final Map.Entry<ProductCategory, CategoryConfig> entry : entries.entrySet()) {			
			service.execute(newMqiConsumerFor(entry.getKey(), entry.getValue()));
		}
	}	

	@Override
	public final MqiMessageEventHandler onMessage(final GenericMessageDto<ProductionEvent> message) throws Exception {
		final ProductionEvent event = message.getBody();
		
		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.predecessor(event.getUid())
				.newReporting("CompressionTrigger");
		
		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(event.getProductFamily(), event.getProductName()),
				new ReportingMessage("Start handling of event for %s", event.getProductName())
		);
		return new MqiMessageEventHandler.Builder<CompressionJob>(ProductCategory.COMPRESSION_JOBS)
				.onSuccess(res -> reporting.end(new ReportingMessage("Finished handling of event for %s", event.getProductName())))
				.onError(e -> reporting.error(new ReportingMessage(
						"Error on handling event for %s: %s", 
						event.getProductName(), 
						LogUtils.toString(e))
				))
				.publishMessageProducer(() -> {
					final CompressionJob job = toCompressionJob(event);
					job.setUid(reporting.getUid());
					return Collections.singletonList(publish(message, job));
				})
				.newResult();
	}
			
	@Override
	public final void onTerminalError(final GenericMessageDto<ProductionEvent> message, final Exception error) {
		LOGGER.error(error);
		errorAppender.send(new FailedProcessingDto(
				settings.getHostname(), 
				new Date(),
				String.format(
						"Error on handling ProductionEvent for %s: %s", 
						message.getBody().getProductName(), 
						LogUtils.toString(error)
				), 
				message
		));
	}

	private final MqiConsumer<?> newMqiConsumerFor(final ProductCategory cat, final CategoryConfig config) {
		LOGGER.debug("Creating MQI consumer for category {} using {}", cat, config);	
		return new MqiConsumer<ProductionEvent>(
				mqiClient, 
				cat, 
				this,
				messageFilter,
				config.getFixedDelayMs(),
				config.getInitDelayPolMs(),
				appStatus
		);
	}
	
	final GenericPublicationMessageDto<CompressionJob> publish(
			final GenericMessageDto<ProductionEvent> mess, 
			final CompressionJob job
	) {
    	final GenericPublicationMessageDto<CompressionJob> messageDto = new GenericPublicationMessageDto<CompressionJob>(
    			mess.getId(), 
    			job.getProductFamily(), 
    			job
    	);
    	messageDto.setInputKey(mess.getInputKey());
    	messageDto.setOutputKey(job.getOutputProductFamily().name());
    	return messageDto;
	}
	
	private final CompressionJob toCompressionJob(final ProductionEvent event) {
		return new CompressionJob(
				event.getKeyObjectStorage(), 
				event.getProductFamily(),
				getCompressedKeyObjectStorage(event.getKeyObjectStorage()),
				getCompressedProductFamily(event.getProductFamily()),
				CompressionDirection.COMPRESS
		);
	}
	
	String getCompressedKeyObjectStorage(final String inputKeyObjectStorage) {
		return inputKeyObjectStorage + SUFFIX_ZIPPPRODUCTFILE;
	}

	ProductFamily getCompressedProductFamily(final ProductFamily inputFamily) {
		return ProductFamily.fromValue(inputFamily.toString() + SUFFIX_ZIPPRODUCTFAMILY);
	}
}
