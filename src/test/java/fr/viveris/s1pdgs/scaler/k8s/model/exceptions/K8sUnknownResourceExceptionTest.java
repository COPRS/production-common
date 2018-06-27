package fr.viveris.s1pdgs.scaler.k8s.model.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import fr.viveris.s1pdgs.scaler.AbstractCodedException.ErrorCode;

public class K8sUnknownResourceExceptionTest {

	@Test
	public void testK8sUnknownResourceException() {
		K8sUnknownResourceException e1 = new K8sUnknownResourceException("message");
		assertEquals(ErrorCode.K8S_UNKNOWN_RESOURCE, e1.getCode());
		assertEquals("message", e1.getMessage());
		assertNull(e1.getCause());
	}

}
