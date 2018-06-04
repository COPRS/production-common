package fr.viveris.s1pdgs.ingestor.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.ingestor.exceptions.AbstractFileException.ErrorCode;

/**
 * Test the exception KafkaSendException
 */
public class KafkaSendExceptionTest {

	/**
	 * Test getters and constructors
	 */
	@Test
	public void testGettersConstructors() {
		Throwable cause = new Exception("cause exception");
		KafkaSendException exception = new KafkaSendException("topic-name", "product-name",
				"msg exception", cause);
		
		assertEquals(ErrorCode.KAFKA_SEND_ERROR, exception.getCode());
		assertEquals("product-name", exception.getProductName());
		assertEquals("topic-name", exception.getTopic());
		assertEquals("msg exception", exception.getMessage());
		assertEquals(cause, exception.getCause());
	}

	/**
	 * Test get log message
	 */
	@Test
	public void testLogMessage() {
		Throwable cause = new Exception("cause exception");
		KafkaSendException exception = new KafkaSendException("topic-name", "product-name",
				"msg exception", cause);
		
		String log = exception.getLogMessage();
		assertTrue(log.contains("[topic topic-name]"));
		assertTrue(log.contains("[msg msg exception]"));
	}

}
