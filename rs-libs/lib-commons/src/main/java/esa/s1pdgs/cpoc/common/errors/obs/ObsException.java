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

package esa.s1pdgs.cpoc.common.errors.obs;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * Exception concerning the object storage
 * 
 * @author Viveris Technologies
 */
public class ObsException extends AbstractCodedException {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -3680895691846942569L;

    /**
     * Key in object storage
     */
    private final String key;

    /**
     * Family
     */
    private final ProductFamily family;

    /**
     */
    public ObsException(final ProductFamily family, final String key,
            final Throwable cause) {
        this(ErrorCode.OBS_ERROR, family, key, cause.getMessage(), cause);
    }

    /**
     */
    protected ObsException(final ErrorCode error, final ProductFamily family,
            final String key, final String message, final Throwable cause) {
        super(error, message, cause);
        this.key = key;
        this.family = family;
    }

    /**
     */
    public ObsException(final ErrorCode error, final ProductFamily family,
            final String key, final String message) {
        super(error, message);
        this.key = key;
        this.family = family;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the family
     */
    public ProductFamily getFamily() {
        return family;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[family %s] [key %s] [msg %s]", family, key,
                getMessage());
    }
}
