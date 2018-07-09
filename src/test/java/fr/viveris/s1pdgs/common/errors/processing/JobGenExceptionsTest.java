package fr.viveris.s1pdgs.common.errors.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fr.viveris.s1pdgs.common.errors.AbstractCodedException.ErrorCode;

/**
 * Test the Exceptions
 * 
 * @author Viveris Technologies
 */
public class JobGenExceptionsTest {

    /**
     * Test the JobGenerationException
     */
    @Test
    public void testJobGenerationException() {
        JobGenerationException e = new JobGenerationException("task-table",
                ErrorCode.JOB_GENERATOR_INIT_FAILED, "errer message");

        assertEquals("task-table", e.getTaskTable());
        assertEquals(ErrorCode.JOB_GENERATOR_INIT_FAILED, e.getCode());
        assertEquals("errer message", e.getMessage());

        String str = e.getLogMessage();
        assertTrue(str.contains("[taskTable task-table]"));
        assertTrue(str.contains("[msg errer message]"));

        JobGenerationException e1 = new JobGenerationException("tasktable",
                ErrorCode.INTERNAL_ERROR, "error message",
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
        JobGenerationException e1 =
                new JobGenBuildTaskTableException("tasktable", "error message",
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
     * Test the MissingRoutingEntryException
     */
    @Test
    public void testMissingRoutingEntryException() {
        JobGenMissingRoutingEntryException e1 =
                new JobGenMissingRoutingEntryException("erreur message");

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
        JobGenMaxNumberTaskTablesReachException e1 =
                new JobGenMaxNumberTaskTablesReachException("erreur message");

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
        JobGenMaxNumberCachedSessionsReachException e1 =
                new JobGenMaxNumberCachedSessionsReachException(
                        "erreur message");

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
        JobGenMaxNumberCachedJobsReachException e1 =
                new JobGenMaxNumberCachedJobsReachException("task-table-1",
                        "erreur message");

        assertEquals(ErrorCode.MAX_NUMBER_CACHED_JOB_REACH, e1.getCode());
        assertEquals("task-table-1", e1.getTaskTable());
        assertEquals("erreur message", e1.getMessage());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[taskTable task-table-1]"));
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
        JobGenInputsMissingException e1 =
                new JobGenInputsMissingException(data);

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
     * Test the InputsMissingException
     */
    @Test
    public void testInputsMissingExceptionNull() {
        JobGenInputsMissingException e1 =
                new JobGenInputsMissingException(null);

        assertTrue(e1.getMissingMetadata().size() == 0);
        assertEquals(ErrorCode.MISSING_INPUT, e1.getCode());
        assertEquals("Missing inputs", e1.getMessage());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertFalse(str1.contains("[input "));
        assertTrue(str1.contains("[msg Missing inputs]"));
    }
}
