package esa.s1pdgs.cpoc.ipf.execution.worker.job.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.ipf.execution.worker.test.SystemUtils;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobPoolDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobTaskDto;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class PoolProcessorTest {
    private final Reporting reporting = ReportingUtils.newReportingBuilder().newTaskReporting("TestProcessing");
	  
    private File testDir = new File("./3");

    @After
    public void clean() {
        if (testDir.exists()) {
            testDir.delete();
        }
    }

    @Test
    public void testExecutionOk() throws AbstractCodedException {
        final LevelJobPoolDto dto = new LevelJobPoolDto();
        dto.addTask(new LevelJobTaskDto(SystemUtils.getCmdMkdir()));
        final PoolProcessor processor = new PoolProcessor(dto, "3", "./", "log", 60);
        assertFalse(testDir.exists());
        processor.process(reporting.getChildFactory());
        assertTrue(testDir.exists() && testDir.isDirectory());
    }

    @Test
    public void testExecutionOkSeveralTasks() throws AbstractCodedException {
        final LevelJobPoolDto dto = new LevelJobPoolDto();
        dto.addTask(new LevelJobTaskDto(SystemUtils.getCmdMkdir()));
        dto.addTask(new LevelJobTaskDto(SystemUtils.getCmdLs()));
        final PoolProcessor processor = new PoolProcessor(dto, "3", "./", "log", 60);
        assertFalse(testDir.exists());
        processor.process(reporting.getChildFactory());
        assertTrue(testDir.exists() && testDir.isDirectory());
    }

    /**
     * Test when task fails with exit code between 1 and 127
     * @throws AbstractCodedException
     */
    @Test
    public void testExecutionK0SeveralTasks() throws AbstractCodedException {
        final LevelJobPoolDto dto = new LevelJobPoolDto();
        dto.addTask(new LevelJobTaskDto(SystemUtils.getCmdFalse()));
        final PoolProcessor processor = new PoolProcessor(dto, "3", "./", "log", 60);
        processor.process(reporting.getChildFactory());
    }
}
