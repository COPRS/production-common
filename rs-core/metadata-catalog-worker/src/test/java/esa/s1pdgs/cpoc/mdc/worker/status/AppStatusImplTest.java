package esa.s1pdgs.cpoc.mdc.worker.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.AppState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.client.StatusService;

public class AppStatusImplTest {
    
    /**
     * Application status for test
     */
    private AppStatusImpl appStatus;

    /**
     * MQI service for stopping the MQI
     */
    @Mock
    private StatusService mqiStatusService;
    
    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);
        
        doNothing().when(mqiStatusService).stop();
        
        appStatus = new AppStatusImpl(3,30, () -> {});
    }
    
    /**
     * Check constructor
     */
    @Test
    public void checkConstructor() {
        assertEquals(AppState.WAITING, appStatus.getStatus().getState());
        assertEquals(3, appStatus.getStatus().getSubStatuses().size());
    }
    
    /**
     * Test setters
     */
    @Test
    public void testSetters() {
        appStatus.setProcessing(ProductCategory.AUXILIARY_FILES, 1245);
        assertTrue(appStatus.getStatus().getSubStatuses().get(ProductCategory.AUXILIARY_FILES).isProcessing());
        assertTrue(appStatus.getStatus().getSubStatuses().get(ProductCategory.EDRS_SESSIONS).isWaiting());
        assertTrue(appStatus.getStatus().getSubStatuses().get(ProductCategory.LEVEL_PRODUCTS).isWaiting());
        assertEquals(1245, appStatus.getStatus().getSubStatuses().get(ProductCategory.AUXILIARY_FILES).getProcessingMsgId());
        assertEquals(1245, appStatus.getProcessingMsgId(ProductCategory.AUXILIARY_FILES));
        assertEquals(0, appStatus.getProcessingMsgId(ProductCategory.EDRS_SESSIONS));
        assertEquals(0, appStatus.getProcessingMsgId(ProductCategory.LEVEL_PRODUCTS));

        appStatus.setWaiting(ProductCategory.AUXILIARY_FILES);
        assertTrue(appStatus.getStatus().getSubStatuses().get(ProductCategory.AUXILIARY_FILES).isWaiting());
        assertTrue(appStatus.getStatus().getSubStatuses().get(ProductCategory.EDRS_SESSIONS).isWaiting());
        assertTrue(appStatus.getStatus().getSubStatuses().get(ProductCategory.LEVEL_PRODUCTS).isWaiting());
        assertEquals(0, appStatus.getStatus().getSubStatuses().get(ProductCategory.AUXILIARY_FILES).getProcessingMsgId());
        assertEquals(0, appStatus.getProcessingMsgId(ProductCategory.AUXILIARY_FILES));

        appStatus.setError(ProductCategory.AUXILIARY_FILES, "PROCESSING");
        assertTrue(appStatus.getStatus().getSubStatuses().get(ProductCategory.AUXILIARY_FILES).isError());
        assertTrue(appStatus.getStatus().getSubStatuses().get(ProductCategory.EDRS_SESSIONS).isWaiting());
        assertTrue(appStatus.getStatus().getSubStatuses().get(ProductCategory.LEVEL_PRODUCTS).isWaiting());
        assertEquals(0, appStatus.getStatus().getSubStatuses().get(ProductCategory.AUXILIARY_FILES).getProcessingMsgId());
        assertEquals(1, appStatus.getStatus().getSubStatuses().get(ProductCategory.AUXILIARY_FILES).getErrorCounterProcessing());
        assertEquals(0, appStatus.getProcessingMsgId(ProductCategory.AUXILIARY_FILES));

        appStatus.setError(ProductCategory.AUXILIARY_FILES, "PROCESSING");
        assertTrue(appStatus.getStatus().getSubStatuses().get(ProductCategory.AUXILIARY_FILES).isError());

        appStatus.setError(ProductCategory.AUXILIARY_FILES, "PROCESSING");
        assertTrue(appStatus.getStatus().getSubStatuses().get(ProductCategory.AUXILIARY_FILES).isFatalError());
        assertTrue(appStatus.getStatus().getSubStatuses().get(ProductCategory.EDRS_SESSIONS).isWaiting());
        assertTrue(appStatus.getStatus().getSubStatuses().get(ProductCategory.LEVEL_PRODUCTS).isWaiting());
        assertEquals(3, appStatus.getStatus().getSubStatuses().get(ProductCategory.AUXILIARY_FILES).getErrorCounterProcessing());
    }

    /**
     * Test isFatalError
     */
    @Test
    public void testIsFatalError() {
        assertFalse(appStatus.getStatus().isFatalError());
        appStatus.setError(ProductCategory.EDRS_SESSIONS, "PROCESSING");
        assertFalse(appStatus.getStatus().isFatalError());
        appStatus.setError(ProductCategory.EDRS_SESSIONS, "PROCESSING");
        assertFalse(appStatus.getStatus().isFatalError());
        appStatus.setError(ProductCategory.EDRS_SESSIONS, "PROCESSING");
        assertTrue(appStatus.getStatus().isFatalError());
    }
    
    /**
     * Test get global state
     */
    @Test
    public void testGetGlobalState() {
        assertEquals(AppState.WAITING, appStatus.getStatus().getState());

        appStatus.setProcessing(ProductCategory.EDRS_SESSIONS, 1245);
        assertEquals(AppState.PROCESSING, appStatus.getStatus().getState());
        
        appStatus.setWaiting(ProductCategory.EDRS_SESSIONS);
        assertEquals(AppState.WAITING, appStatus.getStatus().getState());

        appStatus.setProcessing(ProductCategory.LEVEL_PRODUCTS, 1245);
        assertEquals(AppState.PROCESSING, appStatus.getStatus().getState());
        
        appStatus.setError(ProductCategory.EDRS_SESSIONS, "PROCESSING");
        assertEquals(AppState.ERROR, appStatus.getStatus().getState());
        
        appStatus.setError(ProductCategory.AUXILIARY_FILES, "PROCESSING");
        assertEquals(AppState.ERROR, appStatus.getStatus().getState());
        
        appStatus.setError(ProductCategory.AUXILIARY_FILES, "PROCESSING");
        assertEquals(AppState.ERROR, appStatus.getStatus().getState());
        
        appStatus.setError(ProductCategory.EDRS_SESSIONS, "PROCESSING");
        assertEquals(AppState.ERROR, appStatus.getStatus().getState());
        
        appStatus.setError(ProductCategory.EDRS_SESSIONS, "PROCESSING");
        assertEquals(AppState.FATALERROR, appStatus.getStatus().getState());
    }
}
