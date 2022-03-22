package esa.s1pdgs.cpoc.appcatalog.server.job.db;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;

/**
 * Available states of a job
 */
public class AppDataJobStateTest {

    /**
     * Test values and valueOf
     */
    @Test
    public void basic() {
        assertEquals(4, AppDataJobState.values().length);
        
        assertEquals(AppDataJobState.DISPATCHING, AppDataJobState.valueOf("DISPATCHING"));
        assertEquals(AppDataJobState.GENERATING, AppDataJobState.valueOf("GENERATING"));
        assertEquals(AppDataJobState.TERMINATED, AppDataJobState.valueOf("TERMINATED"));
        assertEquals(AppDataJobState.WAITING, AppDataJobState.valueOf("WAITING"));
    }
}
