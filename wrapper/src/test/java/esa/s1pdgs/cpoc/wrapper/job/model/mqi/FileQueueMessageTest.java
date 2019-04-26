package esa.s1pdgs.cpoc.wrapper.job.model.mqi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.wrapper.job.model.mqi.FileQueueMessage;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object FileQueueMessage
 * 
 * @author Viveris Technologies
 */
public class FileQueueMessageTest {

    /**
     * File
     */
    private static final File FILE = new File("test-file");

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        FileQueueMessage obj = new FileQueueMessage(ProductFamily.AUXILIARY_FILE,
                "product-name", FILE);
        assertEquals(ProductFamily.AUXILIARY_FILE, obj.getFamily());
        assertEquals("product-name", obj.getProductName());
        assertEquals(FILE, obj.getFile());
    }

    /**
     * Test to string
     */
    @Test
    public void testToString() {
        FileQueueMessage obj = new FileQueueMessage(ProductFamily.L1_ACN,
                "product-name", FILE);
        String str = obj.toString();
        assertTrue(str.contains("family: L1_ACN"));
        assertTrue(str.contains("productName: product-name"));
        assertTrue(str.contains("file: " + FILE));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(FileQueueMessage.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
