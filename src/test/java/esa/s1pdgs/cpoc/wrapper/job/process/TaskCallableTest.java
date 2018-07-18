package esa.s1pdgs.cpoc.wrapper.job.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Test;

import esa.s1pdgs.cpoc.wrapper.job.process.TaskCallable;
import esa.s1pdgs.cpoc.wrapper.job.process.TaskResult;
import esa.s1pdgs.cpoc.wrapper.test.SystemUtils;

public class TaskCallableTest {
    
    private File testDir = new File("./3");
    
    @After
    public void clean() {
        if (testDir.exists()) {
            testDir.delete();
        }
    }
	
	@Test
	public void testRun() throws InterruptedException, ExecutionException {
        // Command dir/ls
        String command = SystemUtils.getCmdMkdir();
        
		assertTrue(!(new File("./3")).exists());
		ExecutorService service = Executors.newSingleThreadExecutor();
		CompletionService<TaskResult> completionService = new ExecutorCompletionService<>(service);
		completionService.submit(new TaskCallable(command, "3", "./"));
		Future<TaskResult> future = completionService.take();
		TaskResult r = future.get();
		assertEquals(command, r.getBinary());
		assertEquals(0, r.getExitCode());
		assertTrue((new File("./3")).isDirectory());
		
		command = SystemUtils.getCmdRmdir();
		completionService.submit(new TaskCallable(command, "3", "./"));
		completionService.take();
		assertTrue(!(new File("./3")).exists());
	}

    @Test
	public void testExitCode() throws InterruptedException, ExecutionException {
		// Command dir/ls
		String command = SystemUtils.getCmdLs();
		
		// Test when folder do not exist
		ExecutorService service = Executors.newSingleThreadExecutor();
		CompletionService<TaskResult> completionService = new ExecutorCompletionService<>(service);
		completionService.submit(new TaskCallable(command, "not_exist", "./src/main/"));
		Future<TaskResult> future = completionService.take();
		TaskResult r = future.get();
		assertNotEquals(0, r.getExitCode());
		// Test when folder exist
		completionService.submit(new TaskCallable(command, "resources", "./src/main/"));
		Future<TaskResult> future2 = completionService.take();
		TaskResult r2 = future2.get();
		assertEquals(0, r2.getExitCode());
	}
    
    @Test
    public void testRunWithInterrupted() throws InterruptedException, ExecutionException {
        // Command dir/ls
        String command = SystemUtils.getCmdSleep();
        
        assertTrue(!(new File("./3")).exists());
        ExecutorService service = Executors.newSingleThreadExecutor();
        CompletionService<TaskResult> completionService = new ExecutorCompletionService<>(service);
        completionService.submit(new TaskCallable(command, "3", "./"));
        
        // Interrupt
        service.shutdownNow();
        Future<TaskResult> future = completionService.take();
        TaskResult r = future.get();
        assertEquals(command, r.getBinary());
        assertEquals(-1, r.getExitCode());
    }

}
