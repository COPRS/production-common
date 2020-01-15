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
                "./test_work_dir/", "job-order", new UUID(23L, 42L));
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
