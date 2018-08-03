package esa.s1pdgs.cpoc.ingestor.kafka;

import esa.s1pdgs.cpoc.ingestor.exceptions.KafkaSendException;

public interface PublicationServices<T> {

	public void send(T obj) throws KafkaSendException;
}
