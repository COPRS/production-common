package esa.s1pdgs.cpoc.xml.model.tasktable.joborder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderTimeInterval;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 *
 */
public class JobOrderTimeIntervalTest {

	/**
	 * Test constructors
	 */
	@Test
	public void testConstructorClone() {

		JobOrderTimeInterval obj = new JobOrderTimeInterval();
		obj.setStart("starttime");
		obj.setStop("stoptime");
		obj.setFileName("file");

		JobOrderTimeInterval clone = new JobOrderTimeInterval(obj);
		assertEquals(obj.getStart(), clone.getStart());
		assertEquals(obj.getStop(), clone.getStop());
		assertEquals(obj.getFileName(), clone.getFileName());
	}

	/**
	 * Test to string
	 */
	@Test
	public void testToString() {

		JobOrderTimeInterval obj = new JobOrderTimeInterval();
		obj.setStart("starttime");
		obj.setStop("stoptime");
		obj.setFileName("file");

		String str = obj.toString();
		assertTrue(str.contains("start: starttime"));
		assertTrue(str.contains("stop: stoptime"));
		assertTrue(str.contains("fileName: file"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(JobOrderTimeInterval.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
