package esa.s1pdgs.cpoc.common.errors.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.processing.IpfExecutionWorkerProcessExecutionException;

/**
 * Test the ProcessExecutionException
 * 
 * @author Viveris Technologies
 */
public class IpfExecutionWorkerProcessExecutionExceptionTest {

    /**
     * Test the ProcessExecutionException
     */
    @Test
    public void testProcessExecutionException() {
        IpfExecutionWorkerProcessExecutionException e1 =
                new IpfExecutionWorkerProcessExecutionException(139, "erreur message");

        assertEquals(ErrorCode.PROCESS_EXIT_ERROR, e1.getCode());
        assertEquals(139, e1.getExitCode());
        assertEquals("erreur message", e1.getMessage());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[exitCode 139]"));
        assertTrue(str1.contains("[msg erreur message]"));
    }

}
