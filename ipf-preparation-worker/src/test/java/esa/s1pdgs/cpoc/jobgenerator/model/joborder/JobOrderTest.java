package esa.s1pdgs.cpoc.jobgenerator.model.joborder;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class JobOrderTest {
	/**
	 * Test to string
	 */
	@Test
	public void testToStringJobOrder() {
	    
	    JobOrder obj = new JobOrder();
		
		String str = obj.toString();
		assertTrue(str.contains("conf: "));
		assertTrue(str.contains("procs: "));
		assertTrue(str.contains("nbProcs: 0"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDtoJobOrder() {
		EqualsVerifier.forClass(JobOrder.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
	
}
