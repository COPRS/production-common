package esa.s1pdgs.cpoc.ingestor.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.ingestor.exceptions.KafkaSendException;
import esa.s1pdgs.cpoc.ingestor.exceptions.AbstractFileException.ErrorCode;
import esa.s1pdgs.cpoc.ingestor.files.model.dto.KafkaConfigFileDto;

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
		KafkaSendException exception = new KafkaSendException("topic-name", "dto-obj", "product-name", "msg exception",
				cause);

		assertEquals(ErrorCode.KAFKA_SEND_ERROR, exception.getCode());
		assertEquals("product-name", exception.getProductName());
		assertEquals("dto-obj", exception.getDto());
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
		KafkaConfigFileDto dto = new KafkaConfigFileDto("product-name", "key-obs");
		KafkaSendException exception = new KafkaSendException("topic-name", dto, "product-name", "msg exception",
				cause);

		String log = exception.getLogMessage();
		assertTrue(log.contains("[topic topic-name]"));
		assertTrue(log.contains("[dto " + dto.toString() + "]"));
		assertTrue(log.contains("[msg msg exception]"));
	}

}
