package esa.s1pdgs.cpoc.mqi.client;

import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

@FunctionalInterface
public interface MqiListener<E> {
	MqiMessageEventHandler onMessage(GenericMessageDto<E> message) throws Exception;
	
	default void onTerminalError(final GenericMessageDto<E> message, final Exception error) {
		// by default, do nothing. services that need some error message creation can implement this method
	}
	
	default void onWarning(final GenericMessageDto<E> message, final String warningMessage) {
		// by default, do nothing. services that need some warning message creation can implement this method
	}
}
