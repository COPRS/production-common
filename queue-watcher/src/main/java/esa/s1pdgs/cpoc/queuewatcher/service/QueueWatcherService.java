package esa.s1pdgs.cpoc.queuewatcher.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.queuewatcher.config.ApplicationProperties;

@Service
public class QueueWatcherService {
	
	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(QueueWatcherService.class);
	
	/**
	 * MQI service for reading message ProductDto
	 */
	private final GenericMqiClient service;


	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";
	
	@Autowired
	private ApplicationProperties properties;
   
	@Autowired
	public QueueWatcherService(final GenericMqiClient service) {
		this.service = service;
	}
		
	@Scheduled(fixedDelayString = "${file.product-categories.compressed-products.fixed-delay-ms}", initialDelayString = "${file.product-categories.compressed-products.init-delay-poll-ms}")
	public void watchCompressionQueue() {
		consume(ProductCategory.COMPRESSED_PRODUCTS);
	}	

	@Scheduled(fixedDelayString = "${file.product-categories.auxiliary-files.fixed-delay-ms}", initialDelayString = "${file.product-categories.auxiliary-files.init-delay-poll-ms}")
	public void watchAuxIngestionQueue() {
		consume(ProductCategory.AUXILIARY_FILES);
	}	

	@Scheduled(fixedDelayString = "${file.product-categories.level-products.fixed-delay-ms}", initialDelayString = "${file.product-categories.level-products.init-delay-poll-ms}")
	public void watchLevelProductQueue() {
		consume(ProductCategory.LEVEL_PRODUCTS);
	}	

	@Scheduled(fixedDelayString = "${file.product-categories.level-segments.fixed-delay-ms}", initialDelayString = "${file.product-categories.level-segments.init-delay-poll-ms}")
	public void watchLevelSegmentsQueue() {
		consume(ProductCategory.LEVEL_SEGMENTS);
	}	

	synchronized protected void writeCSV(String dateTimeStamp, String productName) throws IOException {
		CSVPrinter csvPrinter = null;
		FileWriter writer = null;
		try {			
			File csvFile = new File(properties.getCsvFile());
			if (csvFile.exists()) {
				writer = new FileWriter(csvFile, true);
				csvPrinter = new CSVPrinter(writer,
						CSVFormat.DEFAULT.withDelimiter(','));
			} else {
				writer = new FileWriter(csvFile, false);
				csvPrinter = new CSVPrinter(writer,
						CSVFormat.DEFAULT.withHeader("timestamp", "file name")
								.withDelimiter(','));
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
	

	/**
	 * Consume message for category and log
	 */
	private final void consume(final ProductCategory category)
	{
		GenericMessageDto<ProductDto> message = null;
		try {
			message = this.service.next(category);
			 if (message == null || message.getBody() == null) {
		            LOGGER.trace(" No message received: continue");
		            return;
		        }
			LOGGER.info("reveived {}: {}", category, message.getBody().getProductName());
			String timeStamp = new SimpleDateFormat(DATE_FORMAT).format(new Date());
			writeCSV(timeStamp,  message.getBody().getProductName());
			this.service.ack(new AckMessageDto(message.getIdentifier(), Ack.OK, null, true), category);

		} catch (AbstractCodedException ace) {
			LOGGER.error("Error Code: {}, Message: {}", ace.getCode().getCode(),
					ace.getLogMessage());
		} catch (Exception e) {
			LOGGER.error("Error occured while writing to CSV {}",e.getMessage());
			 //this.mqiServiceForCompressedProducts.ack(new AckMessageDto(message.getIdentifier(), Ack.ERROR, null, false));
		}
		return;
	}

}

