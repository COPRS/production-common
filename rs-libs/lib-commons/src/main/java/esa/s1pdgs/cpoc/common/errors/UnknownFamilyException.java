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
 * @author Viveris Technologies
 */
public class UnknownFamilyException extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 9140236445794096614L;

    /**
     * Invalid family
     */
    private final String family;

    /**
     * @param message
     * @param family
     */
    public UnknownFamilyException(final String family, final String message) {
        super(ErrorCode.UNKNOWN_FAMILY, message);
        this.family = family;
    }

    /**
     * @return the family
     */
    public String getFamily() {
        return family;
    }

    /**
     * @see AbstractCodedException#getLogMessage()
     */
    @Override
    public String getLogMessage() {
        return String.format("[family %s] [msg %s]", family, getMessage());
    }

}
