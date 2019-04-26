package esa.s1pdgs.cpoc.mdcatalog.extraction.model;

import org.junit.Test;

import esa.s1pdgs.cpoc.mdcatalog.extraction.model.L0OutputFileDescriptor;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class L0OutputFileDescriptorTest {

	@Test
    public void equalsConfigFileDecriptor() {
		EqualsVerifier.forClass(L0OutputFileDescriptor.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
