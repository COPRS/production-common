package fr.viveris.s1pdgs.level0.wrapper.model.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.level0.wrapper.model.ResumeDetails;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.AbstractCodedException.ErrorCode;

/**
 * Test the class KafkaSendException
 * @author Viveris Technologies
 *
 */
public class KafkaSendExceptionTest {

    /**
     * Test the KafkaSendException
     */
    @Test
    public void testKafkaSendException() {
        KafkaSendException e1 = new KafkaSendException("topic-kafka", "dto-object", "product-name", "error message",
                new Throwable("throwable message"));

        assertEquals("topic-kafka", e1.getTopic());
        assertEquals("dto-object", e1.getDto());
        assertEquals("product-name", e1.getProductName());
        assertEquals(ErrorCode.KAFKA_SEND_ERROR, e1.getCode());
        assertEquals("error message", e1.getMessage());
        assertEquals("throwable message", e1.getCause().getMessage());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[resuming " + (new ResumeDetails("topic-kafka", "dto-object")).toString() + "]"));
        assertTrue(str1.contains("[productName product-name]"));
        assertTrue(str1.contains("[msg error message]"));
    }

}
