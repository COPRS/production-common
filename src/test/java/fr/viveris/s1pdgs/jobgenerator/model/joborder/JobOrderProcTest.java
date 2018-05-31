package fr.viveris.s1pdgs.jobgenerator.model.joborder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 */
public class JobOrderProcTest {
	
	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {
		
		JobOrderBreakpoint breakpt = new JobOrderBreakpoint("true", Arrays.asList("file1"));
		
		JobOrderInput input1 = new JobOrderInput("type1", null, null, null, null);
		JobOrderInput input2 = new JobOrderInput("type2", null, null, null, null);
		List<JobOrderInput> inputs = Arrays.asList(input1, input2);
		
		JobOrderOutput output1 = new JobOrderOutput("type4", null, null);
		JobOrderOutput output2 = new JobOrderOutput("type5", null, null);
		JobOrderOutput output3 = new JobOrderOutput("type6", null, null);
		List<JobOrderOutput> outputs = Arrays.asList(output1, output2, output3);
		
		JobOrderProc obj = new JobOrderProc();
		obj.setTaskName("name");
		obj.setTaskVersion("vers");
		obj.setBreakpoint(breakpt);
		obj.setInputs(inputs);
		obj.setOutputs(outputs);
		
		JobOrderProc clone = new JobOrderProc(obj);
		assertEquals(obj.getTaskName(), clone.getTaskName());
		assertEquals(obj.getTaskVersion(), clone.getTaskVersion());
		assertEquals(obj.getBreakpoint(), clone.getBreakpoint());
		assertEquals(obj.getInputs().get(0), clone.getInputs().get(0));
		assertEquals(obj.getOutputs().get(1), clone.getOutputs().get(1));
		
		obj.setBreakpoint(null);
		JobOrderProc clone2 = new JobOrderProc(obj);
		assertNull(clone2.getBreakpoint());
		
		obj.setInputs(new ArrayList<>());
		obj.addInput(null);
		JobOrderProc clone3 = new JobOrderProc(obj);
		assertTrue(obj.getInputs().size() == 1);
		assertTrue(clone3.getInputs().size() == 0);
		
		obj.setOutputs(new ArrayList<>());
		obj.addOutput(null);
		JobOrderProc clone4 = new JobOrderProc(obj);
		assertTrue(obj.getOutputs().size() == 1);
		assertTrue(clone4.getOutputs().size() == 0);
	}
	
	/**
	 * Test to string
	 */
	@Test
	public void testToString() {
		
		JobOrderBreakpoint breakpt = new JobOrderBreakpoint("true", Arrays.asList("file1"));
		
		JobOrderInput input1 = new JobOrderInput("type1", null, null, null, null);
		JobOrderInput input2 = new JobOrderInput("type2", null, null, null, null);
		List<JobOrderInput> inputs = Arrays.asList(input1, input2);
		
		JobOrderOutput output1 = new JobOrderOutput("type4", null, null);
		JobOrderOutput output2 = new JobOrderOutput("type5", null, null);
		JobOrderOutput output3 = new JobOrderOutput("type6", null, null);
		List<JobOrderOutput> outputs = Arrays.asList(output1, output2, output3);
		
		JobOrderProc obj = new JobOrderProc();
		obj.setTaskName("name");
		obj.setTaskVersion("vers");
		obj.setBreakpoint(breakpt);
		obj.setInputs(inputs);
		obj.setOutputs(outputs);
		
		String str = obj.toString();
		assertTrue(str.contains("taskName: name"));
		assertTrue(str.contains("taskVersion: vers"));
		assertTrue(str.contains("breakpoint: " + breakpt.toString()));
		assertTrue(str.contains("inputs: " + inputs.toString()));
		assertTrue(str.contains("nbInputs: 2"));
		assertTrue(str.contains("outputs: " + outputs.toString()));
		assertTrue(str.contains("nbOutputs: 3"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(JobOrderProc.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
