package esa.s1pdgs.cpoc.queuewatcher.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mqi.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.queuewatcher.config.ApplicationProperties;

@Service
public class QueueWatcherService implements MqiListener<ProductDto> {

	private static final Logger LOGGER = LogManager.getLogger(QueueWatcherService.class);

	/**
	 * MQI service for reading message ProductDto
	 */
	@Autowired
	private GenericMqiClient mqiClient;

	@Autowired
	private ApplicationProperties properties;

	private final long auxFilesPollingIntervalMs;

	private final long levelProductsPollingIntervalMs;

	private final long levelSegmentsPollingIntervalMs;

	private final long compressedProductsPollingIntervalMs;

	@Autowired
	public QueueWatcherService(
			@Value("${file.product-categories.auxiliary-files.fixed-delay-ms}") final long auxFilesPollingIntervalMs,
			@Value("${file.product-categories.level-products.fixed-delay-ms}") final long levelProductsPollingIntervalMs,
			@Value("${file.product-categories.level-segments.fixed-delay-ms}") final long levelSegmentsPollingIntervalMs,
			@Value("${file.product-categories.compressed-products.fixed-delay-ms}") final long compressedProductsPollingIntervalMs) {
		this.auxFilesPollingIntervalMs = auxFilesPollingIntervalMs;
		this.levelProductsPollingIntervalMs = levelProductsPollingIntervalMs;
		this.levelSegmentsPollingIntervalMs = levelSegmentsPollingIntervalMs;
		this.compressedProductsPollingIntervalMs = compressedProductsPollingIntervalMs;
	}

	@PostConstruct
	public void initService() {

		final ExecutorService service = Executors.newFixedThreadPool(4);
		if (auxFilesPollingIntervalMs > 0) {
			service.execute(new MqiConsumer<ProductDto>(mqiClient, ProductCategory.AUXILIARY_FILES, this,
					auxFilesPollingIntervalMs));
		}
		if (levelProductsPollingIntervalMs > 0) {
			service.execute(new MqiConsumer<ProductDto>(mqiClient, ProductCategory.LEVEL_PRODUCTS, this,
					levelProductsPollingIntervalMs));
		}
		if (levelSegmentsPollingIntervalMs > 0) {
			service.execute(new MqiConsumer<ProductDto>(mqiClient, ProductCategory.LEVEL_SEGMENTS, this,
					levelSegmentsPollingIntervalMs));
		}
		if (compressedProductsPollingIntervalMs > 0) {
			service.execute(new MqiConsumer<ProductDto>(mqiClient, ProductCategory.COMPRESSED_PRODUCTS, this,
					compressedProductsPollingIntervalMs));
		}
		// Seems to be not relevant for EDRS_SESSIONS
	}

	protected synchronized void writeCSV(String dateTimeStamp, String productName) throws IOException {
		CSVPrinter csvPrinter = null;
		FileWriter writer = null;
		try {
			File csvFile = new File(properties.getCsvFile());
			if (csvFile.exists()) {
				writer = new FileWriter(csvFile, true);
				csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withDelimiter(','));
			} else {
				writer = new FileWriter(csvFile, false);
				csvPrinter = new CSVPrinter(writer,
						CSVFormat.DEFAULT.withHeader("timestamp", "file name").withDelimiter(','));
			}

			csvPrinter.printRecord(dateTimeStamp, productName);
			csvPrinter.flush();
		} finally {
			if (writer != null) {
				writer.close();
			}
			if (csvPrinter != null) {
				csvPrinter.close();
			}

		}
	}

	@Override
	public void onMessage(GenericMessageDto<ProductDto> message) {
		final ProductDto product = message.getBody();
		String productName = product.getProductName();
		ProductCategory category = ProductCategory.of(product.getFamily());

		LOGGER.info("received {}: {}", category, productName);

		try {
			writeCSV(DateUtils.formatToMetadataDateTimeFormat(LocalDateTime.now()), productName);
		} catch (IOException e) {
			LOGGER.error("Error occured while writing to CSV {}", LogUtils.toString(e));
		}
	}

}
