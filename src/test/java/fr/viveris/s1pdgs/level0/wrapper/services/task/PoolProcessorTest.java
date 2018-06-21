package fr.viveris.s1pdgs.level0.wrapper.services.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Test;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobPoolDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobTaskDto;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.AbstractCodedException;
import fr.viveris.s1pdgs.level0.wrapper.test.SystemUtils;

public class PoolProcessorTest {

    private File testDir = new File("./3");

    @After
    public void clean() {
        if (testDir.exists()) {
            testDir.delete();
        }
    }

    @Test
    public void testExecutionOk() throws AbstractCodedException {
        JobPoolDto dto = new JobPoolDto();
        dto.addTask(new JobTaskDto(SystemUtils.getCmdMkdir()));
        PoolProcessor processor = new PoolProcessor(dto, "3", "./", "log", 60);
        assertFalse(testDir.exists());
        processor.process();
        assertTrue(testDir.exists() && testDir.isDirectory());
    }

    @Test
    public void testExecutionOkSeveralTasks() throws AbstractCodedException {
        JobPoolDto dto = new JobPoolDto();
        dto.addTask(new JobTaskDto(SystemUtils.getCmdMkdir()));
        dto.addTask(new JobTaskDto(SystemUtils.getCmdLs()));
        PoolProcessor processor = new PoolProcessor(dto, "3", "./", "log", 60);
        assertFalse(testDir.exists());
        processor.process();
        assertTrue(testDir.exists() && testDir.isDirectory());
    }

    /**
     * Test when task fails with exit code between 1 and 127
     * @throws AbstractCodedException
     */
    @Test
    public void testExecutionK0SeveralTasks() throws AbstractCodedException {
        JobPoolDto dto = new JobPoolDto();
        dto.addTask(new JobTaskDto(SystemUtils.getCmdFalse()));
        PoolProcessor processor = new PoolProcessor(dto, "3", "./", "log", 60);
        processor.process();
    }
}
