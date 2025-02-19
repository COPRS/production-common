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

package esa.s1pdgs.cpoc.ipf.execution.worker.job.process;

import java.util.Objects;

/**
 * @author Viveris Technologies
 */
public class TaskResult {

    /**
     * Binary
     */
    private String binary;

    /**
     * Exit code of the binary execution
     */
    private int exitCode;

    /**
     * @param binary
     * @param exitCode
     */
    public TaskResult(final String binary, final int exitCode) {
        super();
        this.binary = binary;
        this.exitCode = exitCode;
    }

    /**
     * @return the binary
     */
    public String getBinary() {
        return binary;
    }

    /**
     * @param binary
     *            the binary to set
     */
    public void setBinary(final String binary) {
        this.binary = binary;
    }

    /**
     * @return the exitCode
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * @param exitCode
     *            the exitCode to set
     */
    public void setExitCode(final int exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{binary: %s, exitCode: %s}", binary, exitCode);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(binary, exitCode);
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
            TaskResult other = (TaskResult) obj;
            ret = Objects.equals(binary, other.binary)
                    && Objects.equals(exitCode, other.exitCode);
        }
        return ret;
    }

}
