package esa.s1pdgs.cpoc.scaler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.scaler.InternalErrorException;
import esa.s1pdgs.cpoc.scaler.AbstractCodedException.ErrorCode;

public class InternalErrorExceptionTest {


	/**
	 * Test the InternalErrorException
	 */
	@Test
	public void testInternalErrorException() {
		InternalErrorException e1 = new InternalErrorException("erreur message");

		assertEquals(ErrorCode.INTERNAL_ERROR, e1.getCode());
		assertEquals("erreur message", e1.getMessage());
		assertNull(e1.getCause());

		String str1 = e1.getLogMessage();
		assertTrue(str1.contains("[msg erreur message]"));

		InternalErrorException e2 = new InternalErrorException("error message", new Throwable("tutu"));

		assertEquals(ErrorCode.INTERNAL_ERROR, e2.getCode());
		assertEquals("error message", e2.getMessage());
		assertEquals("tutu", e2.getCause().getMessage());

		String str2 = e2.getLogMessage();
		assertTrue(str2.contains("[msg error message]"));
	}
}
