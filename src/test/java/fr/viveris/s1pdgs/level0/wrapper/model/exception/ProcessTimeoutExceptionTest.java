package fr.viveris.s1pdgs.level0.wrapper.model.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test the class ProcessTimeoutException
 * 
 * @author Viveris Technologies
 */
public class ProcessTimeoutExceptionTest {

    /**
     * Test the InvalidFormatProduct
     */
    @Test
    public void testProcessTimeoutException() {
        ProcessTimeoutException e1 =
                new ProcessTimeoutException("erreur message");

        assertEquals(
                fr.viveris.s1pdgs.level0.wrapper.model.exception.AbstractCodedException.ErrorCode.PROCESS_TIMEOUT,
                e1.getCode());
        assertEquals("erreur message", e1.getMessage());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[msg erreur message]"));
    }

}
