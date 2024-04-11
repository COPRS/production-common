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

package esa.s1pdgs.cpoc.xml.model.tasktable.joborder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.xml.model.joborder.StandardJobOrderBreakpoint;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderOutput;
import esa.s1pdgs.cpoc.xml.model.joborder.StandardJobOrderProc;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 */
public class StandardJobOrderProcTest {
	
	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {
		
		StandardJobOrderBreakpoint breakpt = new StandardJobOrderBreakpoint("true", Arrays.asList("file1"));
		
		JobOrderInput input1 = new JobOrderInput("type1", null, null, null, null);
		JobOrderInput input2 = new JobOrderInput("type2", null, null, null, null);
		List<JobOrderInput> inputs = Arrays.asList(input1, input2);
		
		JobOrderOutput output1 = new JobOrderOutput("type4", null, null);
		JobOrderOutput output2 = new JobOrderOutput("type5", null, null);
		JobOrderOutput output3 = new JobOrderOutput("type6", null, null);
		List<JobOrderOutput> outputs = Arrays.asList(output1, output2, output3);
		
		StandardJobOrderProc obj = new StandardJobOrderProc();
		obj.setTaskName("name");
		obj.setTaskVersion("vers");
		obj.setBreakpoint(breakpt);
		obj.setInputs(inputs);
		obj.setOutputs(outputs);
		
		StandardJobOrderProc clone = new StandardJobOrderProc(obj, ApplicationLevel.L0);
		assertEquals(obj.getTaskName(), clone.getTaskName());
		assertEquals(obj.getTaskVersion(), clone.getTaskVersion());
		assertEquals(obj.getBreakpoint(), clone.getBreakpoint());
		assertEquals(obj.getInputs().get(0), clone.getInputs().get(0));
		assertEquals(obj.getOutputs().get(1), clone.getOutputs().get(1));
		
		obj.setBreakpoint(null);
		StandardJobOrderProc clone2 = new StandardJobOrderProc(obj, ApplicationLevel.L0);
		assertNull(clone2.getBreakpoint());
		
		obj.setInputs(new ArrayList<>());
		obj.addInput(null);
		StandardJobOrderProc clone3 = new StandardJobOrderProc(obj, ApplicationLevel.L0);
		assertTrue(obj.getInputs().size() == 1);
		assertTrue(clone3.getInputs().size() == 0);
		
		obj.setOutputs(new ArrayList<>());
		obj.addOutput(null);
		StandardJobOrderProc clone4 = new StandardJobOrderProc(obj, ApplicationLevel.L0);
		assertTrue(obj.getOutputs().size() == 1);
		assertTrue(clone4.getOutputs().size() == 0);
	}
	
	/**
	 * Test to string
	 */
	@Test
	public void testToString() {
		
		StandardJobOrderBreakpoint breakpt = new StandardJobOrderBreakpoint("true", Arrays.asList("file1"));
		
		JobOrderInput input1 = new JobOrderInput("type1", null, null, null, null);
		JobOrderInput input2 = new JobOrderInput("type2", null, null, null, null);
		List<JobOrderInput> inputs = Arrays.asList(input1, input2);
		
		JobOrderOutput output1 = new JobOrderOutput("type4", null, null);
		JobOrderOutput output2 = new JobOrderOutput("type5", null, null);
		JobOrderOutput output3 = new JobOrderOutput("type6", null, null);
		List<JobOrderOutput> outputs = Arrays.asList(output1, output2, output3);
		
		StandardJobOrderProc obj = new StandardJobOrderProc();
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
		EqualsVerifier.forClass(StandardJobOrderProc.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
