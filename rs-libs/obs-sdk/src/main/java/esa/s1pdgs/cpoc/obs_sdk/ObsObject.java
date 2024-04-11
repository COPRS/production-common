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

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Describe an object of the S1-PDGS object storage
 * 
 * @author Viveris Technologies
 */
public class ObsObject {

    /**
     * Object prefix or key
     */
    protected String key;

    /**
     * Object family
     */
    protected ProductFamily family;

    /**
     * Constructor
     * 
     * @param family
     * @param key
     */
    public ObsObject(final ProductFamily family, final String key) {
        this.family = family;
        this.key = key;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(final String key) {
        this.key = key;
    }

    /**
     * @return the family
     */
    public ProductFamily getFamily() {
        return family;
    }

    /**
     * @param family
     *            the family to set
     */
    public void setFamily(final ProductFamily family) {
        this.family = family;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{key: %s, family: %s}", key, family);
    }

    /**
     * toString function used by the classes extending this one
     * 
     * @return
     */
    public String toStringForExtend() {
        return String.format("key: %s, family: %s", key, family);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(key, family);
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
            ObsObject other = (ObsObject) obj;
            ret = Objects.equals(key, other.key)
                    && Objects.equals(family, other.family);
        }
        return ret;
    }

}
