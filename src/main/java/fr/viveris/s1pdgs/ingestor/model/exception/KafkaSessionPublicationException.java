package fr.viveris.s1pdgs.ingestor.model.exception;

public class KafkaSessionPublicationException extends FileException {

	private static final long serialVersionUID = 4003353404276591615L;

	private static final String MESSAGE = "Session not published for %s";

	public KafkaSessionPublicationException(String productName) {
		super(String.format(MESSAGE, productName), productName);
	}

	public KafkaSessionPublicationException(String productName, Throwable cause) {
		super(String.format(MESSAGE, productName), productName, cause);
	}
}
