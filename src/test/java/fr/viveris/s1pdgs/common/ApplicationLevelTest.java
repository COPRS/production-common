package fr.viveris.s1pdgs.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test the enumeration ApplicationLevel
 * 
 * @author Viveris Technologies
 */
public class ApplicationLevelTest {

    /**
     * Test default method of enumeration
     */
    @Test
    public void testValueOf() {
        assertEquals(2, ApplicationLevel.values().length);
        assertEquals(ApplicationLevel.L0, ApplicationLevel.valueOf("L0"));
    }
}
