package fr.viveris.s1pdgs.ingestor.kafka;

import fr.viveris.s1pdgs.ingestor.exceptions.KafkaSendException;

public interface PublicationServices<T> {

	public void send(T obj) throws KafkaSendException;
}
