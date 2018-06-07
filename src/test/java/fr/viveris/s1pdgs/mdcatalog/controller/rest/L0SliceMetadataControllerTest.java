package fr.viveris.s1pdgs.mdcatalog.controller.rest;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import fr.viveris.s1pdgs.mdcatalog.controllers.rest.L0SliceMetadataController;
import fr.viveris.s1pdgs.mdcatalog.model.metadata.L0SliceMetadata;
import fr.viveris.s1pdgs.mdcatalog.services.es.EsServices;

@WebMvcTest(L0SliceMetadataController.class)
public class L0SliceMetadataControllerTest {

	@Mock
	private EsServices esServices;
	
	@Autowired
	private MockMvc mvc;
	
	@Before
	public void init() throws IOException {
		MockitoAnnotations.initMocks(this);		
	}
	
	private void mockGetL0Slice(L0SliceMetadata response) throws Exception {
		doReturn(response).when(esServices).getL0Slice(Mockito.any(String.class), Mockito.any(String.class));
	}
	
	@Test
	public void getL0SliceMetadata() throws Exception {
		L0SliceMetadata response = new L0SliceMetadata();
		response.setProductName("name");
		response.setProductType("type");
		response.setKeyObjectStorage("url");
		response.setValidityStart("validityStartTime");
		response.setValidityStop("validityStopTime");
		response.setInstrumentConfigurationId(0);
		response.setNumberSlice(2);
		response.setDatatakeId("datatakeId");
		
		this.mockGetL0Slice(response);
		
		RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/l0Slice/type/name").accept(MediaType.APPLICATION_JSON);
		MvcResult result = mvc.perform(requestBuilder).andReturn();
		
		System.out.println(result);
		
	}
	
}
