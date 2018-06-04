package fr.viveris.s1pdgs.ingestor.kafka.exceptions;

import fr.viveris.s1pdgs.ingestor.exceptions.FileTerminatedException;

public class KafkaAuxFilesPublicationException extends FileTerminatedException {

	private static final long serialVersionUID = -7410398897010397040L;

	private static final String MESSAGE = "Auxiliary file not published in KAFKA topic";

	public KafkaAuxFilesPublicationException(String productName, Throwable cause) {
		super(ErrorCode.KAFKA_SEND_ERROR, productName, MESSAGE + ": " + cause.getMessage(), cause);
	}

	@Override
	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}
}
