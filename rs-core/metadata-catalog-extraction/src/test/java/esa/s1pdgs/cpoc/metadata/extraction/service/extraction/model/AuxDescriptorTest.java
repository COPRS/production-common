package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class AuxDescriptorTest {
	@Test
    public void equalsConfigFileDecriptor() {
		EqualsVerifier.forClass(AuxDescriptor.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }
}
