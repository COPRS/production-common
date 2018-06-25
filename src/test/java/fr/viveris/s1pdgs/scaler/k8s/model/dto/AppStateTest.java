package fr.viveris.s1pdgs.scaler.k8s.model.dto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
