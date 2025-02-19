/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.mqi.model.rest;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Message to send to the MQI for publishing a message
 * 
 * @author Viveris Technologies
 * @param <T>
 */
public class GenericPublicationMessageDto<T> implements MessageDto<T> {

    /**
     * Identifier of the input message if exists
     */
    private long inputMessageId;

    /**
     * Key of the input (if exists) to use for routing the message
     */
    private String inputKey;

    /**
     * Family of the output
     */
    private ProductFamily family;

    /**
     * Key of the output to use for routing the message
     */
    private String outputKey;

    /**
     * Message to publish
     */
    private T messageToPublish;

    /**
     * Default constructor
     */
    public GenericPublicationMessageDto() {
        family = ProductFamily.BLANK;
    }

    /**
     * Constructor
     */
    public GenericPublicationMessageDto(final ProductFamily family,
            final T messageToPublish) {
        this.family = family;
        this.messageToPublish = messageToPublish;
        this.inputMessageId = 0;
    }

    /**
     * Constructor
     */
    public GenericPublicationMessageDto(final long inputMessageId,
            final ProductFamily family, final T messageToPublish) {
        this.family = family;
        this.messageToPublish = messageToPublish;
        this.inputMessageId = inputMessageId;
    }
    
    @Override
    @JsonIgnore
	public T getDto() {
		return messageToPublish;
	}

	/**
     * @return the inputMessageId
     */
    public long getInputMessageId() {
        return inputMessageId;
    }

    /**
     * @param inputMessageId
     *            the inputMessageId to set
     */
    public void setInputMessageId(final long inputMessageId) {
        this.inputMessageId = inputMessageId;
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
     * @return the family
     */
    public ProductFamily getFamily() {
        return family;
    }

    /**
     * @param family
     *            the family to set
     */
    public void setFamily(final ProductFamily family) {
        this.family = family;
    }

    /**
     * @return the outputKey
     */
    public String getOutputKey() {
        return outputKey;
    }

    /**
     * @param outputKey
     *            the outputKey to set
     */
    public void setOutputKey(final String outputKey) {
        this.outputKey = outputKey;
    }

    /**
     * @return the messageToPublish
     */
    public T getMessageToPublish() {
        return messageToPublish;
    }

    /**
     * @param messageToPublish
     *            the messageToPublish to set
     */
    public void setMessageToPublish(final T messageToPublish) {
        this.messageToPublish = messageToPublish;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(
                "{inputMessageId: %s, inputKey: %s, family: %s, outputKey: %s, messageToPublish: %s}",
                inputMessageId, inputKey, family, outputKey, messageToPublish);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(inputMessageId, inputKey, family, outputKey,
                messageToPublish);
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
            final GenericPublicationMessageDto<?> other =
                    (GenericPublicationMessageDto<?>) obj;
            ret = inputMessageId == other.inputMessageId
                    && Objects.equals(inputKey, other.inputKey)
                    && Objects.equals(family, other.family)
                    && Objects.equals(outputKey, other.outputKey)
                    && Objects.equals(messageToPublish, other.messageToPublish);
        }
        return ret;
    }
}
