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
import esa.s1pdgs.cpoc.ingestion.worker.config.IngestionWorkerServiceConfigurationProperties;
import esa.s1pdgs.cpoc.ingestion.worker.config.IngestionTypeConfiguration;
import esa.s1pdgs.cpoc.ingestion.worker.product.IngestionResult;
import esa.s1pdgs.cpoc.ingestion.worker.product.Product;
import esa.s1pdgs.cpoc.ingestion.worker.product.ProductException;
import esa.s1pdgs.cpoc.ingestion.worker.product.ProductService;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.report.FilenameReportingOutput;
import esa.s1pdgs.cpoc.report.InboxReportingInput;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;

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
		final Reporting.Factory reportingFactory = new LoggerReporting.Factory("Ingestion");

		final IngestionJob ingestion = message.getBody();
		LOG.debug("received Ingestion: {}", ingestion.getProductName());

		final Reporting reporting = reportingFactory.newReporting(0);
		reporting.begin(
				new InboxReportingInput(ingestion.getProductName(), ingestion.getRelativePath(), ingestion.getPickupPath()), 
				new ReportingMessage("Start processing of {}", ingestion.getProductName())
		);

		try {
			final IngestionResult result = identifyAndUpload(reportingFactory, message, ingestion);
			publish(result.getIngestedProducts(), message, reportingFactory);
			delete(ingestion, reportingFactory);
			reporting.end(
					new FilenameReportingOutput(ingestion.getProductName()),
					new ReportingMessage(result.getTransferAmount(),"End processing of {}", ingestion.getProductName())
			);
		} catch (Exception e) {
			reporting.error(new ReportingMessage(LogUtils.toString(e)));
		}
	}

	final IngestionResult identifyAndUpload(final Reporting.Factory reportingFactory,
			final GenericMessageDto<IngestionJob> message, final IngestionJob ingestion) throws InternalErrorException {
		IngestionResult result = IngestionResult.NULL;
		try {
			final ProductFamily family = getFamilyFor(ingestion);

			final Reporting reportObs = reportingFactory.newReporting(1);

			reportObs.begin(new ReportingMessage("Start uploading {} in OBS", ingestion.getProductName()));

			try {
				result = productService.ingest(family, ingestion);
			} catch (ProductException e) {
				reportObs.error(new ReportingMessage("Error uploading {} in OBS: {}", ingestion.getProductName(), e.getMessage()));
				throw e;
			}
			reportObs.end(new ReportingMessage("End uploading {} in OBS", ingestion.getProductName()));
			// is thrown if product shall be marked as invalid
		} catch (ProductException e) {
			LOG.warn(e.getMessage());
			productService.markInvalid(ingestion);
			message.getBody().setFamily(ProductFamily.INVALID);
			final FailedProcessingDto failed = new FailedProcessingDto(properties.getHostname(), new Date(),
					e.getMessage(), message);
			errorRepoAppender.send(failed);
		}
		return result;
	}

	final void publish(final List<Product<AbstractDto>> products, final GenericMessageDto<IngestionJob> message,
			final Reporting.Factory reportingFactory) throws InternalErrorException {
		for (final Product<AbstractDto> product : products) {
			final GenericPublicationMessageDto<? extends AbstractDto> result = new GenericPublicationMessageDto<>(
					message.getId(), product.getFamily(), product.getDto());
			result.setInputKey(message.getInputKey());
			result.setOutputKey(product.getFamily().toString());
			LOG.info("publishing : {}", result);

			final Reporting reporting = reportingFactory.newReporting(2);

			final ProductCategory category = ProductCategory.of(product.getFamily());
			reporting.begin(new ReportingMessage("Start publishing file {} in topic", message.getBody().getProductName()));
			try {
				mqiClient.publish(result, category);
				reporting.end(new ReportingMessage("End publishing file {} in topic", message.getBody().getProductName()));
			} catch (AbstractCodedException e) {
				reporting.error(new ReportingMessage("[code {}] {}", e.getCode().getCode(), e.getLogMessage()));
			}
		}
	}

	final void delete(final IngestionJob ingestion, final Reporting.Factory reportingFactory)
			throws InternalErrorException, InterruptedException {
		final File file = Paths.get(ingestion.getPickupPath(), ingestion.getRelativePath()).toFile();
		if (file.exists()) {
			final Reporting reporting = reportingFactory.newReporting(3);
			reporting.begin(new ReportingMessage("Start removing file {}", file.getPath()));

			FileUtils.deleteWithRetries(file, properties.getMaxRetries(), properties.getTempoRetryMs());
		}
	}

	final ProductFamily getFamilyFor(final IngestionJob dto) throws ProductException {
		for (final IngestionTypeConfiguration config : properties.getTypes()) {
			if (dto.getProductName().matches(config.getRegex())) {
				LOG.debug("Found {} for {}", config, dto);
				try {
					return ProductFamily.valueOf(config.getFamily());
				} catch (Exception e) {
					throw new ProductException(String.format("Invalid %s for %s (allowed: %s)", config, dto,
							Arrays.toString(ProductFamily.values())));
				}
			}
		}
		throw new ProductException(String.format("No matching config found for %s in: %s", dto, properties.getTypes()));
	}

}
