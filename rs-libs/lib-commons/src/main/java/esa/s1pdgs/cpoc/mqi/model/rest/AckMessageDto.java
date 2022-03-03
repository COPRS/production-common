package esa.s1pdgs.cpoc.mqi.model.rest;

import java.util.Objects;

/**
 * Exchange message for ack API
 * 
 * @author Viveris Technologies
 */
public class AckMessageDto {

    /**
     * Message identifier
     */
    private long messageId;

    /**
     * Acknowledgment
     */
    private Ack ack;

    /**
     * Message to publish in case of error
     */
    private String message;

    /**
     * True indicates that the consuming application will be stopped so the MQI
     * shall be stopped to
     */
    private boolean stop;

    /**
     * 
     */
    public AckMessageDto() {
        messageId = 0;
    }

    /**
     * @param messageId
     * @param ack
     * @param message
     */
    public AckMessageDto(final long messageId, final Ack ack,
            final String message, final boolean stop) {
        super();
        this.messageId = messageId;
        this.ack = ack;
        this.message = message;
        this.stop = stop;
    }

    /**
     * @return the messageId
     */
    public long getMessageId() {
        return messageId;
    }

    /**
     * @param messageId
     *            the messageId to set
     */
    public void setMessageId(final long messageId) {
        this.messageId = messageId;
    }

    /**
     * @return the ack
     */
    public Ack getAck() {
        return ack;
    }

    /**
     * @param ack
     *            the ack to set
     */
    public void setAck(final Ack ack) {
        this.ack = ack;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message
     *            the message to set
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * @return the stop
     */
    public boolean isStop() {
        return stop;
    }

    /**
     * @param stop
     *            the stop to set
     */
    public void setStop(final boolean stop) {
        this.stop = stop;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{messageId: %s, ack: %s, message: %s, stop: %s}",
                messageId, ack, message, stop);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(messageId, ack, message, stop);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            AckMessageDto other = (AckMessageDto) obj;
            ret = messageId == other.messageId && Objects.equals(ack, other.ack)
                    && Objects.equals(message, other.message)
                    && stop == other.stop;
        }
        return ret;
    }
}
