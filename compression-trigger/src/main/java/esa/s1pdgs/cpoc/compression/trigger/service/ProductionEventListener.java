package esa.s1pdgs.cpoc.compression.trigger.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class ProductionEventListener implements MqiListener<ProductionEvent> {

	private static final Logger LOGGER = LogManager.getLogger(ProductionEventListener.class);

	private final GenericMqiClient mqiClient;

	private final AppStatus appStatus;

	private final CompressionTrigger compressionTrigger;

	@Autowired
	public ProductionEventListener(final AppStatus appStatus, final GenericMqiClient mqiClient,
			final CompressionTrigger compressionTrigger) {
		this.appStatus = appStatus;
		this.mqiClient = mqiClient;
		this.compressionTrigger = compressionTrigger;
	}

	@Override
	public void onMessage(GenericMessageDto<ProductionEvent> productionEventMessage) {

		try {
			compressionTrigger.trigger(productionEventMessage);
		} catch (AbstractCodedException ace) {
			ackNegatively(false, productionEventMessage, ace.getLogMessage());
		}

		ackPositively(false, productionEventMessage);
	}

	/**
	 * @param dto
	 * @param errorMessage
	 */
	protected void ackNegatively(final boolean stop, final GenericMessageDto<ProductionEvent> dto,
			final String errorMessage) {
		LOGGER.info("Acknowledging negatively {} ", dto.getBody());
		try {
			mqiClient.ack(new AckMessageDto(dto.getId(), Ack.ERROR, errorMessage, stop),
					ProductCategory.COMPRESSED_PRODUCTS);
		} catch (AbstractCodedException ace) {
			LOGGER.error("Unable to confirm negatively request:{}", ace);
		}
		appStatus.setError("PROCESSING");
	}

	protected void ackPositively(final boolean stop, final GenericMessageDto<ProductionEvent> dto) {
		LOGGER.info("Acknowledging positively {}", dto.getBody());
		try {
			mqiClient.ack(new AckMessageDto(dto.getId(), Ack.OK, null, stop), ProductCategory.COMPRESSED_PRODUCTS);
		} catch (AbstractCodedException ace) {
			LOGGER.error("Unable to confirm positively request:{}", ace);
			appStatus.setError("PROCESSING");
		}
	}

}
