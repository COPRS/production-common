package esa.s1pdgs.cpoc.mdcatalog.model;

import org.junit.Test;

import esa.s1pdgs.cpoc.mdcatalog.model.L1OutputFileDescriptor;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class L1OutputFileDescriptorTest {

	@Test
    public void equalsConfigFileDecriptor() {
		EqualsVerifier.forClass(L1OutputFileDescriptor.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }
}
