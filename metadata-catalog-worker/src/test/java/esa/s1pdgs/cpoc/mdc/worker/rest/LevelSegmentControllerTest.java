/**
 * 
 */
package esa.s1pdgs.cpoc.mdc.worker.rest;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataNotPresentException;
import esa.s1pdgs.cpoc.mdc.worker.es.EsServices;
import esa.s1pdgs.cpoc.mdc.worker.rest.LevelSegmentController;
import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;

/**
 *
 *
 * @author Viveris Technologies
 */
public class LevelSegmentControllerTest extends RestControllerTest {

    
    @Mock
    private EsServices esServices;

    private LevelSegmentController controller;

    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);

        this.controller = new LevelSegmentController(esServices);
        this.initMockMvc(this.controller);
    }
    
    @Test
    public void nominalCaseTest() throws Exception {
        
        LevelSegmentMetadata result = new LevelSegmentMetadata();
        result.setProductName("name");
        result.setProductType("L0_SEGMENT");
        result.setKeyObjectStorage("kobs");
        result.setValidityStart("start");
        result.setValidityStop("stop");
        result.setDatatakeId("14256");
        result.setConsolidation("consol");
        result.setPolarisation("pol");
        
        doReturn(result).when(esServices).getLevelSegment(Mockito.any(), Mockito.any());
        
        request(get("/level_segment/L0_SEGMENT/name"))
        .andExpect(MockMvcResultMatchers.status().isOk());
        verify(esServices, times(1)).getLevelSegment(Mockito.eq(ProductFamily.L0_SEGMENT), Mockito.eq("name"));
    }
    
    @Test
    public void metadataNotPresentTest() throws Exception {
        doThrow(new MetadataNotPresentException("name")).when(esServices).getLevelSegment(Mockito.any(), Mockito.any());
        request(get("/level_segment/L0_SEGMENT/name"))
        .andExpect(MockMvcResultMatchers.status().is4xxClientError());
        verify(esServices, times(1)).getLevelSegment(Mockito.eq(ProductFamily.L0_SEGMENT), Mockito.eq("name"));
    }
    
    @Test
    public void abstrcatExceptionTest() throws Exception {
        doThrow(new MetadataMalformedException("name")).when(esServices).getLevelSegment(Mockito.any(), Mockito.any());
        request(get("/level_segment/L0_SEGMENT/name"))
        .andExpect(MockMvcResultMatchers.status().is5xxServerError());
        verify(esServices, times(1)).getLevelSegment(Mockito.eq(ProductFamily.L0_SEGMENT), Mockito.eq("name"));
    }
    
    @Test
    public void exceptionTest() throws Exception {
        doThrow(new Exception("name")).when(esServices).getLevelSegment(Mockito.any(), Mockito.any());
        request(get("/level_segment/L0_SEGMENT/name"))
        .andExpect(MockMvcResultMatchers.status().is5xxServerError());
        verify(esServices, times(1)).getLevelSegment(Mockito.eq(ProductFamily.L0_SEGMENT), Mockito.eq("name"));
    }
}
