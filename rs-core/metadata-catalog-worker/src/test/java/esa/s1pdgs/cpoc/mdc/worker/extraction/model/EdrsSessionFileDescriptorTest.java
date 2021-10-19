package esa.s1pdgs.cpoc.mdc.worker.extraction.model;

import org.junit.Test;

import esa.s1pdgs.cpoc.mdc.worker.extraction.model.EdrsSessionFileDescriptor;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class EdrsSessionFileDescriptorTest {

	@Test
    public void equalsConfigFileDecriptor() {
		EqualsVerifier.forClass(EdrsSessionFileDescriptor.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
