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

package esa.s1pdgs.cpoc.common.errors.processing;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * Exception occurred during job generation
 * 
 * @author Viveris Technologies
 */
public class IpfExecutionWorkerProcessExecutionException extends AbstractCodedException {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -7488001919910076897L;

    /**
     * Exit code of the process
     */
    private final int exitCode;

    /**
     * Constructor
     * 
     * @param message
     */
    public IpfExecutionWorkerProcessExecutionException(final int exitCode, final String message) {
        super(ErrorCode.PROCESS_EXIT_ERROR, message);
        this.exitCode = exitCode;
    }

    /**
     * @return
     */
    public int getExitCode() {
        return this.exitCode;
    }

    /**
     * @see AbstractCodedException#getLogMessage()
     */
    @Override
    public String getLogMessage() {
        return String.format("[exitCode %d] [msg %s]", exitCode, getMessage());
    }
}
