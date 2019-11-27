package esa.s1pdgs.cpoc.mqi.client;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

@FunctionalInterface
public interface MqiListener<E> {
	void onMessage(GenericMessageDto<E> message) throws AbstractCodedException;
}
