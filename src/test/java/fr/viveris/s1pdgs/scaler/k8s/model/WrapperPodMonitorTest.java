package fr.viveris.s1pdgs.scaler.k8s.model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class WrapperPodMonitorTest {

	@Test
	public void testToString() {
		PodDesc podDesc = new PodDesc("podDescName");
		WrapperPodMonitor obj = new WrapperPodMonitor(podDesc);
		obj.setLogicalStatus(PodLogicalStatus.PROCESSING);
		obj.setPassedExecutionTime(15000);
		obj.setRemainingExecutionTime(25000);
		
		String str = obj.toString();
		assertTrue(str.contains("description: {name: podDescName"));
		assertTrue(str.contains("logicalStatus: PROCESSING"));
		assertTrue(str.contains("passedExecutionTime: 15000"));
		assertTrue(str.contains("remainingExecutionTime: 25000"));
	}
	
	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(WrapperPodMonitor.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
