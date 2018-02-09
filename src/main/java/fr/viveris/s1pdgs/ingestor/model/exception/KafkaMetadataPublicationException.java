package fr.viveris.s1pdgs.ingestor.model.exception;

public class KafkaMetadataPublicationException extends FileException {

	private static final long serialVersionUID = -7410398897010397040L;

	private static final String MESSAGE = "Metadata not published for %s";

	public KafkaMetadataPublicationException(String productName) {
		super(String.format(MESSAGE, productName), productName);
	}

	public KafkaMetadataPublicationException(String productName, Throwable cause) {
		super(String.format(MESSAGE, productName), productName, cause);
	}
}
