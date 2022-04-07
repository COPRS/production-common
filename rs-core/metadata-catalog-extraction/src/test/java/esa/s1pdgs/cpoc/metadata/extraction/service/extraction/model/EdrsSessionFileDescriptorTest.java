package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class EdrsSessionFileDescriptorTest {

	@Test
    public void equalsConfigFileDecriptor() {
		EqualsVerifier.forClass(EdrsSessionFileDescriptor.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
