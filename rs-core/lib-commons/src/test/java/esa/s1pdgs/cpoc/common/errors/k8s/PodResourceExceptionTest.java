package esa.s1pdgs.cpoc.common.errors.k8s;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.k8s.PodResourceException;

/**
 * Test the exception PodResourceException
 * 
 * @author Viveris Technologies
 */
public class PodResourceExceptionTest {

    /**
     * Test getters and log
     */
    @Test
    public void testPodResourceException() {
        PodResourceException e1 = new PodResourceException("message");
        assertEquals(ErrorCode.K8S_NO_TEMPLATE_POD, e1.getCode());
        assertEquals("message", e1.getMessage());
        assertNull(e1.getCause());

        PodResourceException e2 =
                new PodResourceException("message", new Throwable("throw"));

        assertEquals(ErrorCode.K8S_NO_TEMPLATE_POD, e2.getCode());
        assertEquals("message", e2.getMessage());
        assertEquals("throw", e2.getCause().getMessage());
    }

}
