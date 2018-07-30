package esa.s1pdgs.cpoc.mdcatalog.status;

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
        
        appStatus = new AppStatus(3);
    }
    
    /**
     * Check constructor
     */
    @Test
    public void checkConstructor() {
        assertEquals(AppState.WAITING, appStatus.getGlobalAppState());
        assertEquals(3, appStatus.getStatus().size());
    }
    
    /**
     * Test setters
     */
    @Test
    public void testSetters() {
        appStatus.setProcessing(ProductCategory.AUXILIARY_FILES, 1245);
        assertTrue(appStatus.getStatus().get(ProductCategory.AUXILIARY_FILES).isProcessing());
        assertTrue(appStatus.getStatus().get(ProductCategory.EDRS_SESSIONS).isWaiting());
        assertTrue(appStatus.getStatus().get(ProductCategory.LEVEL_PRODUCTS).isWaiting());
        assertEquals(1245, appStatus.getStatus().get(ProductCategory.AUXILIARY_FILES).getProcessingMsgId());
        assertEquals(1245, appStatus.getProcessingMsgId(ProductCategory.AUXILIARY_FILES));
        assertEquals(0, appStatus.getProcessingMsgId(ProductCategory.EDRS_SESSIONS));
        assertEquals(0, appStatus.getProcessingMsgId(ProductCategory.LEVEL_PRODUCTS));

        appStatus.setWaiting(ProductCategory.AUXILIARY_FILES);
        assertTrue(appStatus.getStatus().get(ProductCategory.AUXILIARY_FILES).isWaiting());
        assertTrue(appStatus.getStatus().get(ProductCategory.EDRS_SESSIONS).isWaiting());
        assertTrue(appStatus.getStatus().get(ProductCategory.LEVEL_PRODUCTS).isWaiting());
        assertEquals(0, appStatus.getStatus().get(ProductCategory.AUXILIARY_FILES).getProcessingMsgId());
        assertEquals(0, appStatus.getProcessingMsgId(ProductCategory.AUXILIARY_FILES));

        appStatus.setError(ProductCategory.AUXILIARY_FILES);
        assertTrue(appStatus.getStatus().get(ProductCategory.AUXILIARY_FILES).isError());
        assertTrue(appStatus.getStatus().get(ProductCategory.EDRS_SESSIONS).isWaiting());
        assertTrue(appStatus.getStatus().get(ProductCategory.LEVEL_PRODUCTS).isWaiting());
        assertEquals(0, appStatus.getStatus().get(ProductCategory.AUXILIARY_FILES).getProcessingMsgId());
        assertEquals(1, appStatus.getStatus().get(ProductCategory.AUXILIARY_FILES).getErrorCounter());
        assertEquals(0, appStatus.getProcessingMsgId(ProductCategory.AUXILIARY_FILES));

        appStatus.setError(ProductCategory.AUXILIARY_FILES);
        assertTrue(appStatus.getStatus().get(ProductCategory.AUXILIARY_FILES).isError());

        appStatus.setError(ProductCategory.AUXILIARY_FILES);
        assertTrue(appStatus.getStatus().get(ProductCategory.AUXILIARY_FILES).isFatalError());
        assertTrue(appStatus.getStatus().get(ProductCategory.EDRS_SESSIONS).isWaiting());
        assertTrue(appStatus.getStatus().get(ProductCategory.LEVEL_PRODUCTS).isWaiting());
        assertEquals(3, appStatus.getStatus().get(ProductCategory.AUXILIARY_FILES).getErrorCounter());
    }

    /**
     * Test isFatalError
     */
    @Test
    public void testIsFatalError() {
        assertFalse(appStatus.isFatalError());
        appStatus.setError(ProductCategory.EDRS_SESSIONS);
        assertFalse(appStatus.isFatalError());
        appStatus.setError(ProductCategory.EDRS_SESSIONS);
        assertFalse(appStatus.isFatalError());
        appStatus.setError(ProductCategory.EDRS_SESSIONS);
        assertTrue(appStatus.isFatalError());
    }
    
    /**
     * Test get global state
     */
    @Test
    public void testGetGlobalState() {
        assertEquals(AppState.WAITING, appStatus.getGlobalAppState());

        appStatus.setProcessing(ProductCategory.EDRS_SESSIONS, 1245);
        assertEquals(AppState.PROCESSING, appStatus.getGlobalAppState());
        
        appStatus.setWaiting(ProductCategory.EDRS_SESSIONS);
        assertEquals(AppState.WAITING, appStatus.getGlobalAppState());

        appStatus.setProcessing(ProductCategory.LEVEL_PRODUCTS, 1245);
        assertEquals(AppState.PROCESSING, appStatus.getGlobalAppState());
        
        appStatus.setError(ProductCategory.EDRS_SESSIONS);
        assertEquals(AppState.ERROR, appStatus.getGlobalAppState());
        
        appStatus.setError(ProductCategory.AUXILIARY_FILES);
        assertEquals(AppState.ERROR, appStatus.getGlobalAppState());
        
        appStatus.setError(ProductCategory.AUXILIARY_FILES);
        assertEquals(AppState.ERROR, appStatus.getGlobalAppState());
        
        appStatus.setError(ProductCategory.EDRS_SESSIONS);
        assertEquals(AppState.ERROR, appStatus.getGlobalAppState());
        
        appStatus.setError(ProductCategory.EDRS_SESSIONS);
        assertEquals(AppState.FATALERROR, appStatus.getGlobalAppState());
    }
}
