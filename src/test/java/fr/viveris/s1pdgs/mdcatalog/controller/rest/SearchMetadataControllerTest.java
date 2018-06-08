package fr.viveris.s1pdgs.mdcatalog.controller.rest;

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

import fr.viveris.s1pdgs.mdcatalog.controllers.rest.SearchMetadataController;
import fr.viveris.s1pdgs.mdcatalog.model.exception.MetadataNotPresentException;
import fr.viveris.s1pdgs.mdcatalog.model.metadata.SearchMetadata;
import fr.viveris.s1pdgs.mdcatalog.services.es.EsServices;
import fr.viveris.s1pdgs.test.RestControllerTest;

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

	private void mockSearchMetadata(SearchMetadata response) throws Exception {
		doReturn(response).when(esServices).lastValCover(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.anyInt());
	}

	private void mockSearchMetadataNotPresentException() throws Exception {
		doThrow(new MetadataNotPresentException("name")).when(esServices).lastValCover(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.anyInt());
	}
	
	private void mockSearchMetadataException() throws Exception {
		doThrow(new Exception()).when(esServices).lastValCover(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.anyInt());
	}
	
	@Test
	public void testSearchMetadataIsNULL() throws Exception {
		this.mockSearchMetadata(null);
		MvcResult result = request(get("/metadata/search?productType=type&mode=LatestValCover&satelliteId=satellitte&t0=startDate&t1=stopDate")).andExpect(MockMvcResultMatchers.status().is4xxClientError())
				.andReturn();
		assertEquals("Result is not returning the HTTP NOT FOUND Status code", 400, result.getResponse().getStatus());
	}

	@Test
	public void testSearchMetadataIsNotPresentException() throws Exception {
		this.mockSearchMetadataNotPresentException();
		MvcResult result = request(get("/metadata/search?productType=type&mode=LatestValCover&satelliteId=satellitte&t0=startDate&t1=stopDate")).andExpect(MockMvcResultMatchers.status().is4xxClientError())
				.andReturn();
		assertEquals("Result is not returning the HTTP NOT FOUND Status code", 400, result.getResponse().getStatus());
	}
	
	@Test
	public void testSearchMetadataBadMode() throws Exception {
		MvcResult result = request(get("/metadata/search?productType=type&mode=BADMODE&satellite=satellitteId&t0=startDate&t1=stopDate")).andExpect(MockMvcResultMatchers.status().is4xxClientError())
				.andReturn();
		assertEquals("Result is not returning the HTTP NOT FOUND Status code", 400, result.getResponse().getStatus());
	}
		
	@Test
	public void testSearchMetadataException() throws Exception {
		this.mockSearchMetadataException();
		MvcResult result = request(get("/metadata/search?productType=type&mode=LatestValCover&satellite=satellitteId&t0=startDate&t1=stopDate")).andExpect(MockMvcResultMatchers.status().is5xxServerError())
				.andReturn();
		assertEquals("Result is not returning the HTTP NOT FOUND Status code", 500, result.getResponse().getStatus());
	}
	
}
