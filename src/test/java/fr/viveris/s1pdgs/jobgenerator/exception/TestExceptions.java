package fr.viveris.s1pdgs.jobgenerator.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fr.viveris.s1pdgs.jobgenerator.exception.AbstractCodedException.ErrorCode;

/**
 * Test the Exceptions
 * @author Cyrielle
 *
 */
public class TestExceptions {

	/**
	 * Test the JobGenerationException
	 */
	@Test
	public void testJobGenerationException() {
		JobGenerationException e = new JobGenerationException("task-table", ErrorCode.JOB_GENERATOR_INIT_FAILED,
				"errer message");

		assertEquals("task-table", e.getTaskTable());
		assertEquals(ErrorCode.JOB_GENERATOR_INIT_FAILED, e.getCode());
		assertEquals("errer message", e.getMessage());

		String str = e.getLogMessage();
		assertTrue(str.contains("[taskTable task-table]"));
		assertTrue(str.contains("[msg errer message]"));

		JobGenerationException e1 = new JobGenerationException("tasktable", ErrorCode.INTERNAL_ERROR, "error message",
				new Throwable("throwable message"));

		assertEquals("tasktable", e1.getTaskTable());
		assertEquals(ErrorCode.INTERNAL_ERROR, e1.getCode());
		assertEquals("error message", e1.getMessage());
		assertEquals("throwable message", e1.getCause().getMessage());

		String str1 = e1.getLogMessage();
		assertTrue(str1.contains("[taskTable tasktable]"));
		assertTrue(str1.contains("[msg error message]"));
	}

	/**
	 * Test the BuildTaskTableException
	 */
	@Test
	public void testBuildTaskTableException() {
		JobGenerationException e1 = new BuildTaskTableException("tasktable", "error message",
				new Throwable("throwable message"));

		assertEquals("tasktable", e1.getTaskTable());
		assertEquals(ErrorCode.JOB_GENERATOR_INIT_FAILED, e1.getCode());
		assertEquals("error message", e1.getMessage());
		assertEquals("throwable message", e1.getCause().getMessage());

		String str1 = e1.getLogMessage();
		assertTrue(str1.contains("[taskTable tasktable]"));
		assertTrue(str1.contains("[msg error message]"));
	}

	/**
	 * Test the ObsS3Exception
	 */
	@Test
	public void testObsS3Exception() {
		ObsS3Exception e1 = new ObsS3Exception("key1", "bucket1", new Throwable("throwable message"));

		assertEquals("key1", e1.getKey());
		assertEquals("bucket1", e1.getBucket());
		assertEquals(ErrorCode.OBS_ERROR, e1.getCode());
		assertEquals("throwable message", e1.getMessage());
		assertEquals("throwable message", e1.getCause().getMessage());

		String str1 = e1.getLogMessage();
		assertTrue(str1.contains("[bucket bucket1]"));
		assertTrue(str1.contains("[key key1]"));
		assertTrue(str1.contains("[msg throwable message]"));
	}

	/**
	 * Test the ObsUnknownObjectException
	 */
	@Test
	public void testObsUnknownObjectException() {
		ObsUnknownObjectException e1 = new ObsUnknownObjectException("key1", "bucket1", "erreur message");

		assertEquals("key1", e1.getKey());
		assertEquals("bucket1", e1.getBucket());
		assertEquals(ErrorCode.OBS_UNKOWN_OBJ, e1.getCode());
		assertEquals("erreur message", e1.getMessage());
		assertNull(e1.getCause());

		String str1 = e1.getLogMessage();
		assertTrue(str1.contains("[bucket bucket1]"));
		assertTrue(str1.contains("[key key1]"));
		assertTrue(str1.contains("[msg erreur message]"));
	}

	/**
	 * Test the MissingRoutingEntryException
	 */
	@Test
	public void testMissingRoutingEntryException() {
		MissingRoutingEntryException e1 = new MissingRoutingEntryException("erreur message");

		assertEquals(ErrorCode.MISSING_ROUTING_ENTRY, e1.getCode());
		assertEquals("erreur message", e1.getMessage());
		assertNull(e1.getCause());

		String str1 = e1.getLogMessage();
		assertTrue(str1.contains("[msg erreur message]"));
	}

	/**
	 * Test the MaxNumberTaskTablesReachException
	 */
	@Test
	public void testMaxNumberTaskTablesReachException() {
		MaxNumberTaskTablesReachException e1 = new MaxNumberTaskTablesReachException("erreur message");

		assertEquals(ErrorCode.MAX_NUMBER_TASKTABLE_REACH, e1.getCode());
		assertEquals("erreur message", e1.getMessage());
		assertNull(e1.getCause());

		String str1 = e1.getLogMessage();
		assertTrue(str1.contains("[msg erreur message]"));
	}

	/**
	 * Test the MaxNumberCachedSessionsReachException
	 */
	@Test
	public void testMaxNumberCachedSessionsReachException() {
		MaxNumberCachedSessionsReachException e1 = new MaxNumberCachedSessionsReachException("erreur message");

		assertEquals(ErrorCode.MAX_NUMBER_CACHED_SESSIONS_REACH, e1.getCode());
		assertEquals("erreur message", e1.getMessage());
		assertNull(e1.getCause());

		String str1 = e1.getLogMessage();
		assertTrue(str1.contains("[msg erreur message]"));
	}

	/**
	 * Test the MaxNumberCachedJobsReachException
	 */
	@Test
	public void testMaxNumberCachedJobsReachException() {
		MaxNumberCachedJobsReachException e1 = new MaxNumberCachedJobsReachException("task-table-1", "erreur message");

		assertEquals(ErrorCode.MAX_NUMBER_CACHED_JOB_REACH, e1.getCode());
		assertEquals("task-table-1", e1.getTaskTable());
		assertEquals("erreur message", e1.getMessage());
		assertNull(e1.getCause());

		String str1 = e1.getLogMessage();
		assertTrue(str1.contains("[taskTable task-table-1]"));
		assertTrue(str1.contains("[msg erreur message]"));
	}

	/**
	 * Test the KafkaSendException
	 */
	@Test
	public void testKafkaSendException() {
		KafkaSendException e1 = new KafkaSendException("topic-kafka", "product-name", "error message",
				new Throwable("throwable message"));

		assertEquals("topic-kafka", e1.getTopic());
		assertEquals("product-name", e1.getProductName());
		assertEquals(ErrorCode.KAFKA_SEND_ERROR, e1.getCode());
		assertEquals("error message", e1.getMessage());
		assertEquals("throwable message", e1.getCause().getMessage());

		String str1 = e1.getLogMessage();
		assertTrue(str1.contains("[topic topic-kafka]"));
		assertTrue(str1.contains("[productName product-name]"));
		assertTrue(str1.contains("[msg error message]"));
	}

	/**
	 * Test the InvalidFormatProduct
	 */
	@Test
	public void testInvalidFormatProduct() {
		InvalidFormatProduct e1 = new InvalidFormatProduct("erreur message");

		assertEquals(ErrorCode.INVALID_PRODUCT_FORMAT, e1.getCode());
		assertEquals("erreur message", e1.getMessage());
		assertNull(e1.getCause());

		String str1 = e1.getLogMessage();
		assertTrue(str1.contains("[msg erreur message]"));
	}

	/**
	 * Test the InputsMissingException
	 */
	@Test
	public void testInputsMissingException() {
		Map<String, String> data = new HashMap<>();
		data.put("key1", "value1");
		data.put("key3", "");
		InputsMissingException e1 = new InputsMissingException(data);

		assertTrue(e1.getMissingMetadata().size() == 2);
		assertEquals(ErrorCode.MISSING_INPUT, e1.getCode());
		assertEquals("Missing inputs", e1.getMessage());
		assertNull(e1.getCause());

		String str1 = e1.getLogMessage();
		assertTrue(str1.contains("[input key1] [reason value1]"));
		assertTrue(str1.contains("[input key3]"));
		assertFalse(str1.contains("[input key3] [reason"));
		assertTrue(str1.contains("[msg Missing inputs]"));
	}

	/**
	 * Test the InternalErrorException
	 */
	@Test
	public void testInternalErrorException() {
		InternalErrorException e1 = new InternalErrorException("erreur message");

		assertEquals(ErrorCode.INTERNAL_ERROR, e1.getCode());
		assertEquals("erreur message", e1.getMessage());
		assertNull(e1.getCause());

		String str1 = e1.getLogMessage();
		assertTrue(str1.contains("[msg erreur message]"));

		InternalErrorException e2 = new InternalErrorException("error message", new Throwable("tutu"));

		assertEquals(ErrorCode.INTERNAL_ERROR, e2.getCode());
		assertEquals("error message", e2.getMessage());
		assertEquals("tutu", e2.getCause().getMessage());

		String str2 = e2.getLogMessage();
		assertTrue(str2.contains("[msg error message]"));
	}
}
