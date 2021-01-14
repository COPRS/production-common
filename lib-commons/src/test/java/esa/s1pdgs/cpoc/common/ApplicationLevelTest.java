package esa.s1pdgs.cpoc.common;

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
        assertEquals(10, ApplicationLevel.values().length);
        assertEquals(ApplicationLevel.L0, ApplicationLevel.valueOf("L0"));
        assertEquals(ApplicationLevel.L1, ApplicationLevel.valueOf("L1"));
        assertEquals(ApplicationLevel.L2, ApplicationLevel.valueOf("L2"));
        assertEquals(ApplicationLevel.L0_SEGMENT, ApplicationLevel.valueOf("L0_SEGMENT"));
        assertEquals(ApplicationLevel.S3_L0, ApplicationLevel.valueOf("S3_L0"));
        assertEquals(ApplicationLevel.S3_L1, ApplicationLevel.valueOf("S3_L1"));
        assertEquals(ApplicationLevel.S3_L2, ApplicationLevel.valueOf("S3_L2"));
        assertEquals(ApplicationLevel.S3_PDU, ApplicationLevel.valueOf("S3_PDU"));
        assertEquals(ApplicationLevel.SPP_MBU, ApplicationLevel.valueOf("SPP_MBU"));
        assertEquals(ApplicationLevel.SPP_OBS, ApplicationLevel.valueOf("SPP_OBS"));
    }
}
