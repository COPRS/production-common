package esa.s1pdgs.cpoc.mdc.worker.extraction.model;

import org.junit.Test;

import esa.s1pdgs.cpoc.mdc.worker.extraction.model.ConfigFileDescriptor;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class ConfigFileDescriptorTest {

	@Test
    public void equalsConfigFileDecriptor() {
		EqualsVerifier.forClass(ConfigFileDescriptor.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
