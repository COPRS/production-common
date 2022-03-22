package esa.s1pdgs.cpoc.queuewatcher.service;

import static java.lang.String.format;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.NullMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.queuewatcher.config.ApplicationProperties;

@Service
public class QueueWatcherService implements MqiListener<ProductionEvent> {

	private static final Logger LOGGER = LogManager.getLogger(QueueWatcherService.class);

	@Autowired
	private GenericMqiClient mqiClient;
	
	@Autowired
	private List<MessageFilter> messageFilter;

	@Autowired
	private ApplicationProperties properties;

	private final long auxFilesPollingIntervalMs;
	private final long levelProductsPollingIntervalMs;
	private final long levelSegmentsPollingIntervalMs;
	
	private final long auxFilesPollingInitialDelayMs;
	private final long levelProductsPollingInitialDelayMs;
	private final long levelSegmentsPollingInitialDelayMs;
	
	private final AppStatus appStatus;

	@Autowired
	public QueueWatcherService(
			@Value("${file.product-categories.auxiliary-files.fixed-delay-ms}") final long auxFilesPollingIntervalMs,
			@Value("${file.product-categories.level-products.fixed-delay-ms}") final long levelProductsPollingIntervalMs,
			@Value("${file.product-categories.level-segments.fixed-delay-ms}") final long levelSegmentsPollingIntervalMs,
			@Value("${file.product-categories.auxiliary-files.init-delay-poll-ms}") final long auxFilesPollingInitialDelayMs,
			@Value("${file.product-categories.level-products.init-delay-poll-ms}") final long levelProductsPollingInitialDelayMs,
			@Value("${file.product-categories.level-segments.init-delay-poll-ms}") final long levelSegmentsPollingInitialDelayMs,
			final AppStatus appStatus
	) {
		this.auxFilesPollingIntervalMs = auxFilesPollingIntervalMs;
		this.levelProductsPollingIntervalMs = levelProductsPollingIntervalMs;
		this.levelSegmentsPollingIntervalMs = levelSegmentsPollingIntervalMs;

		this.auxFilesPollingInitialDelayMs = auxFilesPollingInitialDelayMs;
		this.levelProductsPollingInitialDelayMs = levelProductsPollingInitialDelayMs;
		this.levelSegmentsPollingInitialDelayMs = levelSegmentsPollingInitialDelayMs;
		this.appStatus = appStatus;
	}

	@PostConstruct
	public void initService() {

		File kafkaFolder = new File(properties.getKafkaFolder());
		kafkaFolder.mkdir();
		if(!kafkaFolder.exists()) {
			LOGGER.error("kafka folder {} has not been created", properties.getKafkaFolder());
			throw new RuntimeException(format("kafka folder %s has not been created", properties.getKafkaFolder()));
		}

		final ExecutorService service = Executors.newFixedThreadPool(4);
		if (auxFilesPollingIntervalMs > 0) {
			service.execute(newConsumerFor(
					ProductCategory.AUXILIARY_FILES, 
					auxFilesPollingIntervalMs, 
					auxFilesPollingInitialDelayMs
			));
		}
		if (levelProductsPollingIntervalMs > 0) {
			service.execute(newConsumerFor(
					ProductCategory.LEVEL_PRODUCTS, 
					levelProductsPollingIntervalMs, 
					levelProductsPollingInitialDelayMs
			));
		}
		if (levelSegmentsPollingIntervalMs > 0) {
			service.execute(newConsumerFor(
					ProductCategory.LEVEL_SEGMENTS, 
					levelSegmentsPollingIntervalMs, 
					levelSegmentsPollingInitialDelayMs
			));
		}
	}
	


	protected synchronized void writeCSV(final String dateTimeStamp, final String productName) throws IOException {
		CSVPrinter csvPrinter = null;
		FileWriter writer = null;
		try {
			final File csvFile = new File(properties.getCsvFile());
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
	public MqiMessageEventHandler onMessage(final GenericMessageDto<ProductionEvent> message) {
		final ProductionEvent product = message.getBody();
		final String productName = product.getKeyObjectStorage();
		final ProductCategory category = ProductCategory.of(product.getProductFamily());
		
		return new MqiMessageEventHandler.Builder<NullMessage>(ProductCategory.UNDEFINED)
				.publishMessageProducer(() -> handleMessage(productName, category))
				.newResult();
	}

	private MqiPublishingJob<NullMessage> handleMessage(
			final String productName, 
			final ProductCategory category
	) {
		LOGGER.info("received {}: {}", category, productName);
		try {
			writeCSV(DateUtils.formatToMetadataDateTimeFormat(LocalDateTime.now()), productName);
		} catch (final IOException e) {
			LOGGER.error("Error occured while writing to CSV {}", LogUtils.toString(e));
		}
		return new MqiPublishingJob<>(Collections.emptyList());
	}
	
	private MqiConsumer<ProductionEvent> newConsumerFor(final ProductCategory category, final long interval, final long delay) {
		return new MqiConsumer<>(
				mqiClient,
				category,
				this,
				messageFilter,
				interval,
				delay,
				appStatus
		);
	}

}
