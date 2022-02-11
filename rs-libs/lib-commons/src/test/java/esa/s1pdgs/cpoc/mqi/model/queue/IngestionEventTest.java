package esa.s1pdgs.cpoc.mqi.model.queue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the DTO object KafkaConfigFileDto
 * 
 * @author Viveris Technologies
 *
 */
public class IngestionEventTest {
	/**
	 * Check equals and hashcode methods
	 */
	@Test
	public void checkEquals() {
		EqualsVerifier.forClass(IngestionEvent.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
