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

package esa.s1pdgs.cpoc.common.errors.os;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class OsEntityException extends AbstractCodedException {

    private static final long serialVersionUID = 1302470342554468202L;

    /**
     * Entity identifier
     */
    private final String identifier;

    /**
     * Type of the entity
     */
    private final String type;

    /**
     * @param type
     * @param id
     * @param code
     * @param message
     */
    public OsEntityException(final String type, final String identifier,
            final ErrorCode code, final String message) {
        super(code, message);
        this.type = type;
        this.identifier = identifier;
    }

    /**
     * @param type
     * @param id
     * @param code
     * @param message
     * @param cause
     */
    public OsEntityException(final String type, final String identifier,
            final ErrorCode code, final String message, final Throwable cause) {
        super(code, message, cause);
        this.type = type;
        this.identifier = identifier;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[%s %s] [msg %s]", type, identifier,
                getMessage());
    }

    /**
     * @return the id
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

}
