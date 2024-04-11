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
public class MetadataMalformedException extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 2939784030412076416L;

    /**
     * Generic message
     */
    private static final String MESSAGE = "Metadata malformed";

    /**
     * Name of missing field
     */
    private final String missingField;

    /**
     * @param missingField
     */
    public MetadataMalformedException(final String missingField) {
        super(ErrorCode.METADATA_MALFORMED_ERROR, MESSAGE);
        this.missingField = missingField;
    }
    
    /**
     * @param missingField, message
     */
    public MetadataMalformedException(final String missingField, final String message) {
        super(ErrorCode.METADATA_MALFORMED_ERROR, message);
        this.missingField = missingField;
    }

    /**
     * @return the missingField
     */
    public String getMissingField() {
        return missingField;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[missingField %s] [msg %s]", missingField,
                getMessage());
    }

}
