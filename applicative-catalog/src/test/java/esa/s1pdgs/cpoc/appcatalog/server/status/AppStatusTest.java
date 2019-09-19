package esa.s1pdgs.cpoc.appcatalog.server.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.AppState;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

public class AppStatusTest {
    
    /**
     * Application status for test
     */
    private AppStatus appStatus;
    
    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);
              
        appStatus = new AppStatus(3,3);
    }
    
    /**
     * Check constructor
     */
    @Test
    public void checkConstructor() {
        assertEquals(false, appStatus.isShallBeStopped());
        assertEquals(AppState.WAITING, appStatus.getStatus().getState());
        assertTrue(appStatus.getStatus().isWaiting());
        assertEquals(0, appStatus.getStatus().getErrorCounterMqi());
        assertTrue(System.currentTimeMillis() >= appStatus.getStatus().getDateLastChangeMs());
        assertTrue(appStatus.getStatus().getDateLastChangeMs() > 0);
        
        appStatus.setShallBeStopped(true);
        assertEquals(true, appStatus.isShallBeStopped());
    }

    /**
     * Test set waiting
     */
    @Test
    public void testWaiting() {
        long timeBefore = appStatus.getStatus().getDateLastChangeMs();
        
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setWaiting("MQI");
        assertTrue(appStatus.getStatus().isWaiting());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setWaiting("MQI");
        assertTrue(appStatus.getStatus().isWaiting());
        assertEquals(timeBefore, appStatus.getStatus().getDateLastChangeMs());
        
        appStatus.setError("MQI");
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setWaiting("MQI");
        assertTrue(appStatus.getStatus().isWaiting());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        
        appStatus.getStatus().setFatalError();
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setWaiting("MQI");
        assertFalse(appStatus.getStatus().isWaiting());
        assertEquals(timeBefore, appStatus.getStatus().getDateLastChangeMs());
        
        appStatus.setStopping();
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setWaiting("MQI");
        assertFalse(appStatus.getStatus().isWaiting());
        assertEquals(timeBefore, appStatus.getStatus().getDateLastChangeMs());
    }

    /**
     * Test set stopping
     */
    @Test
    public void testStopping() {
        long timeBefore = appStatus.getStatus().getDateLastChangeMs();
        
        appStatus.setWaiting("MQI");
        assertFalse(appStatus.isShallBeStopped());
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setStopping();
        assertTrue(appStatus.isShallBeStopped());
        assertTrue(appStatus.getStatus().isStopping());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(0, appStatus.getStatus().getErrorCounterMqi());
        
        appStatus = new AppStatus(3,3);
        appStatus.setError("MQI");
        assertEquals(1, appStatus.getStatus().getErrorCounterMqi());
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setStopping();
        assertTrue(appStatus.isShallBeStopped());
        assertTrue(appStatus.getStatus().isStopping());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(0, appStatus.getStatus().getErrorCounterMqi());
        
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setStopping();
        assertTrue(appStatus.getStatus().isStopping());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(0, appStatus.getStatus().getErrorCounterMqi());
    }

    /**
     * Test set error
     */
    @Test
    public void testError() {
        long timeBefore = appStatus.getStatus().getDateLastChangeMs();
        
        appStatus.getStatus().setWaiting();
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setError("MQI");
        assertTrue(appStatus.getStatus().isError());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(1, appStatus.getStatus().getErrorCounterMqi());
        
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setError("MQI");
        assertTrue(appStatus.getStatus().isError());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(2, appStatus.getStatus().getErrorCounterMqi());
        
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setError("MQI");
        assertFalse(appStatus.getStatus().isError());
        assertTrue(appStatus.getStatus().isFatalError());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(3, appStatus.getStatus().getErrorCounterMqi());
        
        appStatus.setStopping();
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setError("MQI");
        assertFalse(appStatus.getStatus().isError());
        assertEquals(timeBefore, appStatus.getStatus().getDateLastChangeMs());
    }
}
