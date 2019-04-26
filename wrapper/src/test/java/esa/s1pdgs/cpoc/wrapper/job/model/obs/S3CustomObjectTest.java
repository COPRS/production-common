package esa.s1pdgs.cpoc.wrapper.job.model.obs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.wrapper.job.model.obs.S3CustomObject;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object S3CustomObject
 * 
 * @author Viveris Technologies
 */
public class S3CustomObjectTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        S3CustomObject obj =
                new S3CustomObject(ProductFamily.AUXILIARY_FILE, "key-obs");
        assertEquals(ProductFamily.AUXILIARY_FILE, obj.getFamily());
        assertEquals("key-obs", obj.getKey());
    }

    /**
     * Test to string
     */
    @Test
    public void testToString() {
        S3CustomObject obj =
                new S3CustomObject(ProductFamily.L0_SLICE, "key-obs");
        String str = obj.toString();
        assertTrue(str.startsWith("{"));
        assertTrue(str.contains("family: L0_SLICE"));
        assertTrue(str.contains("key: key-obs"));
        assertTrue(str.endsWith("}"));
        
        str = obj.toStringForExtendedClasses();
        assertFalse(str.startsWith("{"));
        assertTrue(str.contains("family: L0_SLICE"));
        assertTrue(str.contains("key: key-obs"));
        assertFalse(str.endsWith("}"));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(S3CustomObject.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
