package fr.viveris.s1pdgs.level0.wrapper.job.model.obs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.common.ProductFamily;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object S3DownloadFile
 * 
 * @author Viveris Technologies
 */
public class S3DownloadFileTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        S3DownloadFile obj = new S3DownloadFile(ProductFamily.AUXILIARY_FILE,
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
        S3DownloadFile obj = new S3DownloadFile(ProductFamily.L0_PRODUCT,
                "key-obs", "target-dir");
        String str = obj.toString();
        assertTrue(str.contains("family: L0_PRODUCT"));
        assertTrue(str.contains("key: key-obs"));
        assertTrue(str.contains("targetDir: target-dir"));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(S3DownloadFile.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
