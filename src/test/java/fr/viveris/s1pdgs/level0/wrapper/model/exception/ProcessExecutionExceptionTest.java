package fr.viveris.s1pdgs.level0.wrapper.model.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.level0.wrapper.model.exception.AbstractCodedException.ErrorCode;

/**
 * Test the ProcessExecutionException
 * 
 * @author Viveris Technologies
 */
public class ProcessExecutionExceptionTest {

    /**
     * Test the ProcessExecutionException
     */
    @Test
    public void testProcessExecutionException() {
        ProcessExecutionException e1 =
                new ProcessExecutionException(139, "erreur message");

        assertEquals(ErrorCode.PROCESS_EXIT_ERROR, e1.getCode());
        assertEquals(139, e1.getExitCode());
        assertEquals("erreur message", e1.getMessage());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[exitCode 139]"));
        assertTrue(str1.contains("[msg erreur message]"));
    }

}
