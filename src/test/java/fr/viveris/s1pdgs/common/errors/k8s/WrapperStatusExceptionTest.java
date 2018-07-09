package fr.viveris.s1pdgs.common.errors.k8s;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.common.errors.AbstractCodedException.ErrorCode;

/**
 * Test the exception WrapperStatusException
 * 
 * @author Viveris Technologies
 */
public class WrapperStatusExceptionTest {

    /**
     * Test getters and log
     */
    @Test
    public void testWrapperStatusException() {
        WrapperStatusException e1 =
                new WrapperStatusException("ip", "name", "message");
        assertEquals(ErrorCode.K8S_WRAPPER_STATUS_ERROR, e1.getCode());
        assertEquals("message", e1.getMessage());
        assertEquals("ip", e1.getIpAddress());
        assertEquals("name", e1.getName());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[podIp ip] [podName name] [msg message]"));

        WrapperStatusException e2 = new WrapperStatusException("ip", "name",
                "message", new Throwable("throw"));

        assertEquals(ErrorCode.K8S_WRAPPER_STATUS_ERROR, e2.getCode());
        assertEquals("message", e2.getMessage());
        assertEquals("throw", e2.getCause().getMessage());

        String str2 = e2.getLogMessage();
        assertTrue(str2.contains("[podIp ip] [podName name] [msg message]"));
    }

}
