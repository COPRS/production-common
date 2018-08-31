package esa.s1pdgs.cpoc.jobgenerator.model.joborder;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import esa.s1pdgs.cpoc.jobgenerator.model.EdrsSessionFile;
import esa.s1pdgs.cpoc.jobgenerator.model.EdrsSessionFileRaw;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderInput;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class JobOrderObjectsTest {
	/**
	 * Test to string
	 */
	@Test
	public void testToStringJobOrder() {

		Date start = new Date(System.currentTimeMillis() - 10000);
		Date stop = new Date(System.currentTimeMillis());
		EdrsSessionFileRaw raw1 = new EdrsSessionFileRaw("raw1");
		EdrsSessionFileRaw raw2 = new EdrsSessionFileRaw("raw2");
		EdrsSessionFileRaw raw3 = new EdrsSessionFileRaw("raw3");
		
		EdrsSessionFile file = new EdrsSessionFile();
		file.setSessionId("session-id");
		file.setStartTime(start);
		file.setStopTime(stop);
		file.setRawNames(Arrays.asList(raw1, raw2, raw3));
		
		String str = file.toString();
		assertTrue(str.contains("sessionId: session-id"));
		assertTrue(str.contains("startTime: " + start.toString()));
		assertTrue(str.contains("stopTime: " + stop.toString()));
		assertTrue(str.contains("rawNames: "));
		assertTrue(str.contains(raw3.toString()));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDtoJobOrder() {
		EqualsVerifier.forClass(JobOrder.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
	/**
	 * Test to string
	 */
	@Test
	public void testToStringJobOrderInput() {

		Date start = new Date(System.currentTimeMillis() - 10000);
		Date stop = new Date(System.currentTimeMillis());
		EdrsSessionFileRaw raw1 = new EdrsSessionFileRaw("raw1");
		EdrsSessionFileRaw raw2 = new EdrsSessionFileRaw("raw2");
		EdrsSessionFileRaw raw3 = new EdrsSessionFileRaw("raw3");
		
		EdrsSessionFile file = new EdrsSessionFile();
		file.setSessionId("session-id");
		file.setStartTime(start);
		file.setStopTime(stop);
		file.setRawNames(Arrays.asList(raw1, raw2, raw3));
		
		String str = file.toString();
		assertTrue(str.contains("sessionId: session-id"));
		assertTrue(str.contains("startTime: " + start.toString()));
		assertTrue(str.contains("stopTime: " + stop.toString()));
		assertTrue(str.contains("rawNames: "));
		assertTrue(str.contains(raw3.toString()));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDtoJobOrderInput() {
		EqualsVerifier.forClass(JobOrderInput.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
	
}
