package fr.viveris.s1pdgs.jobgenerator.exception;

public class KafkaSendException extends AbstractCodedException {

	private static final long serialVersionUID = 8248616873024871315L;

	/**
	 * Name of the topic
	 */
	private final String topic;

	/**
	 * Name of the product
	 */
	private final String productName;

	public KafkaSendException(final String topic, final String productName, final String message, final Throwable e) {
		super(ErrorCode.KAFKA_SEND_ERROR, message, e);
		this.topic = topic;
		this.productName = productName;
	}

	/**
	 * @return the topic
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * @return the productName
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[topic %s] [productName %s] [msg %s]", this.topic, this.productName, getMessage());
	}

}
