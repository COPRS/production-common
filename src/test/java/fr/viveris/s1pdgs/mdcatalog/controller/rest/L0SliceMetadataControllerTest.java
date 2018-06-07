package fr.viveris.s1pdgs.mdcatalog.controller.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.viveris.s1pdgs.mdcatalog.controllers.rest.L0SliceMetadataController;
import fr.viveris.s1pdgs.mdcatalog.controllers.rest.dto.L0SliceMetadataDto;
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
		//Expected Result
		L0SliceMetadataDto expectedResult = new L0SliceMetadataDto("name", "type", "url", "validityStartTime", "validityStopTime");
		expectedResult.setInstrumentConfigurationId(0);
		expectedResult.setNumberSlice(2);
		expectedResult.setDatatakeId("datatakeId");
		
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
		assertEquals("test ok", 200, result.getResponse().getStatus());
		//L0SliceMetadataDto resultDTO = new L0SliceMetadataDto(result.getResponse().getContentAsByteArray())
		
		//assertEquals("Response are differents", expectedResult.toString(), result.getResponse().getContentAsString());

	}

}
