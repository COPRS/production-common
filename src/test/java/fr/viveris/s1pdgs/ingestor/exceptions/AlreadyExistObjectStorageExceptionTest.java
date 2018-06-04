package fr.viveris.s1pdgs.ingestor.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.ingestor.exceptions.AbstractFileException.ErrorCode;

/**
 * Test the exception AlreadyExistObjectStorageException
 */
public class AlreadyExistObjectStorageExceptionTest {

	/**
	 * Test getters and constructors
	 */
	@Test
	public void testGettersConstructors() {
		Throwable cause = new Exception("cause exception");
		AlreadyExistObjectStorageException exception = new AlreadyExistObjectStorageException("product-name",
				cause);
		
		assertEquals(ErrorCode.OBS_ALREADY_EXIST, exception.getCode());
		assertEquals("product-name", exception.getProductName());
		assertEquals(cause, exception.getCause());
		assertTrue(exception.getMessage().contains("cause exception"));
	}

	/**
	 * Test get log message
	 */
	@Test
	public void testLogMessage() {
		Throwable cause = new Exception("cause exception");
		AlreadyExistObjectStorageException exception = new AlreadyExistObjectStorageException("product-name",
				cause);
		
		String log = exception.getLogMessage();
		assertTrue(log.contains("[msg "));
		assertTrue(log.contains("cause exception]"));
	}
}
