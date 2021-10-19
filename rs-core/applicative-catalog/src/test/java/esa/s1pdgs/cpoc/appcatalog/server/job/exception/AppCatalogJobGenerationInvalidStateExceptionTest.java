package esa.s1pdgs.cpoc.appcatalog.server.job.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AbstractAppDataException.ErrorCode;

public class AppCatalogJobGenerationInvalidStateExceptionTest {

    @Test
    public void testConstructors() {
        AppCatalogJobGenerationInvalidStateException obj =
                new AppCatalogJobGenerationInvalidStateException("state-error",
                        "type-error");
        assertEquals(ErrorCode.JOB_GENERATION_INVALID_STATE, obj.getCode());
        assertEquals("state-error", obj.getState());
        assertEquals("type-error", obj.getType());
    }

    @Test
    public void testLogMessage() {
        AppCatalogJobGenerationInvalidStateException obj =
                new AppCatalogJobGenerationInvalidStateException("state-error",
                        "type-error");
        String str = obj.getLogMessage();
        assertTrue(str.contains("[state state-error]"));
        assertTrue(str.contains("[type type-error]"));
    }
}
