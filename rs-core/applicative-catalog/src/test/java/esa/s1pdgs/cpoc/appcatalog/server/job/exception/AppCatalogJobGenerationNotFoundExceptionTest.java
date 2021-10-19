package esa.s1pdgs.cpoc.appcatalog.server.job.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AbstractAppDataException.ErrorCode;

public class AppCatalogJobGenerationNotFoundExceptionTest {

    @Test
    public void testConstructors() {
        AppCatalogJobGenerationNotFoundException obj =
                new AppCatalogJobGenerationNotFoundException(1254L, "task-table");
        assertEquals(ErrorCode.JOB_GENERATION_NOT_FOUND, obj.getCode());
        assertEquals(1254L, obj.getJobId());
        assertEquals("task-table", obj.getTaskTable());
    }

    @Test
    public void testLogMessage() {
        AppCatalogJobGenerationNotFoundException obj =
                new AppCatalogJobGenerationNotFoundException(124L, "task-table");
        String str = obj.getLogMessage();
        assertTrue(str.contains("[jobId 124]"));
        assertTrue(str.contains("[taskTable task-table]"));
    }

}
