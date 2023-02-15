package esa.s1pdgs.cpoc.ipf.execution.worker.job.process;

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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.StreamUtils;

import esa.s1pdgs.cpoc.ipf.execution.worker.test.SystemUtils;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class TaskCallableTest {
    private final Reporting reporting = ReportingUtils.newReportingBuilder(MissionId.S1).newReporting("TestProcessing");
	  
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
        	StreamUtils.copy(in,out);
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
				new TaskCallable(ipf.getPath(), "", "0", testDir.getPath(),System.out::println, System.out::println, reporting)
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
				.submit(new TaskCallable(ipf.getPath(), "", "0", testDir.getPath(), outputConsumer, outputConsumer, reporting));
		final TaskResult result = future.get();
		assertEquals(ipf.getPath(), result.getBinary());
		assertEquals(0, result.getExitCode());
		
		// check that logs have been consumed
		assertEquals("hello;world;", builder.toString()); 
	}
}
