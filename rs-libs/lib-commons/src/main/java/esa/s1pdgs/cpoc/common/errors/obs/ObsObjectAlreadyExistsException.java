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

/**
 * Exception raised when object already exist in object storage
 */
public class ObsObjectAlreadyExistsException extends ObsException {

    /**
     * 
     */
    private static final long serialVersionUID = -5331517744542218021L;

    /**
     * Custom message
     */
    private static final String MESSAGE =
            "Object already exists in object storage";

    /**
     * @param productName
     * @param cause
     */
    public ObsObjectAlreadyExistsException(final ProductFamily family, final String key,
            final Throwable cause) {
        super(ErrorCode.OBS_ALREADY_EXIST, family, key,
                MESSAGE + ": " + cause.getMessage(), cause);
    }
}
