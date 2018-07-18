package esa.s1pdgs.cpoc.mdcatalog.model.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.mdcatalog.model.ProductFamily;
import esa.s1pdgs.cpoc.mdcatalog.model.exception.ObjectStorageException;
import esa.s1pdgs.cpoc.mdcatalog.model.exception.AbstractCodedException.ErrorCode;

/**
 * Test the exception ObjectStorageException
 */
public class ObjectStorageExceptionTest {

	/**
	 * Test getters and constructors
	 */
	@Test
	public void testGettersConstructors() {
		Throwable cause = new Exception("cause exception");
		ObjectStorageException exception = new ObjectStorageException(ProductFamily.L0_ACN,
				"key-obs", cause);
		
		assertEquals(ErrorCode.OBS_ERROR, exception.getCode());
		assertEquals("key-obs", exception.getProductName());
		assertEquals("key-obs", exception.getKey());
		assertEquals(ProductFamily.L0_ACN, exception.getFamily());
		assertEquals(cause, exception.getCause());
		assertTrue(exception.getMessage().contains("cause exception"));
	}

	/**
	 * Test get log message
	 */
	@Test
	public void testLogMessage() {
		Throwable cause = new Exception("cause exception");
		ObjectStorageException exception = new ObjectStorageException(ProductFamily.EDRS_SESSION,
				"key-obs", cause);
		
		String log = exception.getLogMessage();
		assertTrue(log.contains("[family EDRS_SESSION]"));
		assertTrue(log.contains("[key key-obs]"));
		assertTrue(log.contains("[msg "));
		assertTrue(log.contains("cause exception]"));
	}

}
