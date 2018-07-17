package fr.viveris.s1pdgs.level0.wrapper.status;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.viveris.s1pdgs.level0.wrapper.status.AppState;

/**
 * Test the enumeration ProductFamily
 * 
 * @author Viveris Technologies
 */
public class AppStateTest {

    /**
     * Test default method of enumeration
     */
    @Test
    public void testValueOf() {
        assertEquals(5, AppState.values().length);
        assertEquals(AppState.WAITING, AppState.valueOf("WAITING"));
        assertEquals(AppState.PROCESSING, AppState.valueOf("PROCESSING"));
        assertEquals(AppState.STOPPING, AppState.valueOf("STOPPING"));
        assertEquals(AppState.FATALERROR, AppState.valueOf("FATALERROR"));
        assertEquals(AppState.ERROR, AppState.valueOf("ERROR"));
    }

}
