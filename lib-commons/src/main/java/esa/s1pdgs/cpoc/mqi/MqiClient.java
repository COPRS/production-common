package esa.s1pdgs.cpoc.mqi;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;

public interface MqiClient {

	/**
	 * Get the next message to proceed
	 * 
	 * @return
	 * @throws AbstractCodedException
	 */
	<T> GenericMessageDto<T> next(ProductCategory category) throws AbstractCodedException;

	/**
	 * Ack a message
	 * 
	 * @param identifier
	 * @param ack
	 * @param message
	 * @return
	 * @throws AbstractCodedException
	 */
	boolean ack(AckMessageDto ack, ProductCategory category) throws AbstractCodedException;

	/**
	 * Publish a message
	 * 
	 * @param message
	 * @throws AbstractCodedException
	 */
	<E extends AbstractDto> void publish(GenericPublicationMessageDto<E> message, ProductCategory category)
			throws AbstractCodedException;

}