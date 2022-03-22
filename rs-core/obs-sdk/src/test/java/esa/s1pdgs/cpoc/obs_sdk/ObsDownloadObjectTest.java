package esa.s1pdgs.cpoc.obs_sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object S3DownloadFile
 * 
 * @author Viveris Technologies
 */
public class ObsDownloadObjectTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
    	ObsDownloadObject obj = new ObsDownloadObject(ProductFamily.AUXILIARY_FILE,
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
    	ObsDownloadObject obj = new ObsDownloadObject(ProductFamily.L0_SLICE,
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
        EqualsVerifier.forClass(ObsDownloadObject.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
