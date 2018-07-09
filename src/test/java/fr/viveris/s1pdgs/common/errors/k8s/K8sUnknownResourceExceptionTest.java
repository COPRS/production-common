package fr.viveris.s1pdgs.common.errors.k8s;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import fr.viveris.s1pdgs.common.errors.AbstractCodedException.ErrorCode;

/**
 * Test the exception K8sUnknownResourceException
 * 
 * @author Viveris Technologies
 */
public class K8sUnknownResourceExceptionTest {

    /**
     * Test getters and log
     */
    @Test
    public void testK8sUnknownResourceException() {
        K8sUnknownResourceException e1 =
                new K8sUnknownResourceException("message");
        assertEquals(ErrorCode.K8S_UNKNOWN_RESOURCE, e1.getCode());
        assertEquals("message", e1.getMessage());
        assertNull(e1.getCause());
    }

}
