package esa.s1pdgs.cpoc.mqi.model.rest;

import java.util.Objects;

/**
 * Exchanged message for consumption
 * 
 * @author Viveris Technologies
 * @param <T>
 *            the message
 */
public class GenericMessageDto<T> {

    /**
     * Message identifier
     */
    private long identifier;

    /**
     * Input key
     */
    private String inputKey;

    /**
     * Message body
     */
    private T body;

    /**
     * Default constructor
     */
    public GenericMessageDto() {
        super();
    }

    /**
     * Constructor using fields
     * 
     * @param identifier
     * @param body
     */
    public GenericMessageDto(final long identifier, final String inputKey,
            final T body) {
        this();
        this.identifier = identifier;
        this.inputKey = inputKey;
        this.body = body;
    }

    /**
     * @return the identifier
     */
    public long getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier
     *            the identifier to set
     */
    public void setIdentifier(final long identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the inputKey
     */
    public String getInputKey() {
        return inputKey;
    }

    /**
     * @param inputKey
     *            the inputKey to set
     */
    public void setInputKey(final String inputKey) {
        this.inputKey = inputKey;
    }

    /**
     * @return the body
     */
    public T getBody() {
        return body;
    }

    /**
     * @param body
     *            the body to set
     */
    public void setBody(final T body) {
        this.body = body;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{identifier: %s, inputKey: %s, body: %s}",
                identifier, inputKey, body);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(identifier, inputKey, body);
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
            GenericMessageDto<?> other = (GenericMessageDto<?>) obj;
            ret = identifier == other.identifier
                    && Objects.equals(inputKey, other.inputKey)
                    && Objects.equals(body, other.body);
        }
        return ret;
    }
}