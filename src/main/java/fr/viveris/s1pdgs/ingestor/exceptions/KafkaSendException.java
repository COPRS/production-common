package fr.viveris.s1pdgs.ingestor.exceptions;

public class KafkaSendException extends FileTerminatedException {

	private static final long serialVersionUID = 8248616873024871315L;

	/**
	 * Name of the topic
	 */
	private final String topic;

	/**
	 * DTO object
	 */
	private final Object dto;

	/**
	 * Constructor
	 * 
	 * @param topic
	 * @param productName
	 * @param message
	 * @param e
	 */
	public KafkaSendException(final String topic, final Object dto, final String productName, final String message,
			final Throwable exc) {
		super(ErrorCode.KAFKA_SEND_ERROR, productName, message, exc);
		this.topic = topic;
		this.dto = dto;
	}

	/**
	 * @return the topic
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * @return the dto
	 */
	public Object getDto() {
		return dto;
	}

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[topic %s] [dto %s] [productName %s] [msg %s]", this.topic, this.dto, this.productName,
				getMessage());
	}

}
