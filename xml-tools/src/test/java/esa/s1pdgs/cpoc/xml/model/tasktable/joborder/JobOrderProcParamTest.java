package esa.s1pdgs.cpoc.xml.model.tasktable.joborder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderProcParam;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class JobOrderProcParamTest {
	
	/**
	 * Test constructors
	 */
	@Test
	public void testConstructorClone() {
		
		JobOrderProcParam obj = new JobOrderProcParam();
		obj.setName("starttime");
		obj.setValue("stoptime");
		
		JobOrderProcParam clone = new JobOrderProcParam(obj);
		assertEquals(obj.getName(), clone.getName());
		assertEquals(obj.getValue(), clone.getValue());
	}
	
	/**
	 * Test to string
	 */
	@Test
	public void testToString() {
		
		JobOrderProcParam obj = new JobOrderProcParam();
		obj.setName("starttime");
		obj.setValue("stoptime");
		
		String str = obj.toString();
		assertTrue(str.contains("name: starttime"));
		assertTrue(str.contains("value: stoptime"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(JobOrderProcParam.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
