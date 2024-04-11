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

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class MqiPublishErrorException extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 7444307610324617302L;

    /**
     * Output
     */
    private final String errorMessage;

    /**
     * @param category
     * @param message
     */
    public MqiPublishErrorException(final String errorMessage,
            final String message) {
        super(ErrorCode.MQI_PUBLISH_ERROR, message);
        this.errorMessage = errorMessage;
    }

    /**
     * @param category
     * @param message
     * @param cause
     */
    public MqiPublishErrorException(final String errorMessage,
            final String message, final Throwable cause) {
        super(ErrorCode.MQI_PUBLISH_ERROR, message, cause);
        this.errorMessage = errorMessage;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[errorMessage %s] [msg %s]", errorMessage,
                getMessage());
    }

}
