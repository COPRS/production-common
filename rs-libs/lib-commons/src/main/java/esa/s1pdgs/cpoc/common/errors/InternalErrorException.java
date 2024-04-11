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

package esa.s1pdgs.cpoc.common.errors;

/**
 * Exception occurred during job generation
 * 
 * @author Cyrielle Gailliard
 */
public class InternalErrorException extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = -7488001919910076897L;

    /**
     * @param message
     */
    public InternalErrorException(final String message) {
        super(ErrorCode.INTERNAL_ERROR, message);
    }

    /**
     * @param message
     * @param e
     */
    public InternalErrorException(final String message, final Throwable cause) {
        super(ErrorCode.INTERNAL_ERROR, message, cause);
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[msg %s]", getMessage());
    }

}
