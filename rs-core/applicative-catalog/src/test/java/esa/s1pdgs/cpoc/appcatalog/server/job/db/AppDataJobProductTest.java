package esa.s1pdgs.cpoc.appcatalog.server.job.db;

import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class AppDataJobProductTest {
    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(AppDataJobProduct.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
