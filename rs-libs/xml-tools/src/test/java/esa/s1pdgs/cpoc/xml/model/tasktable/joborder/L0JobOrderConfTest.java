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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderProcParam;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderSensingTime;
import esa.s1pdgs.cpoc.xml.model.joborder.L0JobOrderConf;
import esa.s1pdgs.cpoc.xml.model.joborder.L1JobOrderConf;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 * @author Cyrielle
 *
 */
public class L0JobOrderConfTest {
	/**
	 * Test clone function
	 */
	@Test
	public void testConstructors() {
		L0JobOrderConf obj = new L0JobOrderConf();
		obj.setProcessorName("processor-name");
		obj.setBreakPointEnable(false);
		obj.setProcessingStation("processing-station");
		obj.setStderrLogLevel("stderr-log-level");
		obj.setStdoutLogLevel("stdout-log-level");
		obj.setTest(true);
		obj.setVersion("v");
		JobOrderSensingTime time = new JobOrderSensingTime("start", "stop");
		obj.setSensingTime(time);
		List<String> configFiles = Arrays.asList("file1","file2","file3");
		obj.setConfigFiles(configFiles);
		obj.addConfigFiles(null);
		JobOrderProcParam proc1 = new JobOrderProcParam("proc1", "val1");
		JobOrderProcParam proc2 = new JobOrderProcParam("proc2", "val2");
		obj.addProcParam(proc1);
		obj.addProcParam(proc2);
		
		L0JobOrderConf clone = new L0JobOrderConf(obj);
		assertEquals(obj.getProcessingStation(), clone.getProcessingStation());
		assertEquals(obj.getProcessorName(), clone.getProcessorName());
		assertEquals(obj.getNbProcParams(), clone.getNbProcParams());
		assertEquals(obj.getSensingTime(), clone.getSensingTime());
		assertEquals(obj.getStderrLogLevel(), clone.getStderrLogLevel());
		assertEquals(obj.getStdoutLogLevel(), clone.getStdoutLogLevel());
		assertEquals(obj.getVersion(), clone.getVersion());
		assertTrue(clone.getNbProcParams() == 2);
		assertTrue(clone.getProcParams().size() == 2);
		assertEquals(obj.getProcParams().get(1), clone.getProcParams().get(1));
		assertTrue(clone.getConfigFiles().size() == 3);
		assertEquals(obj.getConfigFiles().get(1), clone.getConfigFiles().get(1));
		assertFalse(obj.isBreakPointEnable());
		
		obj.setSensingTime(null);
		obj.setProcParams(new ArrayList<>());
		L1JobOrderConf clone2 = new L1JobOrderConf(obj);
		assertNull(clone2.getSensingTime());
		assertTrue(clone2.getNbProcParams() == 0);
		assertTrue(clone2.getProcParams().size() == 0);
		
		obj.setSensingTime(null);
		obj.addProcParam(null);
		L0JobOrderConf clone3 = new L0JobOrderConf(obj);
		assertNull(clone3.getSensingTime());
		assertTrue(obj.getNbProcParams() == 1);
		assertTrue(clone3.getNbProcParams() == 0);
		assertTrue(clone3.getProcParams().size() == 0);
	}
	/**
	 * Test to string
	 */
	@Test
	public void testToStringL0JobOrderConf() {
		
		L0JobOrderConf obj = new L0JobOrderConf();
		obj.setProcessorName("processor-name");
		obj.setBreakPointEnable(false);
		obj.setProcessingStation("processing-station");
		obj.setStderrLogLevel("stderr-log-level");
		obj.setStdoutLogLevel("stdout-log-level");
		obj.setTest(true);
		obj.setVersion("v");
		JobOrderSensingTime time = new JobOrderSensingTime("start", "stop");
		obj.setSensingTime(time);
		List<String> configFiles = Arrays.asList("file1","file2","file3");
		obj.setConfigFiles(configFiles);
		obj.addConfigFiles(null);
		JobOrderProcParam proc1 = new JobOrderProcParam("proc1", "val1");
		JobOrderProcParam proc2 = new JobOrderProcParam("proc2", "val2");
		obj.addProcParam(proc1);
		obj.addProcParam(proc2);
		
		assertTrue(obj.getNbProcParams() == 2);
		assertTrue(obj.getConfigFiles().size() == 3);
		
		String str = obj.toString();
		assertTrue(str.contains("processorName: processor-name"));
		assertTrue(str.contains("version: v"));
		assertTrue(str.contains("stdoutLogLevel: stdout-log-level"));
		assertTrue(str.contains("stderrLogLevel: stderr-log-level"));
		assertTrue(str.contains("test: true"));
		assertTrue(str.contains("breakPointEnable: false"));
		assertTrue(str.contains("processingStation: processing-station"));
		assertTrue(str.contains("sensingTime: " + time.toString()));
		assertTrue(str.contains("configFiles: " + configFiles.toString()));
		assertTrue(str.contains("procParams: "));
		assertTrue(str.contains(proc1.toString()));
		assertTrue(str.contains("nbProcParams: 2"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDtoL0JobOrderConf() {
		EqualsVerifier.forClass(L0JobOrderConf.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
