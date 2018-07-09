package fr.viveris.s1pdgs.common.errors.mqi;

import fr.viveris.s1pdgs.common.ResumeDetails;
import fr.viveris.s1pdgs.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class KafkaSendException extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 8248616873024871315L;

    /**
     * Name of the topic
     */
    private final String topic;

    /**
     * Name of the product
     */
    private final String productName;

    /**
     * DTO to publish
     */
    private final Object dto;

    /**
     * @param topic
     * @param productName
     * @param message
     * @param e
     */
    public KafkaSendException(final String topic, final Object dto,
            final String productName, final String message,
            final Throwable cause) {
        super(ErrorCode.KAFKA_SEND_ERROR, message, cause);
        this.topic = topic;
        this.productName = productName;
        this.dto = dto;
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
        return String.format("[resuming %s] [productName %s] [msg %s]",
                new ResumeDetails(topic, dto), productName, getMessage());
    }

}
