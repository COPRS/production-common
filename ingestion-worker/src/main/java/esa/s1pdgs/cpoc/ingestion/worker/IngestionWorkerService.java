package esa.s1pdgs.cpoc.ingestion.worker;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.ingestion.worker.config.IngestionTypeConfiguration;
import esa.s1pdgs.cpoc.ingestion.worker.config.IngestionWorkerServiceConfigurationProperties;
import esa.s1pdgs.cpoc.ingestion.worker.product.IngestionResult;
import esa.s1pdgs.cpoc.ingestion.worker.product.Product;
import esa.s1pdgs.cpoc.ingestion.worker.product.ProductException;
import esa.s1pdgs.cpoc.ingestion.worker.product.ProductService;
import esa.s1pdgs.cpoc.ingestion.worker.product.ProductServiceImpl;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsEmptyFileException;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;
import esa.s1pdgs.cpoc.report.message.input.InboxReportingInput;
import esa.s1pdgs.cpoc.report.message.output.FilenameReportingOutput;

@Service
public class IngestionWorkerService implements MqiListener<IngestionJob> {
	static final Logger LOG = LogManager.getLogger(IngestionWorkerService.class);

	private final GenericMqiClient mqiClient;
	private final ErrorRepoAppender errorRepoAppender;
	private final IngestionWorkerServiceConfigurationProperties properties;
	private final ProductService productService;

	@Autowired
	public IngestionWorkerService(final GenericMqiClient mqiClient, final ErrorRepoAppender errorRepoAppender,
			final IngestionWorkerServiceConfigurationProperties properties, final ProductService productService) {
		this.mqiClient = mqiClient;
		this.errorRepoAppender = errorRepoAppender;
		this.properties = properties;
		this.productService = productService;
	}
	
	@PostConstruct
	public void initService() {
		if (properties.getPollingIntervalMs() > 0) {
			final ExecutorService service = Executors.newFixedThreadPool(1);
			service.execute(new MqiConsumer<IngestionJob>(mqiClient, ProductCategory.INGESTION, this,
					properties.getPollingIntervalMs()));
		}
	}

	@Override
	public void onMessage(final GenericMessageDto<IngestionJob> message) {
		final Reporting reporting = ReportingUtils.newReportingBuilderFor("IngestionWorker")
				.newWorkerComponentReporting();

		final IngestionJob ingestion = message.getBody();
		LOG.debug("received Ingestion: {}", ingestion.getKeyObjectStorage());
		reporting.begin(
				new InboxReportingInput(ingestion.getKeyObjectStorage(), ingestion.getRelativePath(), ingestion.getPickupPath()), 
				new ReportingMessage("Start processing of {}", ingestion.getKeyObjectStorage())
		);

		try {
			File file = ProductServiceImpl.toFile(ingestion);
			if(org.apache.commons.io.FileUtils.sizeOf(file) == 0) {
				throw new Exception("Empty file detected: " + file.getName());
			}
			
			final IngestionResult result = identifyAndUpload(reporting.getChildFactory(), message, ingestion);
			publish(result.getIngestedProducts(), message, reporting.getChildFactory());
			delete(ingestion, reporting.getChildFactory());
			reporting.end(
					new FilenameReportingOutput(ingestion.getKeyObjectStorage()),
					new ReportingMessage(result.getTransferAmount(),"End processing of {}", ingestion.getKeyObjectStorage())
			);
		} catch (final Exception e) {
			reporting.error(new ReportingMessage(LogUtils.toString(e)));
			throw new RuntimeException(
					String.format("Error on ingestion of %s: %s", ingestion, LogUtils.toString(e)),
					e
			);
		}
	}

	final IngestionResult identifyAndUpload(
			final Reporting.ChildFactory reportingChildFactory,
			final GenericMessageDto<IngestionJob> message, 
			final IngestionJob ingestion
	) throws InternalErrorException, ObsEmptyFileException {
		IngestionResult result = IngestionResult.NULL;

		// TODO: Refactor Exception handling
		try {
			final ProductFamily family = getFamilyFor(ingestion);
			final Reporting reportObs = reportingChildFactory.newChild("IngestionWorker.ObsUpload");

			reportObs.begin(new ReportingMessage("Start uploading {} in OBS", ingestion.getKeyObjectStorage()));

			try {
				reportObs.begin(new ReportingMessage("Start uploading {} in OBS", ingestion.getKeyObjectStorage()));

				try {
					result = productService.ingest(family, ingestion);
				} catch (final ProductException | ObsEmptyFileException e) {
					reportObs.error(new ReportingMessage("Error uploading {} in OBS: {}",
							ingestion.getKeyObjectStorage(), e.getMessage()));
					throw e;
				}
				reportObs.end(new ReportingMessage("End uploading {} in OBS", ingestion.getKeyObjectStorage()));
				// is thrown if product shall be marked as invalid
			} catch (final ProductException e) {
				LOG.warn(e.getMessage());
				productService.markInvalid(ingestion);
				message.getBody().setProductFamily(ProductFamily.INVALID);
				final FailedProcessingDto failed = new FailedProcessingDto(properties.getHostname(), new Date(),
						e.getMessage(), message);
				errorRepoAppender.send(failed);
			}
		} catch (ObsEmptyFileException e) {

			final FailedProcessingDto failed = new FailedProcessingDto(properties.getHostname(), new Date(),
					e.getMessage(), message);
			errorRepoAppender.send(failed);
			throw e;
		}
		return result;
	}

	final void publish(
			final List<Product<IngestionEvent>> products, 
			final GenericMessageDto<IngestionJob> message,
			final Reporting.ChildFactory reportingChildFactory
	) throws AbstractCodedException {
		for (final Product<IngestionEvent> product : products) {
			final GenericPublicationMessageDto<? extends AbstractMessage> result = new GenericPublicationMessageDto<>(
					message.getId(), product.getFamily(), product.getDto());
			result.setInputKey(message.getInputKey());
			result.setOutputKey(product.getFamily().toString());
			LOG.info("publishing : {}", result);

			final Reporting report = reportingChildFactory.newChild("IngestionWorker.Publish");

			report.begin(new ReportingMessage("Start publishing file {} in topic", message.getBody().getKeyObjectStorage()));
			try {
				mqiClient.publish(result, ProductCategory.INGESTION_EVENT);
				report.end(new ReportingMessage("End publishing file {} in topic", message.getBody().getKeyObjectStorage()));
			} catch (final AbstractCodedException e) {
				report.error(new ReportingMessage("[code {}] {}", e.getCode().getCode(), e.getLogMessage()));
			}
		}
	}

	final void delete(final IngestionJob ingestion, final Reporting.ChildFactory reportingChildFactory)
			throws InternalErrorException, InterruptedException {
		final File file = Paths.get(ingestion.getPickupPath(), ingestion.getRelativePath()).toFile();
		if (file.exists()) {
			final Reporting childReporting = reportingChildFactory.newChild("IngestionWorker.DeleteFromPickup");
			childReporting.begin(new ReportingMessage("Start removing file {}", file.getPath()));

			try {
				FileUtils.deleteWithRetries(file, properties.getMaxRetries(), properties.getTempoRetryMs());
				childReporting.end(new ReportingMessage("End removing file {}", file.getPath()));
			} catch (final Exception e) {
				childReporting.error(new ReportingMessage("Error on removing file {}: {}", LogUtils.toString(e)));
			}
		}
	}

	final ProductFamily getFamilyFor(final IngestionJob dto) throws ProductException {
		for (final IngestionTypeConfiguration config : properties.getTypes()) {
			if (dto.getKeyObjectStorage().matches(config.getRegex())) {
				LOG.debug("Found {} for {}", config, dto);
				try {
					return ProductFamily.valueOf(config.getFamily());
				} catch (final Exception e) {
					throw new ProductException(
							String.format(
									"Invalid %s for %s (allowed: %s)", 
									config, 
									dto,
									Arrays.toString(ProductFamily.values())
							)
					);
				}
			}
		}
		throw new ProductException(String.format("No matching config found for %s in: %s", dto, properties.getTypes()));
	}
	
//	private void handleProductAsInvalid(IngestionJob ingestion, GenericMessageDto<IngestionJob> ingestMessage,
//			String errorMessage) {
//
//		LOG.warn(errorMessage);
//		productService.markInvalid(ingestion);
//		ingestMessage.getBody().setProductFamily(ProductFamily.INVALID);
//		final FailedProcessingDto failed = new FailedProcessingDto(properties.getHostname(), new Date(), errorMessage,
//				ingestMessage);
//		errorRepoAppender.send(failed);
//	}

}
