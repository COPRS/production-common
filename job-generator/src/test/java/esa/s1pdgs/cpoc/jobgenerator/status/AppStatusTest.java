package esa.s1pdgs.cpoc.jobgenerator.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.AppState;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.jobgenerator.status.AppStatus;
import esa.s1pdgs.cpoc.mqi.client.StatusService;

public class AppStatusTest {
    
    /**
     * Application status for test
     */
    private AppStatus appStatus;

    /**
     * MQI service for stopping the MQI
     */
    @Mock
    private StatusService mqiStatusService;
    
    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);
        
        doNothing().when(mqiStatusService).stop();
        
        appStatus = new AppStatus(3, 30, mqiStatusService);
    }
    
    /**
     * Check constructor
     */
    @Test
    public void checkConstructor() {
        assertEquals(false, appStatus.isShallBeStopped());
        assertEquals(AppState.WAITING, appStatus.getStatus().getState());
        assertTrue(appStatus.getStatus().isWaiting());
        assertEquals(0, appStatus.getStatus().getErrorCounterProcessing());
        assertEquals(0, appStatus.getStatus().getErrorCounterNextMessage());
        assertTrue(System.currentTimeMillis() >= appStatus.getStatus().getDateLastChangeMs());
        assertTrue(appStatus.getStatus().getDateLastChangeMs() > 0);
        assertEquals(0, appStatus.getProcessingMsgId());
        
        appStatus.setShallBeStopped(true);
        assertEquals(true, appStatus.isShallBeStopped());
    }

    /**
     * Test set waiting
     */
    @Test
    public void testWaiting() {
        long timeBefore = appStatus.getStatus().getDateLastChangeMs();
        
        appStatus.setProcessing(123);
        assertEquals(123, appStatus.getProcessingMsgId());
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setWaiting();
        assertEquals(0, appStatus.getProcessingMsgId());
        assertTrue(appStatus.getStatus().isWaiting());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setWaiting();
        assertTrue(appStatus.getStatus().isWaiting());
        assertEquals(timeBefore, appStatus.getStatus().getDateLastChangeMs());
        
        appStatus.setError("PROCESSING");
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setWaiting();
        assertTrue(appStatus.getStatus().isWaiting());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        
        appStatus.getStatus().setFatalError();
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setWaiting();
        assertFalse(appStatus.getStatus().isWaiting());
        assertEquals(timeBefore, appStatus.getStatus().getDateLastChangeMs());
        
        appStatus.setStopping();
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setWaiting();
        assertFalse(appStatus.getStatus().isWaiting());
        assertEquals(timeBefore, appStatus.getStatus().getDateLastChangeMs());
    }

    /**
     * Test set processing
     */
    @Test
    public void testProcessing() {
        long timeBefore = appStatus.getStatus().getDateLastChangeMs();
        
        appStatus.getStatus().setWaiting();
        assertEquals(0, appStatus.getProcessingMsgId());
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setProcessing(123);
        assertEquals(123, appStatus.getProcessingMsgId());
        assertTrue(appStatus.getStatus().isProcessing());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setProcessing(123);
        assertEquals(123, appStatus.getProcessingMsgId());
        assertTrue(appStatus.getStatus().isProcessing());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        
        appStatus.setError("PROCESSING");
        assertEquals(123, appStatus.getProcessingMsgId());
        assertEquals(1, appStatus.getStatus().getErrorCounterProcessing());
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setProcessing(123);
        assertEquals(123, appStatus.getProcessingMsgId());
        assertTrue(appStatus.getStatus().isProcessing());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(0, appStatus.getStatus().getErrorCounterProcessing());
        
        appStatus.getStatus().setFatalError();
        assertEquals(123, appStatus.getProcessingMsgId());
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setProcessing(123);
        assertEquals(123, appStatus.getProcessingMsgId());
        assertFalse(appStatus.getStatus().isProcessing());
        assertEquals(timeBefore, appStatus.getStatus().getDateLastChangeMs());
        
        appStatus.setStopping();
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setProcessing(123);
        assertEquals(123, appStatus.getProcessingMsgId());
        assertFalse(appStatus.getStatus().isProcessing());
        assertEquals(timeBefore, appStatus.getStatus().getDateLastChangeMs());
    }

    /**
     * Test set stopping
     */
    @Test
    public void testStopping() {
        long timeBefore = appStatus.getStatus().getDateLastChangeMs();
        
        appStatus.setProcessing(123);
        assertFalse(appStatus.isShallBeStopped());
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setStopping();
        assertFalse(appStatus.isShallBeStopped());
        assertTrue(appStatus.getStatus().isStopping());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(0, appStatus.getStatus().getErrorCounterProcessing());
        
        appStatus = new AppStatus(3, 30, mqiStatusService);
        appStatus.setError("PROCESSING");
        assertEquals(1, appStatus.getStatus().getErrorCounterProcessing());
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setStopping();
        assertTrue(appStatus.isShallBeStopped());
        assertTrue(appStatus.getStatus().isStopping());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(0, appStatus.getStatus().getErrorCounterProcessing());
        
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setStopping();
        assertTrue(appStatus.getStatus().isStopping());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(0, appStatus.getStatus().getErrorCounterProcessing());
    }

    /**
     * Test set error
     */
    @Test
    public void testError() {
        long timeBefore = appStatus.getStatus().getDateLastChangeMs();
        
        appStatus.getStatus().setWaiting();
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setError("PROCESSING");
        assertTrue(appStatus.getStatus().isError());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(1, appStatus.getStatus().getErrorCounterProcessing());
        
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setError("PROCESSING");
        assertTrue(appStatus.getStatus().isError());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(2, appStatus.getStatus().getErrorCounterProcessing());
        
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setError("PROCESSING");
        assertFalse(appStatus.getStatus().isError());
        assertTrue(appStatus.getStatus().isFatalError());
        assertTrue(timeBefore <= appStatus.getStatus().getDateLastChangeMs());
        assertEquals(3, appStatus.getStatus().getErrorCounterProcessing());
        
        appStatus.setStopping();
        timeBefore = appStatus.getStatus().getDateLastChangeMs();
        appStatus.setError("PROCESSING");
        assertFalse(appStatus.getStatus().isError());
        assertEquals(timeBefore, appStatus.getStatus().getDateLastChangeMs());
    }
}
