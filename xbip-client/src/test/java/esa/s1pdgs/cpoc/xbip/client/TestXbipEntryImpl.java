package esa.s1pdgs.cpoc.xbip.client;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public final class TestXbipEntryImpl {
	@Test
	public void checkEquals() {
		EqualsVerifier.forClass(XbipEntryImpl.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
