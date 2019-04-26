package esa.s1pdgs.cpoc.ingestor.kafka;

import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublicationError;

public interface PublicationServices<T> {

	public void send(T obj) throws MqiPublicationError;
}
