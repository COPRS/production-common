package esa.s1pdgs.cpoc.queuewatcher.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.AuxiliaryFileDto;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelSegmentDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

@Service
public class QueueWatcherService {
	
	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(QueueWatcherService.class);
	
	/**
	 * MQI service for reading message CompressionJobDto
	 */
	private final GenericMqiService<CompressionJobDto> mqiServiceForCompressedProducts;

	/**
	 * MQI service for reading message AuxiliaryFileDto
	 */
	private final GenericMqiService<AuxiliaryFileDto> mqiServiceForAUXProducts;

	/**
	 * MQI service for reading message LevelProductDto
	 */
	private final GenericMqiService<LevelProductDto> mqiServiceForLevelProducts;

		/**
	 * MQI service for reading message LevelSegmentDto
	 */
	private final GenericMqiService<LevelSegmentDto> mqiServiceForLevelSegments;


	@Autowired
	public QueueWatcherService(
			@Qualifier("mqiServiceForCompression") final GenericMqiService<CompressionJobDto> mqiServiceForCompressedProducts,
			@Qualifier("mqiServiceForAuxiliaryFiles") final GenericMqiService<AuxiliaryFileDto> mqiServiceForAUXProducts,
			@Qualifier("mqiServiceForLevelProducts") final GenericMqiService<LevelProductDto> mqiServiceForLevelProducts,
			@Qualifier("mqiServiceForLevelSegments") final GenericMqiService<LevelSegmentDto> mqiServiceForLevelSegments) {

		this.mqiServiceForCompressedProducts = mqiServiceForCompressedProducts;
		this.mqiServiceForAUXProducts = mqiServiceForAUXProducts;
		this.mqiServiceForLevelProducts = mqiServiceForLevelProducts;
		this.mqiServiceForLevelSegments = mqiServiceForLevelSegments;
	}

	
	
	/**
	 * Consume message and log
	 */
	@Scheduled(fixedDelayString = "${file.product-categories.compressed-products.fixed-delay-ms}", initialDelayString = "${file.product-categories.compressed-products.init-delay-poll-ms}")
	public void watchCompressionQueue() {
		
		GenericMessageDto<CompressionJobDto> message = null;
		try {
			message = this.mqiServiceForCompressedProducts.next();
			 if (message == null || message.getBody() == null) {
		            LOGGER.debug(" No message received: continue");
		            return;
		        }
			LOGGER.info("{}", message.getBody().getProductName());
			this.mqiServiceForCompressedProducts.ack(new AckMessageDto(message.getIdentifier(), Ack.OK, null, true));

		} catch (AbstractCodedException ace) {
			 LOGGER.error("[ [step 0] [{}] [code {}] {}",               ace.getCode().getCode(), ace.getLogMessage());
			 //this.mqiServiceForCompressedProducts.ack(new AckMessageDto(message.getIdentifier(), Ack.ERROR, null, false));
		}
		return;
	}
	

	/**
	 * Consume message and log
	 */
	@Scheduled(fixedDelayString = "${file.product-categories.auxiliary-files.fixed-delay-ms}", initialDelayString = "${file.product-categories.auxiliary-files.init-delay-poll-ms}")
	public void watchAuxIngestionQueue() {
		
		GenericMessageDto<AuxiliaryFileDto> message = null;
		try {
			message = this.mqiServiceForAUXProducts.next();
			 if (message == null || message.getBody() == null) {
		            LOGGER.trace(" No message received: continue");
		            return;
		        }
			 
			LOGGER.info("{}", message.getBody().getProductName());
			this.mqiServiceForAUXProducts.ack(new AckMessageDto(message.getIdentifier(), Ack.OK, null, true));
		} catch (AbstractCodedException ace) {
			 LOGGER.error("[ [step 0] [{}] [code {}] {}",               ace.getCode().getCode(), ace.getLogMessage());
			 //this.mqiServiceForCompressedProducts.ack(new AckMessageDto(message.getIdentifier(), Ack.ERROR, null, false));
		}

		return;
	}
	
	

	/**
	 * Consume message and log
	 */
	@Scheduled(fixedDelayString = "${file.product-categories.level-products.fixed-delay-ms}", initialDelayString = "${file.product-categories.level-products.init-delay-poll-ms}")
	public void watchLevelProductQueue() {
		
		GenericMessageDto<LevelProductDto> message = null;
		try {
			message = this.mqiServiceForLevelProducts.next();
			if (message == null || message.getBody() == null) {
		            LOGGER.trace(" No message received: continue");
		            return;
		        }
			 
			LOGGER.info("{}", message.getBody().getProductName());
			this.mqiServiceForLevelProducts.ack(new AckMessageDto(message.getIdentifier(), Ack.OK, null, true));
		} catch (AbstractCodedException ace) {
			 LOGGER.error("[ [step 0] [{}] [code {}] {}",               ace.getCode().getCode(), ace.getLogMessage());
			 //this.mqiServiceForCompressedProducts.ack(new AckMessageDto(message.getIdentifier(), Ack.ERROR, null, false));
		}

		return;
	}
	
	

	/**
	 * Consume message and log
	 */
	@Scheduled(fixedDelayString = "${file.product-categories.level-segments.fixed-delay-ms}", initialDelayString = "${file.product-categories.level-segments.init-delay-poll-ms}")
	public void watchLevelSegmentsQueue() {
		
		GenericMessageDto<LevelSegmentDto> message = null;
		try {
			message = this.mqiServiceForLevelSegments.next();
			
  		    if (message == null || message.getBody() == null) {
		            LOGGER.trace(" No message received: continue");
		            return;
		        }
			LOGGER.info("{}", message.getBody().getName());
			this.mqiServiceForLevelSegments.ack(new AckMessageDto(message.getIdentifier(), Ack.OK, null, true));
		} catch (AbstractCodedException ace) {
			 LOGGER.error("[ [step 0] [{}] [code {}] {}",               ace.getCode().getCode(), ace.getLogMessage());
			 //this.mqiServiceForCompressedProducts.ack(new AckMessageDto(message.getIdentifier(), Ack.ERROR, null, false));
		}
		return;
	}
	
	
}
