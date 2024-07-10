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

package esa.s1pdgs.cpoc.obs_sdk;

/**
 * Extension of SdkClientException that represents an error response returned
 * by an Amazon web service. Receiving an exception of this type indicates that
 * the caller's request was correctly transmitted to the service, but for some
 * reason, the service was not able to process it, and returned an error
 * response instead.
 */
public class ObsServiceException extends SdkClientException {

    /**
     * Serial version
     */
    private static final long serialVersionUID = -6812572862277601718L;

    /**
     * @see java.lang.Exception#Exception(String)
     */
    public ObsServiceException(final String message) {
        super(message);
    }

    /**
     * @see java.lang.Exception#Exception(String, Throwable)
     */
    public ObsServiceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
