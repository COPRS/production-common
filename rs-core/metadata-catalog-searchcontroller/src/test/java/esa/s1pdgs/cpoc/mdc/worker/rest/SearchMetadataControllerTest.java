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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataNotPresentException;
import esa.s1pdgs.cpoc.mdc.worker.service.EsServices;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;

public class SearchMetadataControllerTest extends RestControllerTest {

	@Mock
	private EsServices esServices;

	private SearchMetadataController controller;

	@Before
	public void init() throws IOException {
		MockitoAnnotations.initMocks(this);

		this.controller = new SearchMetadataController(esServices);
		this.initMockMvc(this.controller);
	}

	private void mockSearchMetadataLastValCover(SearchMetadata response) throws Exception {
		doReturn(response).when(esServices).lastValCover(Mockito.any(String.class), Mockito.any(ProductFamily.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString());
	}
	
	private void mockSearchMetadataValIntersect(List<SearchMetadata> response) throws Exception {
        doReturn(response).when(esServices).valIntersect(Mockito.any(String.class),Mockito.any(String.class),Mockito.any(String.class),Mockito.any(ProductFamily.class),Mockito.any(String.class),Mockito.any(String.class));
    }
	
	private void mockSearchMetadataFullCoverage(List<SearchMetadata> response) throws Exception {
		doReturn(response).when(esServices).fullCoverage(Mockito.any(String.class),Mockito.any(String.class),Mockito.any(String.class),Mockito.any(ProductFamily.class),Mockito.any(String.class),Mockito.any(String.class));
	}
	
	private void mockSearchMetadataLastValIntersect(SearchMetadata response) throws Exception {
        doReturn(response).when(esServices).lastValIntersect(Mockito.any(String.class),Mockito.any(String.class),Mockito.any(String.class),Mockito.any(ProductFamily.class),Mockito.any(String.class));
    }

	private void mockSearchMetadataNotPresentException() throws Exception {
		doThrow(new MetadataNotPresentException("name")).when(esServices).lastValCover(Mockito.any(String.class), Mockito.any(ProductFamily.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.anyInt(), Mockito.anyString(), Mockito.isNull());
	}
	
	private void mockSearchMetadataException() throws Exception {
		doThrow(new Exception()).when(esServices).lastValCover(Mockito.any(String.class), Mockito.any(ProductFamily.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString());
	}
	
	
	@Test
	public void testSearchMetadataLastValCover() throws Exception {
		List<SearchMetadata> expectedResult = new ArrayList<>();
		expectedResult.add(new SearchMetadata("name", "type", "kobs", "startDate", "stopDate",
                "mission", "satellite", "station"));
		SearchMetadata response = new SearchMetadata();
		response.setProductName("name");
		response.setProductType("type");
		response.setKeyObjectStorage("kobs");
		response.setValidityStart("startDate");
		response.setValidityStop("stopDate");
		this.mockSearchMetadataLastValCover(response);
		MvcResult result = request(get("/metadata/L0_SLICE/search?productType=type&mode=LatestValCover&processMode=NRT&satellite=satellite&t0=2017-12-08T12:45:23.000000Z&t1=2017-12-08T13:02:19.000000Z")).andExpect(MockMvcResultMatchers.status().isOk())
				.andReturn();
		assertEquals("Result is not returning the HTTP OK Status code", 200, result.getResponse().getStatus());
	}
	
	@Test
    public void testSearchMetadataValIntersect() throws Exception {
        List<SearchMetadata> expectedResult = new ArrayList<>();
        expectedResult.add(new SearchMetadata("name", "type", "kobs", "startDate", "stopDate",
                "mission", "satellite", "station"));
        SearchMetadata r = new SearchMetadata();
        r.setProductName("name");
        r.setProductType("type");
        r.setKeyObjectStorage("kobs");
        r.setValidityStart("startDate");
        r.setValidityStop("stopDate");
        List<SearchMetadata> response = new ArrayList<>();
        response.add(r);
        this.mockSearchMetadataValIntersect(response);
        MvcResult result = request(get("/metadata/L0_SEGMENT/search?mode=ValIntersect&t0=2017-12-08T12:45:23.132456Z&t1=2017-12-08T13:02:19.123456Z&productType=type&processMode=pMode&satellite=B")).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        assertEquals("Result is not returning the HTTP OK Status code", 200, result.getResponse().getStatus());
    }
	
	@Test
    public void testSearchMetadataFullCoverage() throws Exception {
        List<SearchMetadata> expectedResult = new ArrayList<>();
        expectedResult.add(new SearchMetadata("name", "type", "kobs", "startDate", "stopDate",
                "mission", "satellite", "station"));
        SearchMetadata r = new SearchMetadata();
        r.setProductName("name");
        r.setProductType("type");
        r.setKeyObjectStorage("kobs");
        r.setValidityStart("startDate");
        r.setValidityStop("stopDate");
        List<SearchMetadata> response = new ArrayList<>();
        response.add(r);
        this.mockSearchMetadataFullCoverage(response);
        MvcResult result = request(get("/metadata/L0_SEGMENT/search?mode=FullCoverage&t0=2017-12-08T12:45:23.132456Z&t1=2017-12-08T13:02:19.123456Z&productType=type&processMode=pMode&satellite=B")).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        assertEquals("Result is not returning the HTTP OK Status code", 200, result.getResponse().getStatus());
    }
	
	@Test
    public void testSearchMetadataLastValIntersect() throws Exception {
        List<SearchMetadata> expectedResult = new ArrayList<>();
        expectedResult.add(new SearchMetadata("name", "type", "kobs", "startDate", "stopDate",
                "mission", "satellite", "station"));
        SearchMetadata response = new SearchMetadata();
        response.setProductName("name");
        response.setProductType("type");
        response.setKeyObjectStorage("kobs");
        response.setValidityStart("startDate");
        response.setValidityStop("stopDate");
        this.mockSearchMetadataLastValIntersect(response);
        MvcResult result = request(get("/metadata/L0_SEGMENT/search?mode=LatestValIntersect&t0=2017-12-08T12:45:23.132456Z&t1=2017-12-08T13:02:19.123456Z&productType=type&processMode=pMode&satellite=B")).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        assertEquals("Result is not returning the HTTP OK Status code", 200, result.getResponse().getStatus());
    }
	
	@Test
	public void testSearchMetadataIsNULL() throws Exception {
		this.mockSearchMetadataLastValCover(null);
		MvcResult result = request(get("/metadata/L0_SLICE/search?productType=type&mode=LatestValCover&processMode=NRT&satellite=satellite&t0=2017-12-08T12:45:23.000000Z&t1=2017-12-08T13:02:19.000000Z")).andExpect(MockMvcResultMatchers.status().isOk())
				.andReturn();
		assertEquals("Result is not returning the HTTP OK Status code", 200, result.getResponse().getStatus());
		assertEquals("Result is different from expected result", 0, result.getResponse().getContentLength());
	}

	@Test
	public void testSearchMetadataIsNotPresentException() throws Exception {
		this.mockSearchMetadataNotPresentException();
		MvcResult result = request(get("/metadata/L0_SLICE/search?productType=type&mode=LatestValCover&processMode=NRT&satellite=satellite&t0=2017-12-08T12:45:23.000000Z&t1=2017-12-08T13:02:19.000000Z")).andExpect(MockMvcResultMatchers.status().is4xxClientError())
				.andReturn();
		assertEquals("Result is not returning the HTTP NOT FOUND Status code", 400, result.getResponse().getStatus());
	}
	
	@Test
	public void testSearchMetadataBadMode() throws Exception {
		MvcResult result = request(get("/metadata/L0_SLICE/search?productType=type&mode=BADMODE&processMode=NRT&satellite=satellite&t0=startDate&t1=stopDate")).andExpect(MockMvcResultMatchers.status().is4xxClientError())
				.andReturn();
		assertEquals("Result is not returning the HTTP NOT FOUND Status code", 400, result.getResponse().getStatus());
	}
		
	@Test
	public void testSearchMetadataException() throws Exception {
		this.mockSearchMetadataException();
		MvcResult result = request(get("/metadata/L0_SLICE/search?productType=type&mode=LatestValCover&processMode=NRT&satellite=satellite&t0=startDate&t1=stopDate")).andExpect(MockMvcResultMatchers.status().is5xxServerError())
				.andReturn();
		assertEquals("Result is not returning the HTTP NOT FOUND Status code", 500, result.getResponse().getStatus());
	}
	
}
