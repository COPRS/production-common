package esa.s1pdgs.cpoc.mqi;

import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

@FunctionalInterface
public interface MqiListener<E> {
	void onMessage(GenericMessageDto<E> message);
}
