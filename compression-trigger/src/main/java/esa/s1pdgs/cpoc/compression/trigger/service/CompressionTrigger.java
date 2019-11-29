package esa.s1pdgs.cpoc.compression.trigger.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;

@Service
public class CompressionTrigger {
	private static final Logger LOGGER = LogManager.getLogger(CompressionTrigger.class);
	
	private static final String SUFFIX_ZIPPRODUCTFAMILY = "_ZIP";
	private static final String SUFFIX_ZIPPPRODUCTFILE = ".zip";

	private final GenericMqiClient mqiClient;

	@Autowired
	public CompressionTrigger(final GenericMqiClient mqiClient) {
		this.mqiClient = mqiClient;
	}

	public void trigger(GenericMessageDto<ProductionEvent> message) throws AbstractCodedException {		
		ProductionEvent productionEvent = message.getBody();
		
		LOGGER.info("Received new production event for compression: {}",productionEvent);

		final GenericPublicationMessageDto<CompressionJob> outputMessage = new GenericPublicationMessageDto<CompressionJob>(
				message.getId(), productionEvent.getProductFamily(), productionEventToCompressionJob(productionEvent));
		this.mqiClient.publish(outputMessage, ProductCategory.COMPRESSED_PRODUCTS);
	}

	private CompressionJob productionEventToCompressionJob(ProductionEvent productionEvent) {

		return new CompressionJob(productionEvent.getKeyObjectStorage(), productionEvent.getProductFamily(),
				getCompressedKeyObjectStorage(productionEvent.getKeyObjectStorage()),
				getCompressedProductFamily(productionEvent.getProductFamily()));
	}

	String getCompressedKeyObjectStorage(String inputKeyObjectStorage) {
		return inputKeyObjectStorage + SUFFIX_ZIPPPRODUCTFILE;
	}

	ProductFamily getCompressedProductFamily(ProductFamily inputFamily) {
		return ProductFamily.fromValue(inputFamily.toString() + SUFFIX_ZIPPRODUCTFAMILY);
	}

}
