package fr.viveris.s1pdgs.jobgenerator.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object EdrsSessionFile
 * 
 * @author Cyrielle Gailliard
 *
 */
public class EdrsSessionFileTest {

	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {
		EdrsSessionFile file = new EdrsSessionFile();
		assertNull(file.getSessionId());
		assertNull(file.getStartTime());
		assertNull(file.getStopTime());
		assertNotNull(file.getRawNames());
		assertTrue(file.getRawNames().size() == 0);

		Date start = new Date(System.currentTimeMillis() - 10000);
		Date stop = new Date(System.currentTimeMillis());
		EdrsSessionFileRaw raw1 = new EdrsSessionFileRaw("raw1");
		EdrsSessionFileRaw raw2 = new EdrsSessionFileRaw("raw2");
		EdrsSessionFileRaw raw3 = new EdrsSessionFileRaw("raw3");
		file = new EdrsSessionFile("session-id", start, stop, Arrays.asList(raw1, raw2, raw3));
		assertEquals("session-id", file.getSessionId());
		assertEquals(start, file.getStartTime());
		assertEquals(stop, file.getStopTime());
		assertNotNull(file.getRawNames());
		assertTrue(file.getRawNames().size() == 3);
		assertEquals(raw2, file.getRawNames().get(1));
	}
	
	/**
	 * Test to string
	 */
	@Test
	public void testToString() {

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
	public void testEquals() {
		EqualsVerifier.forClass(EdrsSessionFile.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
