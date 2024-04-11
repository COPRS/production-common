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
public class MqiPublishApiError extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 126226117695671374L;

    /**
     * Category
     */
    private final ProductCategory category;

    /**
     * Output
     */
    private final Object output;

    /**
     * @param category
     * @param message
     */
    public MqiPublishApiError(final ProductCategory category,
            final Object output, final String message) {
        super(ErrorCode.MQI_PUBLISH_API_ERROR, message);
        this.category = category;
        this.output = output;
    }

    /**
     * @param category
     * @param message
     * @param cause
     */
    public MqiPublishApiError(final ProductCategory category,
            final Object output, final String message, final Throwable cause) {
        super(ErrorCode.MQI_PUBLISH_API_ERROR, message, cause);
        this.category = category;
        this.output = output;
    }

    /**
     * @return the category
     */
    public ProductCategory getCategory() {
        return category;
    }

    /**
     * @return the output
     */
    public Object getOutput() {
        return output;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[category %s] [output %s] [msg %s]", category,
                output, getMessage());
    }

}
