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

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;

public class MonitorLogUtilsTest {


    /**
     * Test the method for getting prefix monitor logs
     */
    @Test
    public void testGetPrefixMonitorLogs() {
        
        IpfExecutionJob job = new IpfExecutionJob(ProductFamily.L0_JOB, "SESSIONID", "FAST",
                "./test_work_dir/", "job-order", "FAST24", new UUID(23L, 42L));
        String baseLog = "[MONITOR]";
        String productLog =
                "[productName SESSIONID] [workDir ./test_work_dir/]";

        assertEquals(baseLog,
                MonitorLogUtils.getPrefixMonitorLog(MonitorLogUtils.LOG_DFT, job));
        assertEquals(baseLog, MonitorLogUtils.getPrefixMonitorLog("tutu", job));

        assertEquals(productLog,
                MonitorLogUtils.getPrefixMonitorLog(MonitorLogUtils.LOG_ERROR, job));

        assertEquals(baseLog + " [step 0] " + productLog,
                MonitorLogUtils.getPrefixMonitorLog(MonitorLogUtils.LOG_BEGIN, job));
        assertEquals(baseLog + " [step 1] " + productLog,
                MonitorLogUtils.getPrefixMonitorLog(MonitorLogUtils.LOG_READ, job));
        assertEquals(baseLog + " [step 3] " + productLog,
                MonitorLogUtils.getPrefixMonitorLog(MonitorLogUtils.LOG_PROCESS, job));
        assertEquals(baseLog + " [step 2] " + productLog,
                MonitorLogUtils.getPrefixMonitorLog(MonitorLogUtils.LOG_INPUT, job));
        assertEquals(baseLog + " [step 4] " + productLog,
                MonitorLogUtils.getPrefixMonitorLog(MonitorLogUtils.LOG_OUTPUT, job));
        assertEquals(baseLog + " [step 5] " + productLog,
                MonitorLogUtils.getPrefixMonitorLog(MonitorLogUtils.LOG_ERASE, job));
        assertEquals(baseLog + " [step 6] " + productLog,
                MonitorLogUtils.getPrefixMonitorLog(MonitorLogUtils.LOG_ACK, job));
        assertEquals(baseLog + " [step 7] " + productLog,
                MonitorLogUtils.getPrefixMonitorLog(MonitorLogUtils.LOG_STATUS, job));
        assertEquals(baseLog + " [step 0] " + productLog,
                MonitorLogUtils.getPrefixMonitorLog(MonitorLogUtils.LOG_END, job));

    }
}
