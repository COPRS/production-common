package fr.viveris.s1pdgs.level0.wrapper.job.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Test;

import fr.viveris.s1pdgs.common.errors.AbstractCodedException;
import fr.viveris.s1pdgs.level0.wrapper.test.SystemUtils;
import fr.viveris.s1pdgs.mqi.model.queue.LevelJobPoolDto;
import fr.viveris.s1pdgs.mqi.model.queue.LevelJobTaskDto;

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
        LevelJobPoolDto dto = new LevelJobPoolDto();
        dto.addTask(new LevelJobTaskDto(SystemUtils.getCmdMkdir()));
        PoolProcessor processor = new PoolProcessor(dto, "3", "./", "log", 60);
        assertFalse(testDir.exists());
        processor.process();
        assertTrue(testDir.exists() && testDir.isDirectory());
    }

    @Test
    public void testExecutionOkSeveralTasks() throws AbstractCodedException {
        LevelJobPoolDto dto = new LevelJobPoolDto();
        dto.addTask(new LevelJobTaskDto(SystemUtils.getCmdMkdir()));
        dto.addTask(new LevelJobTaskDto(SystemUtils.getCmdLs()));
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
        LevelJobPoolDto dto = new LevelJobPoolDto();
        dto.addTask(new LevelJobTaskDto(SystemUtils.getCmdFalse()));
        PoolProcessor processor = new PoolProcessor(dto, "3", "./", "log", 60);
        processor.process();
    }
}
