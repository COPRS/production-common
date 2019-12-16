package esa.s1pdgs.cpoc.ipf.preparation.worker.model.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.ipf.preparation.worker.model.converter.TaskTableToJobOrderConverter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrderOutput;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.enums.JobOrderFileNameType;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableOuput;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableTask;
import esa.s1pdgs.cpoc.ipf.preparation.worker.tasks.TestL0Utils;

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
		TaskTableToJobOrderConverter converter = new TaskTableToJobOrderConverter();
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
		assertEquals("[Procs] Invalid breakpoint enable", "OFF", o.getProcs().get(2).getBreakpoint().getEnable());

		assertTrue("[Inputs] Invalid number", 0 == o.getProcs().get(0).getInputs().size());

		assertTrue("[Outputs] Invalid number", task11.getOutputs().size() == o.getProcs().get(0).getOutputs().size());
		JobOrderOutput oOutput = o.getProcs().get(0).getOutputs().get(0);
		TaskTableOuput tOutput = task11.getOutputs().get(0);
		assertFalse("[Outputs] Invalid mandatory", oOutput.isMandatory());
		assertTrue("[Outputs] Invalid filenametype", oOutput.getFileNameType() == JobOrderFileNameType.REGEXP);
		assertEquals("[Outputs] Invalid filenametype", tOutput.getType(), oOutput.getFileType());
	}

}
