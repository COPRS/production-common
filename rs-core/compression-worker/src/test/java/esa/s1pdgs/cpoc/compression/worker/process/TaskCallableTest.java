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

package esa.s1pdgs.cpoc.compression.worker.process;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.compression.worker.test.SystemUtils;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class TaskCallableTest {
	final Reporting report = ReportingUtils.newReportingBuilder(MissionId.S1).newReporting("TestProcessing");
	  
	private File testDir;
	private File script;
	private File data;
	
	private CompletionService<TaskResult> completionService = new ExecutorCompletionService<>(
			Executors.newSingleThreadExecutor()
    );

	@Before
	public final void setUp() throws IOException {
		testDir = Files.createTempDirectory("tmp").toFile();
		script = new File(testDir, "compress.sh");
		    	
    	try (final InputStream in = SystemUtils.getInputStream("compress.sh");
    		 final OutputStream out = SystemUtils.newFileOutputStream(script))
    	{
        	IOUtils.copy(in,out);
    	}
    	script.setExecutable(true);
    	
    	data = new File(testDir,"data.dat");
    	try (final InputStream in = SystemUtils.getInputStream("data.dat");
       		 final OutputStream out = SystemUtils.newFileOutputStream(data))
       	{
           	IOUtils.copy(in,out);
       	}
	}

	@After
	public final void tearDown() throws IOException {
		script.delete();
		testDir.delete();
		data.delete();
	}
	
	@Test
	public void testRun_Nominal() throws Exception {		
//		final Future<TaskResult> future = completionService.submit(
//				new TaskCallable(script.getPath(),data.getPath(), testDir.getPath(), reportingFactory.newReporting(0))
//		);
//		final TaskResult result = future.get();
//		assertEquals(script.getPath(), result.getBinary());
//		assertEquals(0, result.getExitCode());
	}
	
	@Test
	public final void testRun_ConsumptionOfLog() throws Exception {
//		final StringBuilder builder = new StringBuilder();
//
//		final Consumer<String> outputConsumer = m -> builder.append(m);
//
//		final Future<TaskResult> future = completionService
//				.submit(new TaskCallable(script.getPath(), data.getPath(), testDir.getPath(), outputConsumer, outputConsumer, reportingFactory.newReporting(0)));
//		final TaskResult result = future.get();
//		assertEquals(script.getPath(), result.getBinary());
//		assertEquals(0, result.getExitCode());
//		
//		// check that logs have been consumed
//		assertEquals("Compressing "+testDir.getPath()+"/data.dat to "+testDir.getPath()+"/data.dat.zip", builder.toString()); 
	}
}

