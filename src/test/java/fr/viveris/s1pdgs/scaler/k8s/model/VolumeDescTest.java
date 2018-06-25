package fr.viveris.s1pdgs.scaler.k8s.model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class VolumeDescTest {

	/**
	 * Test toString
	 */
	@Test
	public void testToString() {
		VolumeDesc obj = new VolumeDesc("name");
		
		String str = obj.toString();
		assertTrue(str.contains("name: name"));
	}
	
	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(VolumeDesc.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
