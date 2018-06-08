package fr.viveris.s1pdgs.mdcatalog.model.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.mdcatalog.model.exception.AbstractCodedException.ErrorCode;

/**
 * Test the exception IgnoredFileException
 */
public class IgnoredFileExceptionTest {

	/**
	 * Test getters and constructors
	 */
	@Test
	public void testGettersConstructors() {
		IgnoredFileException exception = new IgnoredFileException("product-name",
				"ignored-name");
		
		assertEquals(ErrorCode.METADATA_IGNORE_FILE, exception.getCode());
		assertEquals("product-name", exception.getProductName());
		assertTrue(exception.getMessage().contains("ignored-name"));
	}

	/**
	 * Test get log message
	 */
	@Test
	public void testLogMessage() {
		IgnoredFileException exception = new IgnoredFileException("product-name",
				"ignored-name");
		
		String log = exception.getLogMessage();
		assertTrue(log.contains("[msg "));
		assertTrue(log.contains("ignored-name"));
	}

}
