package esa.s1pdgs.cpoc.wrapper.job.model.mqi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.wrapper.job.model.mqi.ObsQueueMessage;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object QueueMessage
 * 
 * @author Viveris Technologies
 */
public class ObsQueueMessageTest {

    /**
     * Test constructors
     */
    @Test
    public void testConstructors() {
        ObsQueueMessage obj = new ObsQueueMessage(ProductFamily.AUXILIARY_FILE,
                "product-name", "key");
        assertEquals(ProductFamily.AUXILIARY_FILE, obj.getFamily());
        assertEquals("product-name", obj.getProductName());
        assertEquals("key", obj.getKeyObs());    
    }

    /**
     * Test to string
     */
    @Test
    public void testToString() {
        ObsQueueMessage obj = new ObsQueueMessage(ProductFamily.EDRS_SESSION,
                "product-name", "key");
        String str = obj.toString();
        assertTrue(str.contains("family: EDRS_SESSION"));
        assertTrue(str.contains("productName: product-name"));
        assertTrue(str.contains("keyObs: key"));
    }

    /**
     * Check equals and hascode methods
     */
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(ObsQueueMessage.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
