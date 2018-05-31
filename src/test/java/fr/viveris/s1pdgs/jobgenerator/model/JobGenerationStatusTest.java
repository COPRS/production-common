package fr.viveris.s1pdgs.jobgenerator.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class JobGenerationStatusTest {
	
	/**
	 * Chack the status update
	 */
	@Test
	public void testUpdateStatus() {
		JobGenerationStatus status = new JobGenerationStatus();
		long tsBefore = 0;
		long tsAfter = 0;
		
		// Check initialization
		assertEquals(GenerationStatusEnum.NOT_READY, status.getStatus());
		assertEquals(0, status.getLastModifiedTime());
		assertEquals(0, status.getNbRetries());
		
		// Check update same value
		tsBefore = System.currentTimeMillis();
		status.updateStatus(GenerationStatusEnum.NOT_READY);
		tsAfter = System.currentTimeMillis();
		assertEquals(GenerationStatusEnum.NOT_READY, status.getStatus());
		assertTrue(status.getLastModifiedTime() >= tsBefore);
		assertTrue(status.getLastModifiedTime() <= tsAfter);
		assertEquals(1, status.getNbRetries());
		
		// Check update same value twice
		tsBefore = System.currentTimeMillis();
		status.updateStatus(GenerationStatusEnum.NOT_READY);
		tsAfter = System.currentTimeMillis();
		assertEquals(GenerationStatusEnum.NOT_READY, status.getStatus());
		assertTrue(status.getLastModifiedTime() >= tsBefore);
		assertTrue(status.getLastModifiedTime() <= tsAfter);
		assertEquals(2, status.getNbRetries());
		
		// Check update other status
		tsBefore = System.currentTimeMillis();
		status.updateStatus(GenerationStatusEnum.PRIMARY_CHECK);
		tsAfter = System.currentTimeMillis();
		assertEquals(GenerationStatusEnum.PRIMARY_CHECK, status.getStatus());
		assertTrue(status.getLastModifiedTime() >= tsBefore);
		assertTrue(status.getLastModifiedTime() <= tsAfter);
		assertEquals(0, status.getNbRetries());
	}
	
	/**
	 * Check toString
	 */
	@Test
	public void testToString() {
		JobGenerationStatus status = new JobGenerationStatus();
		status.updateStatus(GenerationStatusEnum.PRIMARY_CHECK);
		status.updateStatus(GenerationStatusEnum.PRIMARY_CHECK);
		String str = status.toString();
		assertTrue(str.contains("status: PRIMARY_CHECK"));
		assertTrue(str.contains("lastModifiedTime: "));
		assertTrue(str.contains("nbRetries: 1"));
	}
	
	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDto() {
		EqualsVerifier.forClass(JobGenerationStatus.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
