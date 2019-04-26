package esa.s1pdgs.cpoc.appcatalog.server.job.db;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Available states of a job generation
 */
public class AppDataJobGenerationStateTest {

    /**
     * Test values and valueOf
     */
    @Test
    public void basic() {
        assertEquals(4, AppDataJobGenerationState.values().length);

        assertEquals(AppDataJobGenerationState.INITIAL,
                AppDataJobGenerationState.valueOf("INITIAL"));
        assertEquals(AppDataJobGenerationState.PRIMARY_CHECK,
                AppDataJobGenerationState.valueOf("PRIMARY_CHECK"));
        assertEquals(AppDataJobGenerationState.READY,
                AppDataJobGenerationState.valueOf("READY"));
        assertEquals(AppDataJobGenerationState.SENT,
                AppDataJobGenerationState.valueOf("SENT"));
    }

}
