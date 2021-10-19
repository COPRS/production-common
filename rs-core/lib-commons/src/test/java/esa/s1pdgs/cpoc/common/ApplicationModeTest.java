package esa.s1pdgs.cpoc.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test the enumeration ApplicationMode
 * 
 * @author Viveris Technologies
 */
public class ApplicationModeTest {

    /**
     * Test default method of enumeration
     */
    @Test
    public void testValueOf() {
        assertEquals(2, ApplicationMode.values().length);
        assertEquals(ApplicationMode.PROD, ApplicationMode.valueOf("PROD"));
        assertEquals(ApplicationMode.TEST, ApplicationMode.valueOf("TEST"));
    }
}
