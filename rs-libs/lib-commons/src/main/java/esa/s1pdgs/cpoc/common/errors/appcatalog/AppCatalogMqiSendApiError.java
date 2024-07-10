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

package esa.s1pdgs.cpoc.common.errors.appcatalog;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class AppCatalogMqiSendApiError extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 8096417675612082535L;

    /**
     * Category
     */
    private final ProductCategory category;

    /**
     * Force
     */
    private final String uri;

    /**
     * object
     */
    private final Object dto;

    /**
     * @param category
     * @param message
     */
    public AppCatalogMqiSendApiError(final ProductCategory category,
            final String uri, final Object dto, final String message) {
        super(ErrorCode.APPCATALOG_MQI_SEND_API_ERROR, message);
        this.category = category;
        this.uri = uri;
        this.dto = dto;
    }

    /**
     * @param category
     * @param message
     * @param cause
     */
    public AppCatalogMqiSendApiError(final ProductCategory category,
            final String uri, final Object dto, final String message,
            final Throwable cause) {
        super(ErrorCode.APPCATALOG_MQI_SEND_API_ERROR, message, cause);
        this.category = category;
        this.uri = uri;
        this.dto = dto;
    }

    /**
     * @return the category
     */
    public ProductCategory getCategory() {
        return category;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
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
        return String.format("[category %s] [uri %s] [dto %s] [msg %s]",
                category, uri, dto, getMessage());
    }

}
