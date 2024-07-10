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

package esa.s1pdgs.cpoc.common.errors.k8s;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class K8sEntityException extends AbstractCodedException {

    /**
     * UUID
     */
    private static final long serialVersionUID = 3960118050558022364L;

    /**
     * @param code
     * @param message
     */
    public K8sEntityException(final ErrorCode code, final String message) {
        super(code, message);
    }

    /**
     * @param code
     * @param message
     * @param e
     */
    public K8sEntityException(final ErrorCode code, final String message,
            final Throwable cause) {
        super(code, message, cause);
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[msg %s]", getMessage());
    }

}
