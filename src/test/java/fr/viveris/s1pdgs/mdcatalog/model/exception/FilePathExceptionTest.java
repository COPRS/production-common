package fr.viveris.s1pdgs.mdcatalog.model.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.mdcatalog.model.exception.AbstractCodedException.ErrorCode;

/**
 * Test the exception FilePathException
 */
public class FilePathExceptionTest {

	/**
	 * Test getters and constructors
	 */
	@Test
	public void testGettersConstructors() {
		FilePathException exception = new FilePathException("product-name",
				"path-test", "family", "msg exception");
		
		assertEquals(ErrorCode.METADATA_FILE_PATH, exception.getCode());
		assertEquals("product-name", exception.getProductName());
		assertEquals("path-test", exception.getPath());
		assertEquals("family", exception.getFamily());
		assertTrue(exception.getMessage().contains("msg exception"));
	}

	/**
	 * Test get log message
	 */
	@Test
	public void testLogMessage() {
		FilePathException exception = new FilePathException("product-name",
				"path-test", "family", "msg exception");
		
		String log = exception.getLogMessage();
		assertTrue(log.contains("[path path-test]"));
		assertTrue(log.contains("[family family]"));
		assertTrue(log.contains("[msg "));
		assertTrue(log.contains("msg exception]"));
	}

}
