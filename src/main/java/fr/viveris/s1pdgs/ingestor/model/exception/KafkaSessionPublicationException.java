package fr.viveris.s1pdgs.ingestor.model.exception;

public class KafkaSessionPublicationException extends FileTerminatedException {

	private static final long serialVersionUID = 4003353404276591615L;

	private static final String MESSAGE = "Session not published in KAFKA topic";

	public KafkaSessionPublicationException(String productName, Throwable cause) {
		super(ErrorCode.KAFKA_SEND_ERROR, productName, MESSAGE + ": " + cause.getMessage(), cause);
	}

	@Override
	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}
}
