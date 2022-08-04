package esa.s1pdgs.cpoc.reqrepo.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.reqrepo.repo.FailedProcessingRepo;
import esa.s1pdgs.cpoc.reqrepo.service.RequestRepository;

@RunWith(SpringRunner.class)
@WebMvcTest(RequestRepositoryController.class)
public class RequestRepositoryControllerTest {
	
	private static final String API_KEY = "TestApiKey";

	@MockBean
	private FailedProcessingRepo failedProcessingRepo;

	@MockBean
	private RequestRepository requestRepository;
	
	@Autowired
	private MockMvc uut;
	
	@Before
	public void before() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		MockitoAnnotations.initMocks(this);
	}
	
	private FailedProcessing newFailedProcessing() throws Exception
	{
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
		
		final FailedProcessing failedProcessingToReturn = new FailedProcessing();
		failedProcessingToReturn.setId("000000000000000000000001");
		failedProcessingToReturn.setMissionId(MissionId.S1);
		failedProcessingToReturn.setTopic("dummyProcessingType");
		failedProcessingToReturn.setFailureDate(dateFormat.parse("2019-06-18T11:09:03.805Z"));
		failedProcessingToReturn.setFailureMessage("dummyMessage");
		failedProcessingToReturn.setMessage("{\"foo\": \"bar\"}");
		failedProcessingToReturn.setStacktrace("...");
		failedProcessingToReturn.setErrorLevel("ERROR");
		return failedProcessingToReturn;
	}

	@Test
	public void test_getFailedProcessings_200() throws Exception {
		doReturn(Collections.singletonList(newFailedProcessing()))
			.when(requestRepository)
			.getFailedProcessings();
		
		final String jsonContent = "[{\"id\":\"000000000000000000000001\",\"failureDate\":\"2019-06-18T11:09:03.805Z\",\"missionId\": \"S1\",\"failureMessage\":\"dummyMessage\",\"topic\":\"dummyProcessingType\",\"message\":\"{\\\"foo\\\": \\\"bar\\\"}\",\"stacktrace\": \"...\",\"errorLevel\": \"ERROR\",\"retryCounter\":0}]";
		
		MvcResult result = uut.perform(get("/api/v1/failedProcessings")
			      .contentType(MediaType.APPLICATION_JSON)
			      .header("ApiKey", API_KEY)
        ).andExpect(status().isOk())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
		.andReturn();
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode expected = mapper.readTree(jsonContent);
		JsonNode actual = mapper.readTree(result.getResponse().getContentAsString());
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_getFailedProcessings_403() throws Exception {
	    uut.perform(get("/api/v1/failedProcessings").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
	        "wrong key")).andExpect(status().isForbidden());
	}

	@Test
	public void test_getFailedProcessings_500() throws Exception {
	    doThrow(new RuntimeException()).when(requestRepository).getFailedProcessings();
	    uut.perform(get("/api/v1/failedProcessings").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
	        API_KEY)).andExpect(status().isInternalServerError());
	}

	@Test
	public void test_getFailedProcessingById_200() throws Exception {

		final FailedProcessing failedProcessing = newFailedProcessing();		

		doReturn(failedProcessing).when(requestRepository).getFailedProcessingById(Mockito.anyString());
		
		final String jsonContent = "{\"id\":\"000000000000000000000001\",\"failureDate\":\"2019-06-18T11:09:03.805Z\",\"missionId\": \"S1\",\"failureMessage\":\"dummyMessage\",\"topic\":\"dummyProcessingType\",\"message\":\"{\\\"foo\\\": \\\"bar\\\"}\",\"stacktrace\": \"...\",\"errorLevel\": \"ERROR\",\"retryCounter\":0}";
		
		MvcResult result = uut.perform(get("/api/v1/failedProcessings/000000000000000000000001")
			      .contentType(MediaType.APPLICATION_JSON)
			      .header("ApiKey", API_KEY)
        ).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
		.andReturn();
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode expected = mapper.readTree(jsonContent);
		JsonNode actual = mapper.readTree(result.getResponse().getContentAsString());
		assertEquals(expected, actual);
	}

	@Test
	public void test_getFailedProcessingById_400() throws Exception {
	    uut.perform(get("/api/v1/failedProcessings/invalidParameter").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
	        API_KEY)).andExpect(status().isBadRequest());
	}
	
	@Test
	public void test_getFailedProcessingById_403() throws Exception {
	    uut.perform(get("/api/v1/failedProcessings/000000000000000000000001").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
	        "wrong key")).andExpect(status().isForbidden());
	}

	@Test
	public void test_getFailedProcessingById_404() throws Exception {
		doReturn(null).when(requestRepository).getFailedProcessingById(Mockito.anyString());
	    uut.perform(get("/api/v1/failedProcessings/000000000000000000000001").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
	        API_KEY)).andExpect(status().isNotFound());
	}

	@Test
	public void test_getFailedProcessingById_500() throws Exception {
	    doThrow(new RuntimeException()).when(requestRepository).getFailedProcessingById("000000000000000000000001");
	    uut.perform(get("/api/v1/failedProcessings/000000000000000000000001").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
	        API_KEY)).andExpect(status().isInternalServerError());
	}

	@Test
	public void test_deleteFailedProcessing_200() throws Exception {

		uut.perform(
				delete("/api/v1/failedProcessings/000000000000000000000001").contentType(MediaType.APPLICATION_JSON).header("ApiKey", API_KEY))
				.andExpect(status().isOk());
	}

	@Test
	public void test_deleteFailedProcessing_400() throws Exception {
		uut.perform(delete("/api/v1/failedProcessings/invalidParameter").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				API_KEY)).andExpect(status().isBadRequest());
	}
	
	@Test
	public void test_deleteFailedProcessing_403() throws Exception {
		uut.perform(delete("/api/v1/failedProcessings/000000000000000000000001").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				"wrong Key")).andExpect(status().isForbidden());
	}

	@Test
	public void test_deleteFailedProcessing_404() throws Exception {

		doThrow(new IllegalArgumentException()).when(requestRepository).deleteFailedProcessing("000000000000000000000001");

		uut.perform(
				delete("/api/v1/failedProcessings/000000000000000000000001").contentType(MediaType.APPLICATION_JSON).header("ApiKey", API_KEY))
				.andExpect(status().isNotFound());
	}

	@Test
	public void test_deleteFailedProcessing_500() throws Exception {

		doThrow(new RuntimeException()).when(requestRepository).deleteFailedProcessing("000000000000000000000001");

		uut.perform(
				delete("/api/v1/failedProcessings/000000000000000000000001").contentType(MediaType.APPLICATION_JSON).header("ApiKey", API_KEY))
				.andExpect(status().isInternalServerError());
	}
	
	@Test
	public void test_restartAndDeleteFailedProcessing_200() throws Exception {

		uut.perform(post("/api/v1/failedProcessings/000000000000000000000001/restart").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				API_KEY)).andExpect(status().isOk());
	}

	@Test
	public void test_restartAndDeleteFailedProcessing_400() throws Exception {
		uut.perform(post("/api/v1/failedProcessings/invalidParameter/restart").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				API_KEY)).andExpect(status().isBadRequest());
	}

	@Test
	public void test_restartAndDeleteFailedProcessing_403() throws Exception {

		uut.perform(post("/api/v1/failedProcessings/000000000000000000000001/restart").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				"wrong key")).andExpect(status().isForbidden());
	}

	@Test
	public void test_restartAndDeleteFailedProcessing_404() throws Exception {

		doThrow(new IllegalArgumentException()).when(requestRepository).restartAndDeleteFailedProcessing("000000000000000000000001");
		uut.perform(post("/api/v1/failedProcessings/000000000000000000000001/restart").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				API_KEY)).andExpect(status().isNotFound());
	}

	@Test
	public void test_restartAndDeleteFailedProcessing_500() throws Exception {

		doThrow(new RuntimeException()).when(requestRepository).restartAndDeleteFailedProcessing("000000000000000000000001");
		uut.perform(post("/api/v1/failedProcessings/000000000000000000000001/restart").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				API_KEY)).andExpect(status().isInternalServerError());
	}
	
	@Test
	public void test_countFailedProcessings() throws Exception {
		doReturn(23L).when(requestRepository).getFailedProcessingsCount();		
		uut.perform(get("/api/v1/failedProcessings/count")
				.contentType(MediaType.APPLICATION_JSON)
				.header("ApiKey",API_KEY))
			.andExpect(status().isOk())
			.andExpect(content().string("23"));
	}
}
