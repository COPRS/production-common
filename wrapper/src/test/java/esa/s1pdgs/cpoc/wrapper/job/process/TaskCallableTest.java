package esa.s1pdgs.cpoc.wrapper.job.process;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.wrapper.test.SystemUtils;

public class TaskCallableTest {
	private final Reporting.Factory reportingFactory = new LoggerReporting.Factory(LogManager.getLogger(TaskCallableTest.class), "TestProcessing");
	  
	private File testDir;
	private File ipf;
	
	private CompletionService<TaskResult> completionService = new ExecutorCompletionService<>(
			Executors.newSingleThreadExecutor()
    );

	@Before
	public final void setUp() throws IOException {
		testDir = Files.createTempDirectory("tmp").toFile();
		ipf = new File(testDir, "dummyIpf.sh");
		    	
    	try (final InputStream in = SystemUtils.getInputStream("ipf.sh");
    		 final OutputStream out = SystemUtils.newFileOutputStream(ipf))
    	{
        	IOUtils.copy(in,out);
    	}
    	ipf.setExecutable(true);
	}

	@After
	public final void tearDown() throws IOException {
		ipf.delete();
		testDir.delete();
	}
	
	@Test
	public void testRun_Nominal() throws Exception {		
		final Future<TaskResult> future = completionService.submit(
				new TaskCallable(ipf.getPath(), "0", testDir.getPath(), reportingFactory.newReporting(0))
		);
		final TaskResult result = future.get();
		assertEquals(ipf.getPath(), result.getBinary());
		assertEquals(0, result.getExitCode());
	}
	
	@Test
	public final void testRun_ConsumptionOfLog() throws Exception {
		final StringBuilder builder = new StringBuilder();

		final Consumer<String> outputConsumer = m -> builder.append(m).append(';');

		final Future<TaskResult> future = completionService
				.submit(new TaskCallable(ipf.getPath(), "0", testDir.getPath(), outputConsumer, outputConsumer, reportingFactory.newReporting(0)));
		final TaskResult result = future.get();
		assertEquals(ipf.getPath(), result.getBinary());
		assertEquals(0, result.getExitCode());
		
		// check that logs have been consumed
		assertEquals("hello;world;", builder.toString()); 
	}
}
