package esa.s1pdgs.cpoc.appcatalog.common.rest.model.job;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Available states of a job generation
 */
public class AppDataJobGenerationDtoStateTest {

    /**
     * Test values and valueOf
     */
    @Test
    public void basic() {
        assertEquals(1, AppDataJobGenerationDtoState.values().length);

        assertEquals(AppDataJobGenerationDtoState.GENERATING,
                AppDataJobGenerationDtoState.valueOf("GENERATING"));
    }

}
