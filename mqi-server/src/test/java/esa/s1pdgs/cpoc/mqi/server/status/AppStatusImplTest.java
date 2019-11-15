package esa.s1pdgs.cpoc.mqi.server.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.AppState;

public class AppStatusImplTest {
    
    /**
     * Application status for test
     */
    private AppStatusImpl appStatus = new AppStatusImpl(3);
    
    /**
     * Check constructor
     */
    @Test
    public void checkConstructor() {
        assertEquals(false, appStatus.isShallBeStopped());
        assertEquals(AppState.WAITING, appStatus.getStatus().getState());
        assertFalse(appStatus.getStatus().isError());
        assertFalse(appStatus.getStatus().isFatalError());
        assertEquals(0, appStatus.getStatus().getErrorCounterNextMessage());
        assertTrue(System.currentTimeMillis() >= appStatus.getStatus().getDateLastChangeMs());
        assertTrue(appStatus.getStatus().getDateLastChangeMs() > 0);
        
        appStatus.setShallBeStopped(true);
        assertEquals(true, appStatus.isShallBeStopped());
    }

    /**
     * Test set stopping
     */
    @Test
    public void testResetError() {
        long timeBefore = appStatus.getStatus().getDateLastChangeMs();
        
        appStatus.setError("MQI");
        assertTrue(appStatus.getStatus().isError());
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setWaiting();
        assertFalse(appStatus.getStatus().isError());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(0, appStatus.getStatus().getErrorCounterNextMessage());
        
        appStatus = new AppStatusImpl(3);
        appStatus.setError("MQI");
        appStatus.setError("MQI");
        appStatus.setError("MQI");
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        assertTrue(appStatus.getStatus().isFatalError());
        appStatus.setWaiting();
        assertTrue(appStatus.getStatus().isFatalError());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(3, appStatus.getStatus().getErrorCounterNextMessage());

        appStatus.setStopping();
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setWaiting();
        assertTrue(appStatus.getStatus().isStopping());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(0, appStatus.getStatus().getErrorCounterNextMessage());
    }

    /**
     * Test set stopping
     */
    @Test
    public void testStopping() {
        long timeBefore = appStatus.getStatus().getDateLastChangeMs();
        
        appStatus.setWaiting();
        assertFalse(appStatus.isShallBeStopped());
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setStopping();
        assertTrue(appStatus.isShallBeStopped());
        assertTrue(appStatus.getStatus().isStopping());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(0, appStatus.getStatus().getErrorCounterNextMessage());
        
        appStatus = new AppStatusImpl(3);
        appStatus.setError("MQI");
        assertEquals(1, appStatus.getStatus().getErrorCounterNextMessage());
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setStopping();
        assertTrue(appStatus.isShallBeStopped());
        assertTrue(appStatus.getStatus().isStopping());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(0, appStatus.getStatus().getErrorCounterNextMessage());
        
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setStopping();
        assertTrue(appStatus.getStatus().isStopping());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(0, appStatus.getStatus().getErrorCounterNextMessage());
    }

    /**
     * Test set error
     */
    @Test
    public void testError() {
        long timeBefore = appStatus.getStatus().getDateLastChangeMs();
        
        appStatus.setWaiting();
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setError("MQI");
        assertTrue(appStatus.getStatus().isError());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(1, appStatus.getStatus().getErrorCounterNextMessage());
        
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setError("MQI");
        assertTrue(appStatus.getStatus().isError());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(2, appStatus.getStatus().getErrorCounterNextMessage());
        
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setError("MQI");
        assertFalse(appStatus.getStatus().isError());
        assertTrue(appStatus.getStatus().isFatalError());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(3, appStatus.getStatus().getErrorCounterNextMessage());
        
        appStatus.setStopping();
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setError("MQI");
        assertFalse(appStatus.getStatus().isError());
        assertEquals(timeBefore, appStatus.getStatus().getDateLastChangeMs());
    }
}
