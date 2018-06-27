package fr.viveris.s1pdgs.scaler.k8s.model.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.scaler.AbstractCodedException.ErrorCode;

public class WrapperStatusExceptionTest {

	@Test
	public void testWrapperStatusException() {
		WrapperStatusException e1 = new WrapperStatusException("ip", "name", "message");
		assertEquals(ErrorCode.K8S_WRAPPER_STATUS_ERROR, e1.getCode());
		assertEquals("message", e1.getMessage());
		assertNull(e1.getCause());
		
		String str1 = e1.getLogMessage();
		assertTrue(str1.contains("[podIp ip] [podName name] [msg message]"));
		
		WrapperStatusException e2 = new WrapperStatusException("ip", "name", "message", new Throwable("throw"));

		assertEquals(ErrorCode.K8S_WRAPPER_STATUS_ERROR, e2.getCode());
		assertEquals("message", e2.getMessage());
		assertEquals("throw", e2.getCause().getMessage());

		String str2 = e2.getLogMessage();
		assertTrue(str2.contains("[podIp ip] [podName name] [msg message]"));
	}

}
