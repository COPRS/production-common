package esa.s1pdgs.cpoc.disseminator.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;

@Service
public class DisseminationService {
	private static final Logger LOGGER = LogManager.getLogger(DisseminationService.class);
	
	private final GenericMqiClient client;
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

