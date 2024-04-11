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

package esa.s1pdgs.cpoc.ipf.execution.worker.job;

import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;

public final class MonitorLogUtils {

    /**
     * Identifier for default monitor logs
     */
    public static final String LOG_DFT = "default";
    /**
     * Identifier for end monitor logs
     */
    public static final String LOG_BEGIN = "begin";
    /**
     * Identifier for end monitor logs
     */
    public static final String LOG_READ = "read";
    /**
     * Identifier for processes execution monitor logs
     */
    public static final String LOG_PROCESS = "process";
    /**
     * Identifier for input download monitor logs
     */
    public static final String LOG_INPUT = "input";
    /**
     * Identifier for output processing monitor logs
     */
    public static final String LOG_OUTPUT = "output";
    /**
     * Identifier for erasing monitor logs
     */
    public static final String LOG_ERASE = "erase";
    /**
     * Identifier for resuming monitor logs
     */
    public static final String LOG_ACK = "ack";
    /**
     * Identifier for status monitor logs
     */
    public static final String LOG_STATUS = "status";
    /**
     * Identifier for end monitor logs
     */
    public static final String LOG_END = "end";
    /**
     * Identifier for error monitor logs
     */
    public static final String LOG_ERROR = "error";

    /**
     * Get the prefix for monitor logs according the step
     * 
     * @param step
     * @return
     */
    public static String getPrefixMonitorLog(final String step,
            final IpfExecutionJob job) {
        String ret;
        switch (step) {
            case LOG_ERROR:
                ret = String.format("[productName %s] [workDir %s]",
                        job.getKeyObjectStorage(), job.getWorkDirectory());
                break;
            case LOG_BEGIN:
                ret = String.format(
                        "[MONITOR] [step 0] [productName %s] [workDir %s]",
                        job.getKeyObjectStorage(), job.getWorkDirectory());
                break;
            case LOG_READ:
                ret = String.format(
                        "[MONITOR] [step 1] [productName %s] [workDir %s]",
                        job.getKeyObjectStorage(), job.getWorkDirectory());
                break;
            case LOG_PROCESS:
                ret = String.format(
                        "[MONITOR] [step 3] [productName %s] [workDir %s]",
                        job.getKeyObjectStorage(), job.getWorkDirectory());
                break;
            case LOG_INPUT:
                ret = String.format(
                        "[MONITOR] [step 2] [productName %s] [workDir %s]",
                        job.getKeyObjectStorage(), job.getWorkDirectory());
                break;
            case LOG_OUTPUT:
                ret = String.format(
                        "[MONITOR] [step 4] [productName %s] [workDir %s]",
                        job.getKeyObjectStorage(), job.getWorkDirectory());
                break;
            case LOG_ACK:
                ret = String.format(
                        "[MONITOR] [step 6] [productName %s] [workDir %s]",
                        job.getKeyObjectStorage(), job.getWorkDirectory());
                break;
            case LOG_ERASE:
                ret = String.format(
                        "[MONITOR] [step 5] [productName %s] [workDir %s]",
                        job.getKeyObjectStorage(), job.getWorkDirectory());
                break;
            case LOG_STATUS:
                ret = String.format(
                        "[MONITOR] [step 7] [productName %s] [workDir %s]",
                        job.getKeyObjectStorage(), job.getWorkDirectory());
                break;
            case LOG_END:
                ret = String.format(
                        "[MONITOR] [step 0] [productName %s] [workDir %s]",
                        job.getKeyObjectStorage(), job.getWorkDirectory());
                break;
            default:
                ret = "[MONITOR]";
                break;
        }
        return ret;
    }

}
