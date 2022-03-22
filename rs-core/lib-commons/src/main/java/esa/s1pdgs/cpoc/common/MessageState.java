package esa.s1pdgs.cpoc.common;

import esa.s1pdgs.cpoc.mqi.model.rest.Ack;

/**
 * Available states of a MQI message:
 * <li>READ: message read by a KAFKA coonsumer</li>
 * <li>SEND: message send to a application by a MQI server</li>
 * <li>ACK_OK: message successfully processing by an application</li>
 * <li>ACK_KO: message processing by an application but error occurred</li>
 */
public enum MessageState {
    READ, SEND, ACK_OK, ACK_KO, ACK_WARN;
	
	public static MessageState of(Ack ack) {  
		switch (ack) {
			case OK:
				return ACK_OK;
			case ERROR:
				return ACK_KO;
			case WARN:
				return ACK_WARN;
			default:
				throw new IllegalArgumentException(String.format("Invalid Ack %s", ack));
		}
	}
}
