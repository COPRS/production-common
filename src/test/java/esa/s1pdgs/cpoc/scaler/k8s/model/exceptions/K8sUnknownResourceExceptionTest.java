package esa.s1pdgs.cpoc.scaler.k8s.model.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import esa.s1pdgs.cpoc.scaler.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.scaler.k8s.model.exceptions.K8sUnknownResourceException;

public class K8sUnknownResourceExceptionTest {

	@Test
	public void testK8sUnknownResourceException() {
		K8sUnknownResourceException e1 = new K8sUnknownResourceException("message");
		assertEquals(ErrorCode.K8S_UNKNOWN_RESOURCE, e1.getCode());
		assertEquals("message", e1.getMessage());
		assertNull(e1.getCause());
	}

}
