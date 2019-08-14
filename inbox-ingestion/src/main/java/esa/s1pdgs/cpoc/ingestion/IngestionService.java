package esa.s1pdgs.cpoc.ingestion;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
import esa.s1pdgs.cpoc.ingestion.config.IngestionServiceConfigurationProperties;
import esa.s1pdgs.cpoc.ingestion.config.IngestionTypeConfiguration;
import esa.s1pdgs.cpoc.ingestion.product.Product;
import esa.s1pdgs.cpoc.ingestion.product.ProductException;
import esa.s1pdgs.cpoc.ingestion.product.ProductService;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;

@Service
public class IngestionService {
	static final Logger LOG = LogManager.getLogger(IngestionService.class);

	private final GenericMqiClient client;
	private final ErrorRepoAppender errorRepoAppender;
	private final IngestionServiceConfigurationProperties properties;
	private final ProductService productService;

	@Autowired
	public IngestionService(final GenericMqiClient client, final ErrorRepoAppender errorRepoAppender,
			final IngestionServiceConfigurationProperties properties, final ProductService productService) {
		this.client = client;
		this.errorRepoAppender = errorRepoAppender;
		this.properties = properties;
		this.productService = productService;
	}

	public void poll() {
		try {
			final GenericMessageDto<IngestionDto> message = client.next(ProductCategory.INGESTION);
			if (message == null || message.getBody() == null) {
				LOG.trace("No message received: continue");
				return;
			}

			AckMessageDto ackMess;
			try {
				onMessage(message);
				ackMess = new AckMessageDto(message.getIdentifier(), Ack.OK, null, false);
				// any other error --> dump prominently into log file but continue
			} catch (Exception e) {
				LOG.error("Unexpected Error on Ingestion", e);
				ackMess = new AckMessageDto(message.getIdentifier(), Ack.ERROR, LogUtils.toString(e), false);
			}
			client.ack(ackMess, ProductCategory.INGESTION);
			// on communication errors with Mqi --> just dump warning and retry on next
			// polling attempt
		} catch (AbstractCodedException ace) {
			LOG.warn("Error Code: {}, Message: {}", ace.getCode().getCode(), ace.getLogMessage());
		}
	}

	public void onMessage(final GenericMessageDto<IngestionDto> message) {
		final Reporting.Factory reportingFactory = new LoggerReporting.Factory(LOG, "Ingestion");

		final IngestionDto ingestion = message.getBody();
		LOG.debug("received Ingestion: {}", ingestion.getProductName());

		final Reporting reporting = reportingFactory
				.product(ingestion.getFamily().toString(), ingestion.getProductName()).newReporting(0);
		reporting.reportStart("Start processing of " + ingestion.getProductName());

		try {
			final List<Product<AbstractDto>> result = identifyAndUpload(reportingFactory, message, ingestion);
			publish(result, message, reportingFactory);
			delete(ingestion, reportingFactory);
			reporting.reportStop("End processing of " + ingestion.getProductName());
		} catch (Exception e) {
			reporting.reportError("{}", LogUtils.toString(e));

		}
	}

	final List<Product<AbstractDto>> identifyAndUpload(final Reporting.Factory reportingFactory,
			final GenericMessageDto<IngestionDto> message, final IngestionDto ingestion) throws InternalErrorException {
		List<Product<AbstractDto>> result = new ArrayList<>();
		try {
			final ProductFamily family = getFamilyFor(ingestion);

			final Reporting reportObs = reportingFactory
					// update family information
					.product(family.toString(), ingestion.getProductName()).newReporting(1);

			reportObs.reportStart("Start uploading " + ingestion.getProductName() + " in OBS");

			try {
				result = productService.ingest(family, ingestion);
			} catch (ProductException e) {
				reportObs.reportError("Error uploading " + ingestion.getProductName() + " in OBS: {}", e.getMessage());
				throw e;
			}
			reportObs.reportStop("End uploading " + ingestion.getProductName() + " in OBS");
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

	final void publish(final List<Product<AbstractDto>> products, final GenericMessageDto<IngestionDto> message,
			final Reporting.Factory reportingFactory) throws InternalErrorException {
		for (final Product<AbstractDto> product : products) {
			final GenericPublicationMessageDto<? extends AbstractDto> result = new GenericPublicationMessageDto<>(
					message.getIdentifier(), product.getFamily(), product.getDto());
			result.setInputKey(message.getInputKey());
			result.setOutputKey(product.getFamily().toString());
			LOG.info("publishing : {}", result);

			final Reporting reporting = reportingFactory
					.product(product.getFamily().toString(), message.getBody().getProductName()).newReporting(3);

			final ProductCategory category = ProductCategory.fromProductFamily(product.getFamily());
			reporting.reportStart("Start publishing file " + message.getBody().getProductName() + " in topic");
			try {
				client.publish(result, category);
				reporting.reportStop("End publishing file " + message.getBody().getProductName() + " in topic");
			} catch (AbstractCodedException e) {
				reporting.reportError("[code {}] {}", e.getCode().getCode(), e.getLogMessage());
			}
		}
	}

	final void delete(final IngestionDto ingestion, final Reporting.Factory reportingFactory)
			throws InternalErrorException, InterruptedException {
		final File file = Paths.get(ingestion.getPickupPath(), ingestion.getRelativePath()).toFile();
		if (file.exists()) {
			final Reporting reporting = reportingFactory.newReporting(2);
			reporting.reportStart("Start removing file " + file.getPath());

			FileUtils.deleteWithRetries(file, properties.getMaxRetries(), properties.getTempoRetryMs());
		}
	}

	final ProductFamily getFamilyFor(final IngestionDto dto) throws ProductException {
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
