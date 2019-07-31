package esa.s1pdgs.cpoc.obs_sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadFile;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object S3DownloadFile
 * 
 * @author Viveris Technologies
 */
public class ObsDownloadFileTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        ObsDownloadFile obj = new ObsDownloadFile(ProductFamily.AUXILIARY_FILE,
                "key-obs", "target-dir");
        assertEquals(ProductFamily.AUXILIARY_FILE, obj.getFamily());
        assertEquals("key-obs", obj.getKey());
        assertEquals("target-dir", obj.getTargetDir());
    }

    /**
     * Test to string
     */
    @Test
    public void testToString() {
        ObsDownloadFile obj = new ObsDownloadFile(ProductFamily.L0_SLICE,
                "key-obs", "target-dir");
        String str = obj.toString();
        assertTrue(str.contains("family: L0_SLICE"));
        assertTrue(str.contains("key: key-obs"));
        assertTrue(str.contains("targetDir: target-dir"));
    }

    /**
     * Check equals and hashcode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(ObsDownloadFile.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
