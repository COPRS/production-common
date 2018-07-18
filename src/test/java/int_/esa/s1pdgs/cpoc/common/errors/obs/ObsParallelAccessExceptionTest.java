package int_.esa.s1pdgs.cpoc.common.errors.obs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import int_.esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import int_.esa.s1pdgs.cpoc.common.errors.obs.ObsParallelAccessException;

/**
 * @author Viveris Technologies
 */
public class ObsParallelAccessExceptionTest {

    /**
     * Test the ObsS3Exception
     */
    @Test
    public void testObsS3Exception() {
        ObsParallelAccessException e1 = new ObsParallelAccessException(
                new Throwable("throwable message"));

        assertEquals(ErrorCode.OBS_PARALLEL_ACCESS, e1.getCode());
        assertEquals("throwable message", e1.getMessage());
        assertEquals("throwable message", e1.getCause().getMessage());

        String str1 = e1.getLogMessage();
        assertTrue(str1.contains("[msg throwable message]"));
    }

}
