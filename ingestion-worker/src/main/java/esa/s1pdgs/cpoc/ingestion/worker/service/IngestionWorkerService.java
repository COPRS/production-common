package esa.s1pdgs.cpoc.ingestion.worker.service;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
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
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
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
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
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
	private final ErrorRepoAppender errorRepoAppender;
	private final IngestionWorkerServiceConfigurationProperties properties;
	private final ProductService productService;
	private final AppStatus appStatus;
	private final InboxAdapterManager inboxAdapterManager;

	@Autowired
	public IngestionWorkerService(
			final GenericMqiClient mqiClient, 
			final ErrorRepoAppender errorRepoAppender,
			final IngestionWorkerServiceConfigurationProperties properties, 
			final ProductService productService,
			final AppStatus appStatus,
			final InboxAdapterManager inboxAdapterManager
	) {
		this.mqiClient = mqiClient;
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
					properties.getPollingIntervalMs(),
					0L,
					appStatus
			));
		}
	}

	@Override
	public final void onMessage(final GenericMessageDto<IngestionJob> message) throws Exception {
		final IngestionJob ingestion = message.getBody();
		
		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.predecessor(ingestion.getUid())				
				.newReporting("IngestionWorker");
		
		LOG.debug("received Ingestion: {}", ingestion.getProductName());
		reporting.begin(
				new InboxReportingInput(ingestion.getProductName(), ingestion.getRelativePath(), ingestion.getPickupBaseURL()), 
				new ReportingMessage("Start processing of %s", ingestion.getProductName())
		);

		try {		
			final URI productUri = IngestionJobs.toUri(ingestion);
			
			final InboxAdapter inboxAdapter = inboxAdapterManager.getInboxAdapterFor(productUri);
			
			final List<Product<IngestionEvent>> result = identifyAndUpload(message, inboxAdapter, ingestion, reporting);
			final Date ingestionFinishedDate = new Date();
			publish(result, message, reporting.getUid());
			delete(ingestion);			
						
			inboxAdapter.delete(productUri);

			reporting.end(
					IngestionWorkerReportingOutput.newInstance(ingestion, ingestionFinishedDate),
					new ReportingMessage(
							ingestion.getProductSizeByte(),
							"End processing of %s", ingestion.getKeyObjectStorage()
					)
			);
		} catch (final Exception e) {
			reporting.error(new ReportingMessage("Error processing of %s: %s", ingestion.getKeyObjectStorage(),  LogUtils.toString(e)));
			throw e;
		}
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
			productService.markInvalid(inboxAdapter, ingestion, reportingFactory);
			message.getBody().setProductFamily(ProductFamily.INVALID);
			throw e;
		}
	}

	final void publish(
			final List<Product<IngestionEvent>> products, 
			final GenericMessageDto<IngestionJob> message,
			final UUID reportingId
	) throws AbstractCodedException {
		// S1PRO-1518: detect original product family for failed requests, which are family INVALID
		// as later systems consuming the upstream messages rely on the correct family
		final IngestionJob ingestion = message.getBody();
		final ProductFamily originalFamily = ingestion.getOriginalFamily();
		
		for (final Product<IngestionEvent> product : products) {
			final IngestionEvent event = product.getDto();
			event.setUid(reportingId);
			event.setProductFamily(originalFamily);	
			
			final GenericPublicationMessageDto<IngestionEvent> result = new GenericPublicationMessageDto<>(
					message.getId(), 
					originalFamily, 
					event
			);
			result.setInputKey(message.getInputKey());
			// S1PRO-1518: keep family 'INVALID' here in restart scenario to allow possible different routing
			result.setOutputKey(product.getFamily().toString());
			LOG.info("publishing : {}", result);
			mqiClient.publish(result, ProductCategory.INGESTION_EVENT);
		}
	}

	final void delete(final IngestionJob ingestion)
			throws InterruptedException,  InternalErrorException {
		final URI uri = IngestionJobs.toUri(ingestion);
		if (!uri.getScheme().equals("file")) {
			LOG.debug("skipping deletion of file {}",ingestion.getProductName());
			return;
		}		
		final File file = Paths.get(uri)
				.resolve(ingestion.getRelativePath())
				.toFile();
		
		if (file.exists()) {
			LOG.debug("Deleting file {}", file);
			FileUtils.deleteWithRetries(
					file, 
					properties.getMaxRetries(), 
					properties.getTempoRetryMs()
			);
		}
	}	
}
