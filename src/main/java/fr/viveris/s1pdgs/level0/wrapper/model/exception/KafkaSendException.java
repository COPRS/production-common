package fr.viveris.s1pdgs.level0.wrapper.model.exception;

public class KafkaSendException extends CodedException {

	private static final long serialVersionUID = 8248616873024871315L;
	
	private String topic;
	
	private String productName;

	public KafkaSendException(String topic, String productName, String message, Throwable e) {
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

	public String getLogMessage() {
		return String.format("[topic %s] [productName %s] [msg %s]", this.topic, this.productName, getMessage());
	}

}
