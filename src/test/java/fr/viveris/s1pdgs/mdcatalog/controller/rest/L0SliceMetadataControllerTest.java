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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.viveris.s1pdgs.mdcatalog.controllers.rest.L0SliceMetadataController;
import fr.viveris.s1pdgs.mdcatalog.model.metadata.L0SliceMetadata;
import fr.viveris.s1pdgs.mdcatalog.services.es.EsServices;
import fr.viveris.s1pdgs.test.RestControllerTest;

public class L0SliceMetadataControllerTest extends RestControllerTest {

	@Mock
	private EsServices esServices;

	private L0SliceMetadataController controller;

	@Before
	public void init() throws IOException {
		MockitoAnnotations.initMocks(this);

		this.controller = new L0SliceMetadataController(esServices);
		this.initMockMvc(this.controller);
	}

	private void mockGetL0Slice(L0SliceMetadata response) throws Exception {
		doReturn(response).when(esServices).getL0Slice(Mockito.any(String.class), Mockito.any(String.class));
	}

	@Test
	public void testGetL0SliceMetadata() throws Exception {
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

		MvcResult result = request(get("/l0Slice/type/name")).andExpect(MockMvcResultMatchers.status().isOk())
				.andReturn();

	}

}
