package fr.viveris.s1pdgs.scaler.k8s.model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class WrapperDescTest {

	/**
	 * Test toString
	 */
	@Test
	public void testToString() {
		WrapperDesc obj = new WrapperDesc("name");
		obj.setTimeSinceLastChange(15000);
		obj.setErrorCounter(0);
		obj.setStatus(PodLogicalStatus.PROCESSING);
		
		String str = obj.toString();
		assertTrue(str.contains("name: name"));
		assertTrue(str.contains("status: PROCESSING"));
		assertTrue(str.contains("timeSinceLastChange: 15000"));
		assertTrue(str.contains("errorCounter: 0"));
	}
	
	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(WrapperDesc.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
