package esa.s1pdgs.cpoc.appcatalog.rest;

/**
 * Available states of a MQI message:
 * <li>READ: message read by a KAFKA coonsumer</li>
 * <li>SEND: message send to a application by a MQI server</li>
 * <li>ACK_OK: message successfully processing by an application</li>
 * <li>ACK_KO: message processing by an application but error occurred</li>
 */
public enum MqiStateMessageEnum {
    READ, SEND, ACK_OK, ACK_KO, ACK_WARN;
}
