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
        assertEquals(4, AppDataJobGenerationDtoState.values().length);

        assertEquals(AppDataJobGenerationDtoState.INITIAL,
                AppDataJobGenerationDtoState.valueOf("INITIAL"));
        assertEquals(AppDataJobGenerationDtoState.PRIMARY_CHECK,
                AppDataJobGenerationDtoState.valueOf("PRIMARY_CHECK"));
        assertEquals(AppDataJobGenerationDtoState.READY,
                AppDataJobGenerationDtoState.valueOf("READY"));
        assertEquals(AppDataJobGenerationDtoState.SENT,
                AppDataJobGenerationDtoState.valueOf("SENT"));
    }

}
