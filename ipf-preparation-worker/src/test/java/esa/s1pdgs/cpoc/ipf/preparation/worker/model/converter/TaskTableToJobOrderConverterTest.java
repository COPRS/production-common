package esa.s1pdgs.cpoc.ipf.preparation.worker.model.converter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableFactory;
import esa.s1pdgs.cpoc.xml.XmlConverter;
import esa.s1pdgs.cpoc.xml.config.XmlConfig;
import esa.s1pdgs.cpoc.xml.model.joborder.AbstractJobOrderConf;
import esa.s1pdgs.cpoc.xml.model.joborder.AbstractJobOrderProc;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderOutput;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderSensingTime;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderTimeInterval;
import esa.s1pdgs.cpoc.xml.model.joborder.StandardJobOrderBreakpoint;
import esa.s1pdgs.cpoc.xml.model.joborder.enums.JobOrderFileNameType;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableOuput;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableTask;

/**
 * Test the converter TaskTable to JobOrder
 * @author Cyrielle Gailliard
 *
 */
public class TaskTableToJobOrderConverterTest {

	/**
	 * Convert the TaskTableAIOP
	 */
	@Test
	public void testConverter() {
		TaskTable t = TestL0Utils.buildTaskTableAIOP();
		TaskTableToJobOrderConverter converter = new TaskTableToJobOrderConverter(ProductMode.SLICING);
		JobOrder o = converter.apply(t);

		TaskTableTask task11 = t.getPools().get(0).getTasks().get(0);
		TaskTableTask task13 = t.getPools().get(0).getTasks().get(2);
		
		assertEquals("[conf] Invalid processor name", t.getProcessorName(), o.getConf().getProcessorName());
		assertEquals("[conf] Invalid version", t.getVersion(), o.getConf().getVersion());

		assertEquals("Invalid number of DynProcParams", t.getDynProcParams().size(), o.getConf().getProcParams().size());
		assertEquals("Invalid name for DynProcParams", t.getDynProcParams().get(0).getName(),
				o.getConf().getProcParams().get(0).getName());
		assertEquals("Invalid value for DynProcParams", t.getDynProcParams().get(0).getDefaultValue(),
				o.getConf().getProcParams().get(0).getValue());

		assertEquals("[Cfg File] Invalid number", t.getCfgFiles().size(), o.getConf().getConfigFiles().size());
		assertEquals("[Cfg File] Invalid name", t.getCfgFiles().get(0).getFileName(), o.getConf().getConfigFiles().get(0));

		assertEquals("[Procs] Invalid number", 4, o.getProcs().size());
		assertEquals("[Procs] Invalid name", task13.getName(), o.getProcs().get(2).getTaskName());
		assertEquals("[Procs] Invalid name", task13.getVersion(), o.getProcs().get(2).getTaskVersion());
		assertNotNull("[Procs] Invalid breakpoint", o.getProcs().get(2).getBreakpoint());
		assertTrue(o.getProcs().get(2).getBreakpoint() instanceof StandardJobOrderBreakpoint);
		assertEquals("[Procs] Invalid breakpoint enable", "OFF", ((StandardJobOrderBreakpoint) o.getProcs().get(2).getBreakpoint()).getEnable());

		assertTrue("[Inputs] Invalid number", 2 == o.getProcs().get(0).getInputs().size());

		assertTrue("[Outputs] Invalid number", task11.getOutputs().size() == o.getProcs().get(0).getOutputs().size());
		JobOrderOutput oOutput = o.getProcs().get(0).getOutputs().get(0);
		TaskTableOuput tOutput = task11.getOutputs().get(0);
		assertFalse("[Outputs] Invalid mandatory", oOutput.isMandatory());
		assertTrue("[Outputs] Invalid filenametype", oOutput.getFileNameType() == JobOrderFileNameType.REGEXP);
		assertEquals("[Outputs] Invalid filenametype", tOutput.getType(), oOutput.getFileType());

		o.getProcs().stream().flatMap(p -> p.getInputs().stream()).forEach(input -> {
			System.out.println(input.getFileType());
		});
	}

	@Test
	public void testConverterForSppObs() throws IOException, JAXBException, URISyntaxException {

		final File taskTableFile = new File(getClass().getResource("/OBS_TT_01_taskTable.xml").toURI());

		final XmlConverter xmlConverter = new XmlConfig().xmlConverter();
		final TaskTable taskTable =
				new TaskTableFactory(xmlConverter).buildTaskTable(taskTableFile, ApplicationLevel.SPP_OBS);

		final TaskTableToJobOrderConverter converter = new TaskTableToJobOrderConverter(ProductMode.SLICING);

		final JobOrder jobOrder = converter.apply(taskTable);

		applyObsParameter(jobOrder);

		final String jobOrderXml = xmlConverter.convertFromObjectToXMLString(jobOrder);

		final String expectedJobOrder =
				new String(Files.readAllBytes(
						new File(getClass().getResource(
								"/JobOrder_SPP_OBS_expected.xml").toURI()).toPath()),
						StandardCharsets.UTF_8);

		assertThat(jobOrderXml, is(equalTo(expectedJobOrder)));
	}

	@Test
	public void testJobOrderCloning() throws IOException, JAXBException, URISyntaxException {

		final File taskTableFile = new File(getClass().getResource("/OBS_TT_01_taskTable.xml").toURI());

		final XmlConverter xmlConverter = new XmlConfig().xmlConverter();
		final TaskTable taskTable =
				new TaskTableFactory(xmlConverter).buildTaskTable(taskTableFile, ApplicationLevel.SPP_OBS);

		final TaskTableToJobOrderConverter converter = new TaskTableToJobOrderConverter(ProductMode.SLICING);

		final JobOrder jobOrder = new JobOrder(converter.apply(taskTable), ApplicationLevel.SPP_OBS);

		applyObsParameter(jobOrder);

		final String jobOrderXml = xmlConverter.convertFromObjectToXMLString(jobOrder);

		final String expectedJobOrder =
				new String(Files.readAllBytes(
						new File(getClass().getResource(
								"/JobOrder_SPP_OBS_expected.xml").toURI()).toPath()),
						StandardCharsets.UTF_8);

		assertThat(jobOrderXml, is(equalTo(expectedJobOrder)));
	}

	private void applyObsParameter(JobOrder jobOrder) {
		JobOrderSensingTime sensingTime = new JobOrderSensingTime();
		sensingTime.setStart("20200121_183236000000");
		sensingTime.setStop("20200121_215006000000");

		AbstractJobOrderConf jobOrderConf = jobOrder.getConf();
		jobOrderConf.setStderrLogLevel("INFO");
		jobOrderConf.setStdoutLogLevel("INFO");
		jobOrderConf.setBreakPointEnable(false);
		jobOrderConf.setProcessingStation("DPA_");
		jobOrderConf.setSensingTime(sensingTime);
		jobOrderConf.getProcParams().get(0).setValue("2020-01-21T18:32:46.331273");

		AbstractJobOrderProc jobOrderProc = jobOrder.getProcs().get(0);
		jobOrderProc.getInputs().remove(1);
		jobOrderProc.getInputs().remove(1);
		jobOrderProc.setInputs(jobOrderProc.getInputs());

		JobOrderInput jobOrderInput = jobOrderProc.getInputs().get(0);
		jobOrderInput.setFileType("AUX_RES");
		jobOrderInput.setFileNameType(JobOrderFileNameType.PHYSICAL);
		jobOrderInput.addFilename("/data/localWD/129/S1B_OPER_AUX_RESORB_OPOD_20200121T223141_V20200121T183236_20200121T215006.EOF", "");
		jobOrderInput.addTimeInterval(
				new JobOrderTimeInterval(
						"20200121_183236000000",
						"20200121_215006000000",
						"/data/localWD/129/S1B_OPER_AUX_RESORB_OPOD_20200121T223141_V20200121T183236_20200121T215006.EOF"));

		JobOrderOutput jobOrderOutput = jobOrderProc.getOutputs().get(0);
		jobOrderOutput.setFileType("___OBS__SS");
		jobOrderOutput.setFileNameType(JobOrderFileNameType.DIRECTORY);
		jobOrderOutput.setFileName("/data/localWD/129");
	}
}
