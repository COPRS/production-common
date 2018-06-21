package fr.viveris.s1pdgs.level0.wrapper.model.kafka;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;
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
        ObsQueueMessage obj = new ObsQueueMessage(ProductFamily.CONFIG,
                "product-name", "key");
        assertEquals(ProductFamily.CONFIG, obj.getFamily());
        assertEquals("product-name", obj.getProductName());
        assertEquals("key", obj.getKeyObs());
    }

    /**
     * Test to string
     */
    @Test
    public void testToString() {
        ObsQueueMessage obj =
                new ObsQueueMessage(ProductFamily.RAW, "product-name", "key");
        String str = obj.toString();
        assertTrue(str.contains("family: RAW"));
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
