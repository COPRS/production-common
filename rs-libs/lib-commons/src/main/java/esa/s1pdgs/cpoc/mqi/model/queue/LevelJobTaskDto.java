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

package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

/**
 * @author Viveris Technologies
 * @see IpfExecutionJob
 */
public class LevelJobTaskDto {

    /**
     * Absolute path of the binary
     */
    private String binaryPath;

    /**
     * Default constructor
     */
    public LevelJobTaskDto() {
        super();
    }

    /**
     * Constructor using fields
     * 
     * @param binaryPath
     */
    public LevelJobTaskDto(final String binaryPath) {
        this();
        this.binaryPath = binaryPath;
    }

    /**
     * @return the binaryPath
     */
    public String getBinaryPath() {
        return binaryPath;
    }

    /**
     * @param binaryPath
     *            the binaryPath to set
     */
    public void setBinaryPath(final String binaryPath) {
        this.binaryPath = binaryPath;
    }

    /**
     * to string
     */
    @Override
    public String toString() {
        return String.format("{binaryPath: %s}", binaryPath);
    }

    /**
     * hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(binaryPath);
    }

    /**
     * equals
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            LevelJobTaskDto other = (LevelJobTaskDto) obj;
            ret = Objects.equals(binaryPath, other.binaryPath);
        }
        return ret;
    }

}
