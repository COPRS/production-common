package esa.s1pdgs.cpoc.wrapper.job.oqc;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobOutputDto;
import esa.s1pdgs.cpoc.mqi.model.queue.OQCFlag;
import esa.s1pdgs.cpoc.wrapper.config.ApplicationProperties;

public class OQCTaskTest {    
	
	private ApplicationProperties defaultProperties;
	
	@Before
	public void setup() {
		defaultProperties = new ApplicationProperties();
		defaultProperties.setOqcEnabled(true);
		defaultProperties.setOqcTimeoutInSeconds(2); // yes, it is very short, but we want fast junit tests!
		File ipf = new File(getClass().getClassLoader().getResource("ipf-oqc.sh").getFile());
		ipf.setExecutable(true);
		defaultProperties.setOqcBinaryPath(ipf.getAbsolutePath());
		defaultProperties.setOqcWorkingDir("/tmp");
	}

	@Test(timeout = 3000)
	public void testTimeout() throws Exception {
		File ipf = new File(getClass().getClassLoader().getResource("ipf-oqc-block.sh").getFile());
		ipf.setExecutable(true);
		defaultProperties.setOqcBinaryPath(ipf.getAbsolutePath());
		
		OQCExecutor executor = new OQCExecutor(defaultProperties);
		LevelJobOutputDto dto = new LevelJobOutputDto();
		dto.setOqcCheck(true);
		
		Path productDir = Files.createTempDirectory("OQCTASK");

		OQCFlag flag = executor.executeOQC(productDir, dto, new OQCDefaultTaskFactory());
		
		assertThat(flag, is(notNullValue()));
		assertThat(flag, is(OQCFlag.NOT_CHECKED));
		
		FileUtils.delete(productDir.toString());
	}
	
	@Test
	public void testOQCExecutorService() throws Exception {
		OQCExecutor executor = new OQCExecutor(defaultProperties);
		LevelJobOutputDto dto = new LevelJobOutputDto();
		dto.setOqcCheck(true);
		
		Path productDir = Files.createTempDirectory("OQCTASK");

		OQCFlag flag = executor.executeOQC(productDir, dto, new OQCDefaultTaskFactory());
		
		assertThat(flag, is(notNullValue()));
		assertThat(flag, is(OQCFlag.CHECKED_OK));
	}
	
	
	
	@Test
	public void testOQCJobOrderGeneration() throws Exception {
		Path productDir = Files.createTempDirectory(Paths.get("/tmp"), "OQCTASK");
		Path workingDir = Files.createTempDirectory(Paths.get("/tmp"), "OQCWORKDIR");
		
		OQCTask task = new OQCTask(defaultProperties, productDir);
		Path jobOrder = task.generateJobOrder(workingDir);
		
		// After the generation, it is expected that a job order file is there
		assertThat(Files.exists(jobOrder), is(true));
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document document = dbf.newDocumentBuilder().parse(Files.newInputStream(jobOrder));
		XPathFactory xpf = XPathFactory.newInstance();
		XPath xpath = xpf.newXPath();
		String input = ((Node) xpath.compile("//Input/List_of_File_Names/File_Name/text()").evaluate(document, XPathConstants.NODE)).getTextContent();
		String output = ((Node) xpath.compile("//Output/File_Name/text()").evaluate(document, XPathConstants.NODE)).getTextContent();
		
		// verify that the content of the job order is as expected
		assertThat(input,is(equalTo(productDir.toString())));
		assertThat(output,is(equalTo(workingDir.toString()+"/reports")));

		// cleanup
		FileUtils.delete(productDir.toString());
		FileUtils.delete(workingDir.toString());
	}
	

	@Test
	public void testCompleteTask() throws Exception {
		Path productDir = Files.createTempDirectory("OQCTASK");
		
		OQCTask task = new OQCTask(defaultProperties, productDir);
		OQCFlag flag = task.call();
		assertThat(flag,is(notNullValue()));
		assertThat(flag,is(OQCFlag.CHECKED_OK));
		
//		FileUtils.delete(productDir.toString());
	}
}
