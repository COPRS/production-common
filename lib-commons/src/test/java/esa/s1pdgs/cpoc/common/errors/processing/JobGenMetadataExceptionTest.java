package esa.s1pdgs.cpoc.common.errors.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;

/**
 * Test the class JobGenMetadataException
 * 
 * @author Viveris Technologies
 */
public class JobGenMetadataExceptionTest {

    /**
     * Test the exception
     */
    @Test
    public void test() {
        JobGenMetadataException e1 =
                new JobGenMetadataException("error-message");
        assertEquals("error-message", e1.getMessage());
        assertEquals(ErrorCode.JOB_GEN_METADATA_ERROR, e1.getCode());
        assertNull(e1.getCause());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[msg error-message]"));

        JobGenMetadataException e2 = new JobGenMetadataException(
                "error-message", new Exception("cause-message"));
        assertEquals("error-message", e2.getMessage());
        assertEquals(ErrorCode.JOB_GEN_METADATA_ERROR, e2.getCode());
        assertNotNull(e2.getCause());
    }

}
