package fr.viveris.s1pdgs.mdcatalog.model.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.mdcatalog.model.exception.AbstractCodedException.ErrorCode;

/**
 * Test the exception FilePathException
 */
public class IllegalFileExtensionTest {

	/**
	 * Test getters and constructors
	 */
	@Test
	public void testGettersConstructors() {
		IllegalFileExtension exception = new IllegalFileExtension("exten");
		
		assertEquals(ErrorCode.METADATA_FILE_EXTENSION, exception.getCode());
		assertEquals(ErrorCode.METADATA_FILE_EXTENSION.getCode(), exception.getCode().getCode());
		assertEquals("", exception.getProductName());
	}

	/**
	 * Test get log message
	 */
	@Test
	public void testLogMessage() {
		IllegalFileExtension exception = new IllegalFileExtension("exten");
		
		String log = exception.getLogMessage();
		assertTrue(log.contains("[msg "));
	}

}
