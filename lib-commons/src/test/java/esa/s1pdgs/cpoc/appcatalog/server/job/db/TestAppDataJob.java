package esa.s1pdgs.cpoc.appcatalog.server.job.db;

import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class TestAppDataJob {
	
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(AppDataJob .class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }


}
