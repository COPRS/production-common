package esa.s1pdgs.cpoc.common.errors.mqi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ResumeDetails;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublicationError;

/**
 * Test the class KafkaSendException
 * 
 * @author Viveris Technologies
 */
public class MqiPublicationErrorTest {

    /**
     * Test the KafkaSendException
     */
    @Test
    public void testKafkaSendException() {
        MqiPublicationError e1 = new MqiPublicationError("topic-kafka",
                "dto-object", "product-name", "error message",
                new Throwable("throwable message"));

        assertEquals("topic-kafka", e1.getTopic());
        assertEquals("dto-object", e1.getDto());
        assertEquals("product-name", e1.getProductName());
        assertEquals(ErrorCode.MQI_PUBLICATION_ERROR, e1.getCode());
        assertEquals("error message", e1.getMessage());
        assertEquals("throwable message", e1.getCause().getMessage());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[resuming "
                + (new ResumeDetails("topic-kafka", "dto-object")).toString()
                + "]"));
        assertTrue(str1.contains("[productName product-name]"));
        assertTrue(str1.contains("[msg error message]"));
    }

}
