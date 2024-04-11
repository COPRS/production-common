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
 * Representes an object whose function is to be downloaded on a local directory
 * 
 * @author Viveris Technologies
 */
public class ObsDownloadObject extends ObsObject {

    /**
     * Objects will be download in this directory
     */
    private String targetDir;

    /**
     * Ignore folders in the key
     */
    private boolean ignoreFolders;

    /**
     * Constructor
     * 
     * @param key
     * @param family
     * @param targetDir
     */
    public ObsDownloadObject(final ProductFamily family, final String key, final String targetDir) {
        super(family, key);
        this.targetDir = targetDir;
        if (family == ProductFamily.EDRS_SESSION) {
            this.ignoreFolders = true;
        } else {
            this.ignoreFolders = false;
        }
    }

    /**
     * @return the targetDir
     */
    public String getTargetDir() {
        return targetDir;
    }

    /**
     * @param targetDir
     *            the targetDir to set
     */
    public void setTargetDir(final String targetDir) {
        this.targetDir = targetDir;
    }

    /**
     * @return the ignoreFolders
     */
    public boolean isIgnoreFolders() {
        return ignoreFolders;
    }

    /**
     * @param ignoreFolders
     *            the ignoreFolders to set
     */
    public void setIgnoreFolders(final boolean ignoreFolders) {
        this.ignoreFolders = ignoreFolders;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String superToStr = super.toStringForExtend();
        return String.format("{%s, targetDir: %s, ignoreFolders: %s}",
                superToStr, targetDir, ignoreFolders);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int superHash = super.hashCode();
        return Objects.hash(superHash, targetDir, ignoreFolders);
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
            ObsDownloadObject other = (ObsDownloadObject) obj;
            ret = super.equals(obj)
                    && Objects.equals(targetDir, other.targetDir)
                    && ignoreFolders == other.ignoreFolders;
        }
        return ret;
    }

}
