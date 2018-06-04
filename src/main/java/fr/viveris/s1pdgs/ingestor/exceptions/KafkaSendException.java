package fr.viveris.s1pdgs.ingestor.exceptions;

public class KafkaSendException extends FileTerminatedException {

	private static final long serialVersionUID = 8248616873024871315L;

	/**
	 * Name of the topic
	 */
	private final String topic;

	/**
	 * Constructor
	 * 
	 * @param topic
	 * @param productName
	 * @param message
	 * @param e
	 */
	public KafkaSendException(final String topic, final String productName, final String message, final Throwable e) {
		super(ErrorCode.KAFKA_SEND_ERROR, productName, message, e);
		this.topic = topic;
	}

	/**
	 * @return the topic
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[topic %s] [productName %s] [msg %s]", this.topic, this.productName, getMessage());
	}

}
