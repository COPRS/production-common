package fr.viveris.s1pdgs.common.errors.obs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.common.errors.AbstractCodedException.ErrorCode;

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
