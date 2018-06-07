package fr.viveris.s1pdgs.mdcatalog.model.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.mdcatalog.model.exception.AbstractCodedException.ErrorCode;

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
		ObjectStorageException exception = new ObjectStorageException("product-name",
				"key-obs", "bucket-name", cause);
		
		assertEquals(ErrorCode.OBS_ERROR, exception.getCode());
		assertEquals("product-name", exception.getProductName());
		assertEquals("key-obs", exception.getKey());
		assertEquals("bucket-name", exception.getBucket());
		assertEquals(cause, exception.getCause());
		assertTrue(exception.getMessage().contains("cause exception"));
	}

	/**
	 * Test get log message
	 */
	@Test
	public void testLogMessage() {
		Throwable cause = new Exception("cause exception");
		ObjectStorageException exception = new ObjectStorageException("product-name",
				"key-obs", "bucket-name", cause);
		
		String log = exception.getLogMessage();
		assertTrue(log.contains("[bucket bucket-name]"));
		assertTrue(log.contains("[key key-obs]"));
		assertTrue(log.contains("[msg "));
		assertTrue(log.contains("cause exception]"));
	}

}
