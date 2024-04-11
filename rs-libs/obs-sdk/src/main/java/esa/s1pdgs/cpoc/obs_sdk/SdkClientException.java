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
 * Base type for all client exceptions thrown by the SDK. This exception is
 * thrown when service could not be contacted for a response, or when client is
 * unable to parse the response from service.
 * 
 * @author Viveris Technologies
 */
public class SdkClientException extends Exception {

    /**
     * Serial version
     */
    private static final long serialVersionUID = -6812572862277601718L;

    /**
     * @see java.lang.Exception#Exception(String)
     */
    public SdkClientException(final String message) {
        super(message);
    }

    /**
     * @see java.lang.Exception#Exception(String, Throwable)
     */
    public SdkClientException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
