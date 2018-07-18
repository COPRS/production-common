package esa.s1pdgs.cpoc.mdcatalog.model.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.mdcatalog.model.ProductFamily;
import esa.s1pdgs.cpoc.mdcatalog.model.exception.ObsUnknownObjectException;
import esa.s1pdgs.cpoc.mdcatalog.model.exception.AbstractCodedException.ErrorCode;

/**
 * Test the exception ObjectStorageException
 */
public class ObsUnknownObjectExceptionTest {

	/**
	 * Test getters and constructors
	 */
	@Test
	public void testGettersConstructors() {
		ObsUnknownObjectException exception = new ObsUnknownObjectException(ProductFamily.L0_ACN,
				"key-obs");
		
		assertEquals(ErrorCode.OBS_UNKOWN_OBJ, exception.getCode());
		assertEquals("key-obs", exception.getProductName());
		assertEquals("key-obs", exception.getKey());
		assertEquals(ProductFamily.L0_ACN, exception.getFamily());
	}

	/**
	 * Test get log message
	 */
	@Test
	public void testLogMessage() {
		ObsUnknownObjectException exception = new ObsUnknownObjectException(ProductFamily.EDRS_SESSION,
				"key-obs");
		
		String log = exception.getLogMessage();
		assertTrue(log.contains("[family EDRS_SESSION]"));
		assertTrue(log.contains("[key key-obs]"));
	}

}
