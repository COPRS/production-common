package esa.s1pdgs.cpoc.appcatalog.server.job.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AbstractAppDataException.ErrorCode;

public class AppCatalogJobGenerationTerminatedExceptionTest {

    @Test
    public void testConstructors() {
        AppCatalogJobGenerationTerminatedException obj =
                new AppCatalogJobGenerationTerminatedException("product-name",
                        Arrays.asList("msg1","msg2","msg3"));
        assertEquals(ErrorCode.JOB_GENERATION_TERMINATED, obj.getCode());
        assertEquals("product-name", obj.getProductName());
        assertEquals(Arrays.asList("msg1","msg2","msg3"), obj.getMqiMessages());
    }

    @Test
    public void testLogMessage() {
        AppCatalogJobGenerationTerminatedException obj =
                new AppCatalogJobGenerationTerminatedException("state-error",
                        Arrays.asList("msg1","msg2","msg3"));
        String str = obj.getLogMessage();
        assertTrue(str.contains("[productName state-error]"));
        assertTrue(str.contains("[mqiMessages " + Arrays.asList("msg1","msg2","msg3").toString() + "]"));
    }

}
