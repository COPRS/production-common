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

import esa.s1pdgs.cpoc.common.ResumeDetails;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class MqiPublicationError extends AbstractCodedException {

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
    public MqiPublicationError(final String topic, final Object dto,
            final String productName, final String message,
            final Throwable cause) {
        super(ErrorCode.MQI_PUBLICATION_ERROR, message, cause);
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
