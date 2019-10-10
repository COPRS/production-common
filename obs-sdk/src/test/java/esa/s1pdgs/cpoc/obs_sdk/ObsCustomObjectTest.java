package esa.s1pdgs.cpoc.obs_sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.obs_sdk.ObsCustomObject;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object S3CustomObject
 * 
 * @author Viveris Technologies
 */
public class ObsCustomObjectTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        ObsCustomObject obj =
                new ObsCustomObject(ProductFamily.AUXILIARY_FILE, "key-obs");
        assertEquals(ProductFamily.AUXILIARY_FILE, obj.getFamily());
        assertEquals("key-obs", obj.getKey());
    }

    /**
     * Test to string
     */
    @Test
    public void testToString() {
        ObsCustomObject obj =
                new ObsCustomObject(ProductFamily.L0_SLICE, "key-obs");
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
     * Check equals and hashcode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(ObsCustomObject.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
