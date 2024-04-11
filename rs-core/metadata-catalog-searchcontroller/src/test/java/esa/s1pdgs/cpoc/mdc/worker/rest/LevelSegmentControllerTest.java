/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.mdc.worker.rest;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.IOException;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataNotPresentException;
import esa.s1pdgs.cpoc.mdc.worker.service.EsServices;
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
        
        final LevelSegmentMetadata result = new LevelSegmentMetadata();
        result.setProductName("name");
        result.setProductType("L0_SEGMENT");
        result.setKeyObjectStorage("kobs");
        result.setValidityStart("start");
        result.setValidityStop("stop");
        result.setDatatakeId("14256");
        result.setConsolidation("consol");
        result.setPolarisation("pol");
        
        doReturn(Collections.singletonList(result)).when(esServices).getLevelSegmentMetadataFor(Mockito.anyString());
        
        request(get("/level_segment/name"))
        .andExpect(MockMvcResultMatchers.status().isOk());
        verify(esServices, times(1)).getLevelSegmentMetadataFor(Mockito.eq("name"));
    }
    
    @Test
    public void metadataNotPresentTest() throws Exception {
        doThrow(new MetadataNotPresentException("name")).when(esServices).getLevelSegmentMetadataFor(Mockito.anyString());
        request(get("/level_segment/name"))
        .andExpect(MockMvcResultMatchers.status().isNoContent());
        verify(esServices, times(1)).getLevelSegmentMetadataFor(Mockito.eq("name"));
    }
    
    @Test
    public void abstrcatExceptionTest() throws Exception {
        doThrow(new MetadataMalformedException("name")).when(esServices).getLevelSegmentMetadataFor(Mockito.anyString());
        request(get("/level_segment/name"))
        .andExpect(MockMvcResultMatchers.status().is5xxServerError());
        verify(esServices, times(1)).getLevelSegmentMetadataFor(Mockito.eq("name"));
    }
    
    @Test
    public void exceptionTest() throws Exception {
        doThrow(new Exception("name")).when(esServices).getLevelSegmentMetadataFor(Mockito.anyString());
        request(get("/level_segment/name"))
        .andExpect(MockMvcResultMatchers.status().is5xxServerError());
        verify(esServices, times(1)).getLevelSegmentMetadataFor(Mockito.eq("name"));
    }
}
