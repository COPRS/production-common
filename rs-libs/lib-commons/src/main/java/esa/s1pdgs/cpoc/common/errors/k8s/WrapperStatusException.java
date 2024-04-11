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

/**
 * @author Viveris Technologies
 */
public class WrapperStatusException extends K8sEntityException {

    /**
     * UUID
     */
    private static final long serialVersionUID = -1655370514373140620L;

    /**
     * IP address
     */
    private final String ipAddress;

    /**
     * Name
     */
    private final String name;

    /**
     * @param ipAddress
     * @param name
     * @param message
     */
    public WrapperStatusException(final String ipAddress, final String name,
            final String message) {
        super(ErrorCode.K8S_WRAPPER_STATUS_ERROR, message);
        this.ipAddress = ipAddress;
        this.name = name;
    }

    /**
     * @param ipAddress
     * @param name
     * @param message
     * @param cause
     */
    public WrapperStatusException(final String ipAddress, final String name,
            final String message, final Throwable cause) {
        super(ErrorCode.K8S_WRAPPER_STATUS_ERROR, message, cause);
        this.ipAddress = ipAddress;
        this.name = name;
    }

    /**
     * @return the ipAddress
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[podIp %s] [podName %s] [msg %s]", ipAddress,
                name, getMessage());
    }

}
