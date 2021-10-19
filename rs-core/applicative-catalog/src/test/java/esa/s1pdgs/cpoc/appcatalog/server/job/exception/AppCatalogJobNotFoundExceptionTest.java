package esa.s1pdgs.cpoc.appcatalog.server.job.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AbstractAppDataException.ErrorCode;

public class AppCatalogJobNotFoundExceptionTest {

    @Test
    public void testConstructors() {
        AppCatalogJobNotFoundException obj =
                new AppCatalogJobNotFoundException(1254L);
        assertEquals(ErrorCode.JOB_NOT_FOUND, obj.getCode());
        assertEquals(1254L, obj.getJobId());
    }

    @Test
    public void testLogMessage() {
        AppCatalogJobNotFoundException obj =
                new AppCatalogJobNotFoundException(124L);
        String str = obj.getLogMessage();
        assertTrue(str.contains("[jobId 124]"));
    }
}
