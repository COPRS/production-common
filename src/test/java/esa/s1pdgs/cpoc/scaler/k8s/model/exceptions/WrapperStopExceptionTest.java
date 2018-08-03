package esa.s1pdgs.cpoc.scaler.k8s.model.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.scaler.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.scaler.k8s.model.exceptions.WrapperStopException;

public class WrapperStopExceptionTest {

	@Test
	public void testWrapperStopException() {
		WrapperStopException e1 = new WrapperStopException("ip", "message");
		assertEquals(ErrorCode.K8S_WRAPPER_STOP_ERROR, e1.getCode());
		assertEquals("message", e1.getMessage());
		assertNull(e1.getCause());
		
		String str1 = e1.getLogMessage();
		assertTrue(str1.contains("[podIp ip] [msg message]"));
		
		WrapperStopException e2 = new WrapperStopException("ip", "message", new Throwable("throw"));

		assertEquals(ErrorCode.K8S_WRAPPER_STOP_ERROR, e2.getCode());
		assertEquals("message", e2.getMessage());
		assertEquals("throw", e2.getCause().getMessage());

		String str2 = e2.getLogMessage();
		assertTrue(str2.contains("[podIp ip] [msg message]"));
	}

}
