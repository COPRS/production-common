package esa.s1pdgs.cpoc.appcatalog.server.job.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AbstractAppDataException.ErrorCode;

public class AppCatalogJobGenerationInvalidTransitionStateExceptionTest {

    @Test
    public void testConstructors() {
        AppCatalogJobGenerationInvalidTransitionStateException obj =
                new AppCatalogJobGenerationInvalidTransitionStateException("state-error",
                        "type-error");
        assertEquals(ErrorCode.JOB_GENERATION_INVALID_STATE_TRANSITION, obj.getCode());
        assertEquals("state-error", obj.getStateBefore());
        assertEquals("type-error", obj.getStateAfter());
    }

    @Test
    public void testLogMessage() {
        AppCatalogJobGenerationInvalidTransitionStateException obj =
                new AppCatalogJobGenerationInvalidTransitionStateException("state-error",
                        "type-error");
        String str = obj.getLogMessage();
        assertTrue(str.contains("[stateBefore state-error]"));
        assertTrue(str.contains("[stateAfter type-error]"));
    }

}
