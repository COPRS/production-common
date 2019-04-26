package esa.s1pdgs.cpoc.jobgenerator.model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.jobgenerator.model.EdrsSessionFileRaw;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object EdrsSessionFileRaw
 * @author Cyrielle Gailliard
 *
 */
public class EdrsSessionFileRawTest {
	
	/**
	 * Test toString
	 */
	@Test
	public void testToString() {
		EdrsSessionFileRaw raw = new EdrsSessionFileRaw();
		raw.setFileName("raw-filename");
		raw.setObjectStorageKey("raw-obs");
		
		String str = raw.toString();
		assertTrue(str.contains("fileName: raw-filename"));
		assertTrue(str.contains("objectStorageKey: raw-obs"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDto() {
		EqualsVerifier.forClass(EdrsSessionFileRaw.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
