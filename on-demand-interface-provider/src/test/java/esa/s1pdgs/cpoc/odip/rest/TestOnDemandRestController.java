package esa.s1pdgs.cpoc.odip.rest;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

import esa.s1pdgs.cpoc.appcatalog.common.OnDemandProcessingRequest;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.odip.config.OdipConfigurationProperties;
import esa.s1pdgs.cpoc.odip.kafka.producer.SubmissionClient;
import esa.s1pdgs.cpoc.odip.service.OnDemandService;

public class TestOnDemandRestController extends RestControllerTest {

	@Mock
	private OnDemandService onDemandService;
	@Mock
	private MetadataClient metadataClient;
	@Mock
	private OdipConfigurationProperties properties;
	@Mock
	private SubmissionClient kafkaSubmissionClient;

	private AppStatus status = AppStatus.NULL;

	private OnDemandRestController controller;

	@Before
	public void init() throws IOException {
		MockitoAnnotations.initMocks(this);

		this.controller = new OnDemandRestController(onDemandService);
		this.initMockMvc(this.controller);
	}

	private void mockQueryByFamilyAndProductName(final SearchMetadata response) throws Exception {
		doReturn(response).when(metadataClient).queryByFamilyAndProductName(Mockito.any(String.class),
				Mockito.any(String.class));
	}

	private void mockGetProductionTypeToProductFamily(final Map<String, String> response) {
		doReturn(response).when(properties).getProductionTypeToProductFamily();
	}

	public static String asJsonString(final Object obj) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			final String jsonContent = mapper.writeValueAsString(obj);
			return jsonContent;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public final void test() throws Exception {
		final String jsonContent = asJsonString(new OnDemandProcessingRequest("ProductNameA", false, "ModeA", "L1"));
		final MockHttpServletRequestBuilder requestBuilder = post("/odip/v1/onDemandProcessings/")
				.contentType(MediaType.APPLICATION_JSON).content(jsonContent);

		this.mockGetProductionTypeToProductFamily(new HashMap<>());
		this.mockQueryByFamilyAndProductName(
				new SearchMetadata("ProductNameA", "ProductTypeA", "ProductNameA", "2020-09-15T09:15:00.000000Z",
						"2020-09-15T09:45:00.000000Z", "MissionIdA", "SatelliteIdA", "StationCodeA"));

		request(requestBuilder).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
	}
	
	@Test
	public final void dumpForCluster() throws Exception {
		final String jsonContent = asJsonString(new OnDemandProcessingRequest("S1B_OPER_AUX_RESORB_OPOD_20200911T053929_V20200911T011556_20200911T043326.EOF", false, "NOMINAL", "OBS"));
		System.out.println(jsonContent.replace("\"", "\\\""));
	}

}
