package esa.s1pdgs.cpoc.appcatalog.common.rest.model.job;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Available states of a job
 */
public class AppDataJobDtoStateTest {

    /**
     * Test values and valueOf
     */
    @Test
    public void basic() {
        assertEquals(4, AppDataJobDtoState.values().length);
        
        assertEquals(AppDataJobDtoState.DISPATCHING, AppDataJobDtoState.valueOf("DISPATCHING"));
        assertEquals(AppDataJobDtoState.GENERATING, AppDataJobDtoState.valueOf("GENERATING"));
        assertEquals(AppDataJobDtoState.TERMINATED, AppDataJobDtoState.valueOf("TERMINATED"));
        assertEquals(AppDataJobDtoState.WAITING, AppDataJobDtoState.valueOf("WAITING"));
    }
}
