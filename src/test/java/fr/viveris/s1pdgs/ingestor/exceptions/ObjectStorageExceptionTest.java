package fr.viveris.s1pdgs.ingestor.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.ingestor.exceptions.AbstractFileException.ErrorCode;
import fr.viveris.s1pdgs.ingestor.files.model.ProductFamily;

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
				"key-obs", ProductFamily.AUXILIARY_FILE, cause);
		
		assertEquals(ErrorCode.OBS_ERROR, exception.getCode());
		assertEquals("product-name", exception.getProductName());
		assertEquals("key-obs", exception.getKey());
		assertEquals(ProductFamily.AUXILIARY_FILE, exception.getFamily());
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
				"key-obs", ProductFamily.EDRS_SESSION, cause);
		
		String log = exception.getLogMessage();
		assertTrue(log.contains("[family EDRS_SESSION]"));
		assertTrue(log.contains("[key key-obs]"));
		assertTrue(log.contains("[msg "));
		assertTrue(log.contains("cause exception]"));
	}

}
