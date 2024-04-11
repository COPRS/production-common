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

package esa.s1pdgs.cpoc.appcatalog.rest;

import java.util.Objects;

/**
 * Object used by the applciative catalog when reading a message
 * 
 * @author Viveris Technologies
 */
public class AppCatSendMessageDto {

    /**
     * Pod
     */
    private String pod;

    /**
     * If true, the pod is the new reader of the message even if its is
     * processing by another
     */
    private boolean force;

    /**
     * Default constructor
     */
    public AppCatSendMessageDto() {
        super();
    }

    /**
     * @param group
     * @param pod
     * @param force
     * @param dto
     */
    public AppCatSendMessageDto(final String pod,
            final boolean force) {
        super();
        this.pod = pod;
        this.force = force;
    }

    /**
     * @return the pod
     */
    public String getPod() {
        return pod;
    }

    /**
     * @param pod
     *            the pod to set
     */
    public void setPod(final String pod) {
        this.pod = pod;
    }

    /**
     * @return the force
     */
    public boolean isForce() {
        return force;
    }

    /**
     * @param force
     *            the force to set
     */
    public void setForce(final boolean force) {
        this.force = force;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{pod: %s, force: %s}",
                pod, force);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(pod, force);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            AppCatSendMessageDto other = (AppCatSendMessageDto) obj;
            ret = Objects.equals(pod, other.pod) && force == other.force;
        }
        return ret;
    }

}
