package esa.s1pdgs.cpoc.appcatalog.server.job.db;

import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class AppDataJobFileTest {
    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(AppDataJobFile.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
