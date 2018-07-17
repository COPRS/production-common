package fr.viveris.s1pdgs.common.errors.mqi;

import fr.viveris.s1pdgs.common.ProductCategory;
import fr.viveris.s1pdgs.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class MqiAckApiError extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = -7004241828112435975L;

    /**
     * Category
     */
    private final ProductCategory category;

    /**
     * Message identifier
     */
    private final long messageId;

    /**
     * Ack message = status + error message if exists
     */
    private final String ackMessage;

    /**
     * @param category
     * @param message
     */
    public MqiAckApiError(final ProductCategory category, final long messageId,
            final String ackMessage, final String message) {
        super(ErrorCode.MQI_ACK_API_ERROR, message);
        this.category = category;
        this.messageId = messageId;
        this.ackMessage = ackMessage;
    }

    /**
     * @param category
     * @param message
     * @param cause
     */
    public MqiAckApiError(final ProductCategory category, final long messageId,
            final String ackMessage, final String message,
            final Throwable cause) {
        super(ErrorCode.MQI_ACK_API_ERROR, message, cause);
        this.category = category;
        this.messageId = messageId;
        this.ackMessage = ackMessage;
    }

    /**
     * @return the category
     */
    public ProductCategory getCategory() {
        return category;
    }

    /**
     * @return the messageId
     */
    public long getMessageId() {
        return messageId;
    }

    /**
     * @return the ackMessage
     */
    public String getAckMessage() {
        return ackMessage;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format(
                "[category %s] [messageId %d] [ackMessage %s] [msg %s]",
                category, messageId, ackMessage, getMessage());
    }

}
