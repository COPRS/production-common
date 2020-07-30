package esa.s1pdgs.cpoc.ingestion.worker.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.ingestion.worker.config.IngestionWorkerServiceConfigurationProperties;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapterManager;
import esa.s1pdgs.cpoc.ingestion.worker.product.IngestionJobs;
import esa.s1pdgs.cpoc.ingestion.worker.product.Product;
import esa.s1pdgs.cpoc.ingestion.worker.product.ProductService;
import esa.s1pdgs.cpoc.ingestion.worker.product.report.IngestionWorkerReportingOutput;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingFactory;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;
import esa.s1pdgs.cpoc.report.message.input.InboxReportingInput;

@Service
public class IngestionWorkerService implements MqiListener<IngestionJob> {
	static final Logger LOG = LogManager.getLogger(IngestionWorkerService.class);

	private final GenericMqiClient mqiClient;
	private final List<MessageFilter> messageFilter;
	private final ErrorRepoAppender errorRepoAppender;
	private final IngestionWorkerServiceConfigurationProperties properties;
	private final ProductService productService;
	private final AppStatus appStatus;
	private final InboxAdapterManager inboxAdapterManager;

	@Autowired
	public IngestionWorkerService(
			final GenericMqiClient mqiClient, 
			final List<MessageFilter> messageFilter,
			final ErrorRepoAppender errorRepoAppender,
			final IngestionWorkerServiceConfigurationProperties properties, 
			final ProductService productService,
			final AppStatus appStatus,
			final InboxAdapterManager inboxAdapterManager
	) {
		this.mqiClient = mqiClient;
		this.messageFilter = messageFilter;
		this.errorRepoAppender = errorRepoAppender;
		this.properties = properties;
		this.productService = productService;
		this.appStatus = appStatus;
		this.inboxAdapterManager = inboxAdapterManager;
	}
	
	@PostConstruct
	public void initService() {
		if (properties.getPollingIntervalMs() > 0) {
			final ExecutorService service = Executors.newFixedThreadPool(1);
			service.execute(new MqiConsumer<IngestionJob>(
					mqiClient,
					ProductCategory.INGESTION, 
					this,
					messageFilter,
					properties.getPollingIntervalMs(),
					0L,
					appStatus
			));
		}
	}

	@Override
	public final MqiMessageEventHandler onMessage(final GenericMessageDto<IngestionJob> message) throws Exception {
		final IngestionJob ingestion = message.getBody();
		
		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.predecessor(ingestion.getUid())				
				.newReporting("IngestionWorker");
		
		LOG.debug("received Ingestion: {}", ingestion.getProductName());		
		final URI productUri = IngestionJobs.toUri(ingestion);		
		final InboxAdapter inboxAdapter = inboxAdapterManager.getInboxAdapterFor(productUri);
	
		return new MqiMessageEventHandler.Builder<IngestionEvent>(ProductCategory.INGESTION_EVENT)
				.onSuccess(res -> {
					inboxAdapter.delete(productUri);
					reporting.end(
							IngestionWorkerReportingOutput.newInstance(ingestion, new Date()),
							new ReportingMessage(
									ingestion.getProductSizeByte(),
									"End processing of %s", ingestion.getKeyObjectStorage()
							)
					);
				})
				.onError(e -> reporting.error(new ReportingMessage(
						"Error processing of %s: %s", 
						ingestion.getKeyObjectStorage(),  
						LogUtils.toString(e)
				)))
				.onMessage(() -> {					
					reporting.begin(
							new InboxReportingInput(ingestion.getProductName(), ingestion.getRelativePath(), ingestion.getPickupBaseURL()), 
							new ReportingMessage("Start processing of %s", ingestion.getProductName())
					);	
					final List<Product<IngestionEvent>> result = identifyAndUpload(message, inboxAdapter, ingestion, reporting);
					return publish(result, message, reporting.getUid());					
				})
				.newResult();
	}
	
	@Override
	public final void onTerminalError(final GenericMessageDto<IngestionJob> message, final Exception error) {
		LOG.error(error);
		errorRepoAppender.send(new FailedProcessingDto(
				properties.getHostname(), 
				new Date(),
				String.format("Error on handling IngestionJob message %s: %s", message.getId(), LogUtils.toString(error)), 
				message
		));
	}

	final List<Product<IngestionEvent>> identifyAndUpload(
			final GenericMessageDto<IngestionJob> message, 
			final InboxAdapter inboxAdapter,
			final IngestionJob ingestion,
			final ReportingFactory reportingFactory
	) throws Exception {
		try {
			return productService.ingest(ingestion.getProductFamily(), inboxAdapter, ingestion, reportingFactory);
		} 
		catch (final Exception e) {
			LOG.error(e);
			throw e;
		}
	}

	final List<GenericPublicationMessageDto<IngestionEvent>> publish(
			final List<Product<IngestionEvent>> products, 
			final GenericMessageDto<IngestionJob> message,
			final UUID reportingId
	) throws AbstractCodedException {				
		final List<GenericPublicationMessageDto<IngestionEvent>> results = new ArrayList<>();		
		for (final Product<IngestionEvent> product : products) {
			final IngestionEvent event = product.getDto();
			event.setUid(reportingId);
			
			final GenericPublicationMessageDto<IngestionEvent> result = new GenericPublicationMessageDto<>(
					message.getId(), 
					product.getFamily(), 
					event
			);
			result.setInputKey(message.getInputKey());
			result.setOutputKey(product.getFamily().toString());
			LOG.info("publishing : {}", result);
			results.add(result);
		}
		return results;
	}	
}
