package esa.s1pdgs.cpoc.mdcatalog.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.AppState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the class StatusPerCategory
 * 
 * @author Viveris Technologies
 */
public class StatusPerCategoryTest {

    /**
     * Test default cosntructor and getters
     */
    @Test
    public void testConstructor() {
        StatusPerCategory dto =
                new StatusPerCategory(ProductCategory.EDRS_SESSIONS);
        assertEquals(AppState.WAITING, dto.getState());
        assertEquals(0, dto.getErrorCounter());
        assertEquals(0, dto.getProcessingMsgId());
        assertEquals(ProductCategory.EDRS_SESSIONS, dto.getCategory());
    }

    /**
     * Test equals and hashcode
     */
    @Test
    public void equalsDto() {
        EqualsVerifier.forClass(StatusPerCategory.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }
    
    /**
     * Test setters from waiting state
     */
    @Test
    public void testTransitionsFromWaitingState() {
        StatusPerCategory dto =
                new StatusPerCategory(ProductCategory.EDRS_SESSIONS);
        
        // Set waiting -> processing
        dto.setProcessing(1245);
        assertTrue(dto.isProcessing());
        assertEquals(AppState.PROCESSING, dto.getState());
        assertEquals(0, dto.getErrorCounter());
        assertEquals(1245, dto.getProcessingMsgId());
        
        // Set processing -> waiting
        dto.setWaiting();
        assertTrue(dto.isWaiting());
        assertEquals(AppState.WAITING, dto.getState());
        assertEquals(0, dto.getErrorCounter());
        assertEquals(0, dto.getProcessingMsgId());
        
        // Set waiting -> error (twice)
        dto.setError(3);
        assertTrue(dto.isError());
        assertEquals(AppState.ERROR, dto.getState());
        assertEquals(1, dto.getErrorCounter());
        assertEquals(0, dto.getProcessingMsgId());
        
        dto.setError(3);
        assertTrue(dto.isError());
        assertEquals(AppState.ERROR, dto.getState());
        assertEquals(2, dto.getErrorCounter());
        assertEquals(0, dto.getProcessingMsgId());
        
        // Set error -> waiting
        dto.setWaiting();
        assertTrue(dto.isWaiting());
        assertEquals(AppState.WAITING, dto.getState());
        assertEquals(2, dto.getErrorCounter());
        assertEquals(0, dto.getProcessingMsgId());
        
        // Set waiting -> fatal error
        dto.setFatalError();
        assertTrue(dto.isFatalError());
        assertEquals(AppState.FATALERROR, dto.getState());
        assertEquals(2, dto.getErrorCounter());
        assertEquals(0, dto.getProcessingMsgId());
        
    }
    
    /**
     * Test setters from fatalaError state
     */
    @Test
    public void testTransitionsFromFatalErrorState() {
        StatusPerCategory dto =
                new StatusPerCategory(ProductCategory.EDRS_SESSIONS);
        dto.setFatalError();
        
        // Set fatalerror -> waiting
        dto.setWaiting();
        assertTrue(dto.isFatalError());
        assertEquals(AppState.FATALERROR, dto.getState());
        assertEquals(0, dto.getErrorCounter());
        assertEquals(0, dto.getProcessingMsgId());
        
        // Set fatalerror -> waiting
        dto.setProcessing(1245);
        assertTrue(dto.isFatalError());
        assertEquals(AppState.FATALERROR, dto.getState());
        assertEquals(0, dto.getErrorCounter());
        assertEquals(0, dto.getProcessingMsgId());
        
        // Set fatalerror -> error (twice)
        dto.setError(3);
        assertTrue(dto.isFatalError());
        assertEquals(AppState.FATALERROR, dto.getState());
        assertEquals(0, dto.getErrorCounter());
        assertEquals(0, dto.getProcessingMsgId());
        
    }
    
    /**
     * Test status is FATAL ERROR when ERROR reaches the maximal number of errors
     */
    @Test
    public void setSetFatalErrorWhennTooErrors() {
        StatusPerCategory dto =
                new StatusPerCategory(ProductCategory.EDRS_SESSIONS);
        
        dto.setError(3);
        assertEquals(AppState.ERROR, dto.getState());
        assertEquals(1, dto.getErrorCounter());
        assertEquals(0, dto.getProcessingMsgId());

        dto.setError(3);
        assertEquals(AppState.ERROR, dto.getState());
        assertEquals(2, dto.getErrorCounter());
        assertEquals(0, dto.getProcessingMsgId());

        dto.setError(3);
        assertEquals(AppState.FATALERROR, dto.getState());
        assertEquals(3, dto.getErrorCounter());
        assertEquals(0, dto.getProcessingMsgId());
    }

}
