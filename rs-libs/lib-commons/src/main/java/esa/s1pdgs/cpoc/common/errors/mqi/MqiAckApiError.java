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

package esa.s1pdgs.cpoc.common.errors.mqi;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

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
