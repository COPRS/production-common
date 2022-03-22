package esa.s1pdgs.cpoc.common.errors.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.processing.IpfExecutionWorkerProcessTimeoutException;

/**
 * Test the class ProcessTimeoutException
 * 
 * @author Viveris Technologies
 */
public class IpfExecutionWorkerProcessTimeoutExceptionTest {

    /**
     * Test the InvalidFormatProduct
     */
    @Test
    public void testProcessTimeoutException() {
        IpfExecutionWorkerProcessTimeoutException e1 =
                new IpfExecutionWorkerProcessTimeoutException("erreur message");

        assertEquals(
                ErrorCode.PROCESS_TIMEOUT,
                e1.getCode());
        assertEquals("erreur message", e1.getMessage());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[msg erreur message]"));
    }

}
