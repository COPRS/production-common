package esa.s1pdgs.cpoc.mdcatalog.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import esa.s1pdgs.cpoc.mdcatalog.es.EsServices;
import esa.s1pdgs.cpoc.mdcatalog.es.model.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataNotPresentException;
import esa.s1pdgs.cpoc.mdcatalog.rest.EdrsSessionMetadataController;
import esa.s1pdgs.cpoc.mdcatalog.rest.dto.EdrsSessionMetadataDto;
import esa.s1pdgs.cpoc.test.RestControllerTest;

public class EdrsSessionMetadataControllerTest  extends RestControllerTest  {

	@Mock
	private EsServices esServices;

	private EdrsSessionMetadataController controller;

	@Before
	public void init() throws IOException {
		MockitoAnnotations.initMocks(this);

		this.controller = new EdrsSessionMetadataController(esServices);
		this.initMockMvc(this.controller);
	}
	
	private void mockGetEdrsSession(EdrsSessionMetadata response) throws Exception {
		doReturn(response).when(esServices).getEdrsSession(Mockito.any(String.class), Mockito.any(String.class));
	}

	private void mockGetEdrsSessionMetadataNotPresentException() throws Exception {
		doThrow(new MetadataNotPresentException("name")).when(esServices).getEdrsSession(Mockito.any(String.class), Mockito.any(String.class));
	}
	
	private void mockGetEdrsSessionMetadataMalformedException() throws Exception {
		doThrow(new MetadataMalformedException("url")).when(esServices).getEdrsSession(Mockito.any(String.class), Mockito.any(String.class));
	}
	
	private void mockGetEdrsSessionException() throws Exception {
		doThrow(new Exception()).when(esServices).getEdrsSession(Mockito.any(String.class), Mockito.any(String.class));
	}
	
	@Test
	public void testGetEdrsSessionMetadata() throws Exception {
		EdrsSessionMetadata response = new EdrsSessionMetadata();
		response.setProductName("name");
		response.setProductType("type");
		response.setKeyObjectStorage("kobs");
		response.setValidityStart("startDate");
		response.setValidityStop("stopDate");
		this.mockGetEdrsSession(response);
		EdrsSessionMetadataDto expectedResult = new EdrsSessionMetadataDto("name", "type", "kobs", "startDate", "stopDate");
		MvcResult result = request(get("/edrsSession/type/name")).andExpect(MockMvcResultMatchers.status().isOk())
				.andReturn();
		assertEquals("Result is not returning the HTTP OK Status code", 200, result.getResponse().getStatus());
		assertEquals("Result is different from the expected result", expectedResult.toString().replaceAll(" ", ""), result.getResponse().getContentAsString());
	}
	
	@Test
	public void testGetEdrsSessionMetadataIsNULL() throws Exception {
		this.mockGetEdrsSession(null);
		MvcResult result = request(get("/edrsSession/type/name")).andExpect(MockMvcResultMatchers.status().is4xxClientError())
				.andReturn();
		assertEquals("Result is not returning the HTTP NOT FOUND Status code", 404, result.getResponse().getStatus());
	}

	@Test
	public void testGetEdrsSessionMetadataIsNotPresentException() throws Exception {
		this.mockGetEdrsSessionMetadataNotPresentException();
		MvcResult result = request(get("/edrsSession/type/name")).andExpect(MockMvcResultMatchers.status().is4xxClientError())
				.andReturn();
		assertEquals("Result is not returning the HTTP NOT FOUND Status code", 404, result.getResponse().getStatus());
	}
	
	@Test
	public void testGetEdrsSessionMetadataMalformedException() throws Exception {
		this.mockGetEdrsSessionMetadataMalformedException();
		MvcResult result = request(get("/edrsSession/type/name")).andExpect(MockMvcResultMatchers.status().is5xxServerError())
				.andReturn();
		assertEquals("Result is not returning the HTTP NOT FOUND Status code", 500, result.getResponse().getStatus());
	}
	
	@Test
	public void testGetEdrsSessionException() throws Exception {
		this.mockGetEdrsSessionException();
		MvcResult result = request(get("/edrsSession/type/name")).andExpect(MockMvcResultMatchers.status().is5xxServerError())
				.andReturn();
		assertEquals("Result is not returning the HTTP NOT FOUND Status code", 500, result.getResponse().getStatus());
	}
	
	@Test
	public void testGetEdrsSessionWithoutVariable() throws Exception {
		MvcResult result = request(get("/edrsSession/")).andExpect(MockMvcResultMatchers.status().is4xxClientError())
				.andReturn();
		assertEquals("Result is not returning the HTTP NOT FOUND Status code", 404, result.getResponse().getStatus());
	}
	
	@Test
	public void testGetEdrsSessionWithOneVariable() throws Exception {
		MvcResult result = request(get("/edrsSession/type/")).andExpect(MockMvcResultMatchers.status().is4xxClientError())
				.andReturn();
		assertEquals("Result is not returning the HTTP NOT FOUND Status code", 404, result.getResponse().getStatus());
	}
	
	@Test
	public void testGetEdrsSessionWithOneOtherVariable() throws Exception {
		MvcResult result = request(get("/edrsSession//name")).andExpect(MockMvcResultMatchers.status().is4xxClientError())
				.andReturn();
		assertEquals("Result is not returning the HTTP NOT FOUND Status code", 404, result.getResponse().getStatus());
	}
}
