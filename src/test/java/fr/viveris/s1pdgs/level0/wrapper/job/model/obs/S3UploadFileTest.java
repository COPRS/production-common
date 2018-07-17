package fr.viveris.s1pdgs.level0.wrapper.job.model.obs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import fr.viveris.s1pdgs.common.ProductFamily;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object S3CustomObject
 * 
 * @author Viveris Technologies
 */
public class S3UploadFileTest {

    /**
     * File
     */
    private static final File FILE = new File("test-file");

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        S3UploadFile obj =
                new S3UploadFile(ProductFamily.AUXILIARY_FILE, "key-obs", FILE);
        assertEquals(ProductFamily.AUXILIARY_FILE, obj.getFamily());
        assertEquals("key-obs", obj.getKey());
        assertEquals(FILE, obj.getFile());
    }

    /**
     * Test to string
     */
    @Test
    public void testToString() {
        S3UploadFile obj =
                new S3UploadFile(ProductFamily.L0_PRODUCT, "key-obs", FILE);
        String str = obj.toString();
        assertTrue(str.contains("family: L0_PRODUCT"));
        assertTrue(str.contains("key: key-obs"));
        assertTrue(str.contains("file: " + FILE));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(S3UploadFile.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
