package esa.s1pdgs.cpoc.appcatalog;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class TestAppDataJobSegmentFile {

	/**
	 * Check equals and hashcode methods
	 */
	@Test
	public void checkEquals() {
		EqualsVerifier.forClass(AppDataJobSegmentFile.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
