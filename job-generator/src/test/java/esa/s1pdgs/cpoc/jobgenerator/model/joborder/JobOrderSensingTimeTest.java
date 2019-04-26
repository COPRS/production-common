package esa.s1pdgs.cpoc.jobgenerator.model.joborder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderSensingTime;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 *
 */
public class JobOrderSensingTimeTest {

	/**
	 * Test constructors
	 */
	@Test
	public void testConstructorClone() {

		JobOrderSensingTime obj = new JobOrderSensingTime();
		obj.setStart("starttime");
		obj.setStop("stoptime");

		JobOrderSensingTime clone = new JobOrderSensingTime(obj);
		assertEquals(obj.getStart(), clone.getStart());
		assertEquals(obj.getStop(), clone.getStop());
	}

	/**
	 * Test to string
	 */
	@Test
	public void testToString() {

		JobOrderSensingTime obj = new JobOrderSensingTime();
		obj.setStart("starttime");
		obj.setStop("stoptime");

		String str = obj.toString();
		assertTrue(str.contains("start: starttime"));
		assertTrue(str.contains("stop: stoptime"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(JobOrderSensingTime.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
