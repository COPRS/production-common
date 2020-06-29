package esa.s1pdgs.cpoc.mdc.worker.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataNotPresentException;
import esa.s1pdgs.cpoc.mdc.worker.service.EsServices;
import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;

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
	
	private void mockgetEdrsSessionsFor(final EdrsSessionMetadata response) throws Exception {
		doReturn(response).when(esServices).getEdrsSessionsFor(Mockito.any(String.class));
	}

	private void mockgetEdrsSessionsForMetadataNotPresentException() throws Exception {
		doThrow(new MetadataNotPresentException("name")).when(esServices).getEdrsSessionsFor(Mockito.any(String.class));
	}
	
	private void mockgetEdrsSessionsForMetadataMalformedException() throws Exception {
		doThrow(new MetadataMalformedException("url")).when(esServices).getEdrsSessionsFor(Mockito.any(String.class));
	}
	
	private void mockgetEdrsSessionsForException() throws Exception {
		doThrow(new Exception()).when(esServices).getEdrsSessionsFor(Mockito.any(String.class));
	}
	
	@Test
	public void testgetEdrsSessionsForMetadata() throws Exception {
		final EdrsSessionMetadata response = new EdrsSessionMetadata();
		response.setProductName("name");
		response.setProductType("type");
		response.setKeyObjectStorage("kobs");
		response.setSessionId("session");
		response.setStartTime("start");
		response.setStopTime("stop");
		response.setValidityStart("vstart");
		response.setValidityStop("vstop");
		response.setMissionId("mission");
		response.setSatelliteId("satellite");
		response.setStationCode("station");
		response.setRawNames(Arrays.<String>asList("a","b","c"));
		this.mockgetEdrsSessionsFor(response);
		final EdrsSessionMetadata expectedResult = new EdrsSessionMetadata("name", "type", "kobs", "session", "start", "stop", "vstart", "vstop", "mission", "satellite", "station", Arrays.<String>asList("a","b","c"));
		final MvcResult result = request(get("/edrsSession/sessionId/mySession321")).andExpect(MockMvcResultMatchers.status().isOk())
				.andReturn();
		assertEquals("Result is not returning the HTTP OK Status code", 200, result.getResponse().getStatus());
		assertEquals("Result is different from the expected result", expectedResult.toJsonString().replaceAll(" ", ""), result.getResponse().getContentAsString());
	}
	
	@Test
	public void testgetEdrsSessionsForMetadataIsNULL() throws Exception {
		this.mockgetEdrsSessionsFor(null);
		final MvcResult result = request(get("/edrsSession/sessionId/mySession321")).andExpect(MockMvcResultMatchers.status().is4xxClientError())
				.andReturn();
		assertEquals("Result is not returning the HTTP NOT FOUND Status code", 404, result.getResponse().getStatus());
	}

	@Test
	public void testgetEdrsSessionsForMetadataIsNotPresentException() throws Exception {
		this.mockgetEdrsSessionsForMetadataNotPresentException();
		final MvcResult result = request(get("/edrsSession/sessionId/mySession321")).andExpect(MockMvcResultMatchers.status().is4xxClientError())
				.andReturn();
		assertEquals("Result is not returning the HTTP NOT FOUND Status code", 404, result.getResponse().getStatus());
	}
	
	@Test
	public void testgetEdrsSessionsForMetadataMalformedException() throws Exception {
		this.mockgetEdrsSessionsForMetadataMalformedException();
		final MvcResult result = request(get("/edrsSession/sessionId/mySession321")).andExpect(MockMvcResultMatchers.status().is5xxServerError())
				.andReturn();
		assertEquals("Result is not returning the HTTP NOT FOUND Status code", 500, result.getResponse().getStatus());
	}
	
	@Test
	public void testgetEdrsSessionsForException() throws Exception {
		this.mockgetEdrsSessionsForException();
		final MvcResult result = request(get("/edrsSession/sessionId/mySession321")).andExpect(MockMvcResultMatchers.status().is5xxServerError())
				.andReturn();
		assertEquals("Result is not returning the HTTP NOT FOUND Status code", 500, result.getResponse().getStatus());
	}
	
	@Test
	public void testgetEdrsSessionsForWithoutVariable() throws Exception {
		final MvcResult result = request(get("/edrsSession/")).andExpect(MockMvcResultMatchers.status().is4xxClientError())
				.andReturn();
		assertEquals("Result is not returning the HTTP NOT FOUND Status code", 404, result.getResponse().getStatus());
	}
	
	@Test
	public void testgetEdrsSessionsForWithOneVariable() throws Exception {
		final MvcResult result = request(get("/edrsSession/type/")).andExpect(MockMvcResultMatchers.status().is4xxClientError())
				.andReturn();
		assertEquals("Result is not returning the HTTP NOT FOUND Status code", 404, result.getResponse().getStatus());
	}
	
	@Test
	public void testgetEdrsSessionsForWithOneOtherVariable() throws Exception {
		final MvcResult result = request(get("/edrsSession//name")).andExpect(MockMvcResultMatchers.status().is4xxClientError())
				.andReturn();
		assertEquals("Result is not returning the HTTP NOT FOUND Status code", 404, result.getResponse().getStatus());
	}
}
