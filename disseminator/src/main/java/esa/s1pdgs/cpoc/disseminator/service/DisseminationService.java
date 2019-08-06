package esa.s1pdgs.cpoc.disseminator.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

@Service
public class DisseminationService {
	private static final Logger LOGGER = LogManager.getLogger(DisseminationService.class);
	private final GenericMqiClient client;
	
	@Autowired
	private final DisseminationProperties properties;
   
	@Autowired
	public DisseminationService(
			final GenericMqiClient client,
			final DisseminationProperties properties
	) {
		this.client = client;
		this.properties = properties;
	}
	
	public void poll() {
//		try {
//			final GenericMessageDto<IngestionDto> message = client.next(ProductCategory.);		
//			if (message == null || message.getBody() == null) {
//				LOG.trace("No message received: continue");
//				return;
//			}
//			
//			AckMessageDto ackMess;			
//			try {
//				onMessage(message);
//				ackMess = new AckMessageDto(message.getIdentifier(), Ack.OK, null, false);
//			// any other error --> dump prominently into log file but continue	
//			} catch (Exception e) {
//				LOG.error("Unexpected Error on Ingestion", e);
//				ackMess = new AckMessageDto(message.getIdentifier(), Ack.ERROR, LogUtils.toString(e), false);
//			}			
//			client.ack(ackMess, ProductCategory.INGESTION);
//		// on communication errors with Mqi --> just dump warning and retry on next polling attempt
//		} catch (AbstractCodedException ace) {
//			LOG.warn("Error Code: {}, Message: {}", ace.getCode().getCode(), ace.getLogMessage());			
//		} 
		
	}


}

