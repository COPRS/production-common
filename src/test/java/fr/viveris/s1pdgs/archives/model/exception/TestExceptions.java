package fr.viveris.s1pdgs.archives.model.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.archives.model.exception.AbstractCodedException.ErrorCode;
import fr.viveris.s1pdgs.archives.model.ProductFamily;

/**
 * Test the Exceptions
 * @author Cyrielle
 *
 */
public class TestExceptions {


	/**
	 * Test the ObsS3Exception
	 */
	@Test
	public void testObsS3Exception() {
		ObjectStorageException e1 = new ObjectStorageException(ProductFamily.L0_PRODUCT, "key1", new Throwable("throwable message"));

		assertEquals("key1", e1.getKey());
		assertEquals(ProductFamily.L0_PRODUCT, e1.getFamily());
		assertEquals(ErrorCode.OBS_ERROR, e1.getCode());
		assertEquals("throwable message", e1.getMessage());
		assertEquals("throwable message", e1.getCause().getMessage());

		String str1 = e1.getLogMessage();
		assertTrue(str1.contains("[key key1]"));
		assertTrue(str1.contains("[family L0_PRODUCT]"));
		assertTrue(str1.contains("[msg throwable message]"));
	}

	/**
	 * Test the ObsUnknownObjectException
	 */
	@Test
	public void testObsUnknownObjectException() {
		ObsUnknownObjectException e1 = new ObsUnknownObjectException(ProductFamily.RAW, "key1");

		assertEquals("key1", e1.getKey());
		assertEquals(ProductFamily.RAW, e1.getFamily());
		assertEquals(ErrorCode.OBS_UNKOWN_OBJ, e1.getCode());
		assertNull(e1.getCause());

		String str1 = e1.getLogMessage();
		assertTrue(str1.contains("[family RAW]"));
		assertTrue(str1.contains("[key key1]"));
	}

}
