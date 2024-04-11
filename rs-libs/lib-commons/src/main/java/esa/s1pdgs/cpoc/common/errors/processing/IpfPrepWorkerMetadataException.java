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
public class IpfPrepWorkerMetadataException extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 126226117695671374L;

    /**
     * @param category
     * @param message
     */
    public IpfPrepWorkerMetadataException(final String message) {
        super(ErrorCode.JOB_GEN_METADATA_ERROR, message);
    }

    /**
     * @param category
     * @param message
     * @param cause
     */
    public IpfPrepWorkerMetadataException(final String message, final Throwable cause) {
        super(ErrorCode.JOB_GEN_METADATA_ERROR, message, cause);
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[msg %s]", getMessage());
    }

}
