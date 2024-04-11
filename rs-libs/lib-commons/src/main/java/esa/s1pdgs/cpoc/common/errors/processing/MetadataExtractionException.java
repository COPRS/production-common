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
 * @author Viveris Technlogies
 */
public class MetadataExtractionException extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 2134771514034032034L;

    /**
     * @param cause
     */
    public MetadataExtractionException(final Throwable cause) {
        super(ErrorCode.METADATA_EXTRACTION_ERROR, cause.getMessage(), cause);
    }
    
    /**
     * @param message
     */
    public MetadataExtractionException(final String message) {
    	super(ErrorCode.METADATA_EXTRACTION_ERROR, message);
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[msg %s]", getMessage());
    }

}
