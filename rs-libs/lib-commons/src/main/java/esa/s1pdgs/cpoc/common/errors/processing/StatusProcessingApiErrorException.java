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

package esa.s1pdgs.cpoc.common.errors.processing;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class StatusProcessingApiErrorException extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 8096417675612082535L;

    /**
     * URI
     */
    private final String uri;

    /**
     * @param uri
     * @param message
     */
    public StatusProcessingApiErrorException(final String uri, final String message) {
        super(ErrorCode.STATUS_PROCESSING_API_ERROR, message);
        this.uri = uri;
    }

    /**
     * @param uri
     * @param message
     * @param cause
     */
    public StatusProcessingApiErrorException(final String uri, final String message,
            final Throwable cause) {
        super(ErrorCode.STATUS_PROCESSING_API_ERROR, message, cause);
        this.uri = uri;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[uri %s] [msg %s]", uri, getMessage());
    }

}
