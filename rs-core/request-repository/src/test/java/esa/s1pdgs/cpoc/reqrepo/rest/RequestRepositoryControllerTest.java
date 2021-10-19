package esa.s1pdgs.cpoc.reqrepo.rest;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import esa.s1pdgs.cpoc.appcatalog.common.FailedProcessing;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.message.MessageProducer;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.reqrepo.repo.FailedProcessingRepo;
import esa.s1pdgs.cpoc.reqrepo.repo.MqiMessageRepo;
import esa.s1pdgs.cpoc.reqrepo.service.RequestRepository;

@RunWith(SpringRunner.class)
@WebMvcTest(RequestRepositoryController.class)
@ActiveProfiles("test")
public class RequestRepositoryControllerTest {
	
	private static final String API_KEY = RequestRepositoryController.API_KEY;

	@MockBean
	private MqiMessageRepo mqiMessageRepository;
	
	@MockBean
	private FailedProcessingRepo failedProcessingRepo;

	@MockBean
	private MessageProducer<Object> messageProducer;
	
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
		failedProcessingToReturn.setId(1001);
		failedProcessingToReturn.setTopic("dummyProcessingType");
		failedProcessingToReturn.setState(MessageState.READ);
		failedProcessingToReturn.setCategory(ProductCategory.AUXILIARY_FILES);
		failedProcessingToReturn.setPartition(9);
		failedProcessingToReturn.setOffset(1234);
		failedProcessingToReturn.setGroup("dummyGroup");
		failedProcessingToReturn.setFailedPod("pod1234");
		failedProcessingToReturn.setLastAssignmentDate(dateFormat.parse("2019-06-18T11:09:03.805Z"));
		failedProcessingToReturn.setSendingPod("pod5678");
		failedProcessingToReturn.setLastSendDate(dateFormat.parse("2019-06-18T11:09:03.805Z"));
		failedProcessingToReturn.setLastAckDate(dateFormat.parse("2019-06-18T11:09:03.805Z"));
		failedProcessingToReturn.setNbRetries(3);
		failedProcessingToReturn.setCreationDate(dateFormat.parse("2019-06-18T11:09:03.805Z"));
		failedProcessingToReturn.setFailureDate(dateFormat.parse("2019-06-18T11:09:03.805Z"));
		failedProcessingToReturn.setFailureMessage("dummyMessage");
		failedProcessingToReturn.setDto(Collections.singletonList(new GenericMessageDto<>()));
		return failedProcessingToReturn;
	}

	@Test
	public void test_getFailedProcessings_200() throws Exception {
		doReturn(Collections.singletonList(newFailedProcessing()))
			.when(requestRepository)
			.getFailedProcessings();

		final String jsonContent = "[{\n" + 
				"    \"id\": 1001,\n" +
				"    \"processingType\": \"dummyProcessingType\",\n" + 
				"    \"processingStatus\": \"READ\",\n" + 
				"    \"productCategory\": \"AUXILIARY_FILES\",\n" + 
				"    \"partition\": 9,\n" + 
				"    \"offset\": 1234,\n" + 
				"    \"group\": \"dummyGroup\",\n" + 
				"    \"failedPod\": \"pod1234\",\n" + 
				"    \"lastAssignmentDate\": \"2019-06-18T11:09:03.805Z\",\n" + 
				"    \"sendingPod\": \"pod5678\",\n" + 
				"    \"lastSendDate\": \"2019-06-18T11:09:03.805Z\",\n" + 
				"    \"lastAckDate\": \"2019-06-18T11:09:03.805Z\",\n" + 
				"    \"nbRetries\": 3,\n" + 
				"    \"creationDate\": \"2019-06-18T11:09:03.805Z\",\n" + 
				"    \"failureDate\": \"2019-06-18T11:09:03.805Z\",\n" + 
				"    \"failureMessage\": \"dummyMessage\",\n" + 
				"    \"processingDetails\": [{}]\n" + 
				"  }]";
		
		uut.perform(get("/api/v1/failedProcessings")
			      .contentType(MediaType.APPLICATION_JSON)
			      .header("ApiKey", API_KEY)
        ).andExpect(status().isOk())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
		.andExpect(content().json(jsonContent));
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

		doReturn(failedProcessing).when(requestRepository).getFailedProcessingById(Mockito.anyLong());
		
		final String jsonContent = "{\n" + 
				"    \"id\": 1001,\n" +
				"    \"processingType\": \"dummyProcessingType\",\n" + 
				"    \"processingStatus\": \"READ\",\n" + 
				"    \"productCategory\": \"AUXILIARY_FILES\",\n" + 
				"    \"partition\": 9,\n" + 
				"    \"offset\": 1234,\n" + 
				"    \"group\": \"dummyGroup\",\n" + 
				"    \"failedPod\": \"pod1234\",\n" + 
				"    \"lastAssignmentDate\": \"2019-06-18T11:09:03.805Z\",\n" + 
				"    \"sendingPod\": \"pod5678\",\n" + 
				"    \"lastSendDate\": \"2019-06-18T11:09:03.805Z\",\n" + 
				"    \"lastAckDate\": \"2019-06-18T11:09:03.805Z\",\n" + 
				"    \"nbRetries\": 3,\n" + 
				"    \"creationDate\": \"2019-06-18T11:09:03.805Z\",\n" + 
				"    \"failureDate\": \"2019-06-18T11:09:03.805Z\",\n" + 
				"    \"failureMessage\": \"dummyMessage\",\n" + 
				"    \"processingDetails\": [{}]\n" + 
				"  }";
		uut.perform(get("/api/v1/failedProcessings/1")
			      .contentType(MediaType.APPLICATION_JSON)
			      .header("ApiKey", API_KEY)
        ).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
		.andExpect(content().json(jsonContent));
	}

	@Test
	public void test_getFailedProcessingById_400() throws Exception {
	    uut.perform(get("/api/v1/failedProcessings/invalidParameter").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
	        API_KEY)).andExpect(status().isBadRequest());
	}
	
	@Test
	public void test_getFailedProcessingById_403() throws Exception {
	    uut.perform(get("/api/v1/failedProcessings/1").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
	        "wrong key")).andExpect(status().isForbidden());
	}

	@Test
	public void test_getFailedProcessingById_404() throws Exception {
		doReturn(null).when(requestRepository).getFailedProcessingById(Mockito.anyLong());
	    uut.perform(get("/api/v1/failedProcessings/1").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
	        API_KEY)).andExpect(status().isNotFound());
	}

	@Test
	public void test_getFailedProcessingById_500() throws Exception {
	    doThrow(new RuntimeException()).when(requestRepository).getFailedProcessingById(1);
	    uut.perform(get("/api/v1/failedProcessings/1").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
	        API_KEY)).andExpect(status().isInternalServerError());
	}

	@Test
	public void test_deleteFailedProcessing_200() throws Exception {

		uut.perform(
				delete("/api/v1/failedProcessings/1").contentType(MediaType.APPLICATION_JSON).header("ApiKey", API_KEY))
				.andExpect(status().isOk());
	}

	@Test
	public void test_deleteFailedProcessing_400() throws Exception {
		uut.perform(delete("/api/v1/failedProcessings/invalidParameter").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				API_KEY)).andExpect(status().isBadRequest());
	}
	
	@Test
	public void test_deleteFailedProcessing_403() throws Exception {
		uut.perform(delete("/api/v1/failedProcessings/1").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				"wrong Key")).andExpect(status().isForbidden());
	}

	@Test
	public void test_deleteFailedProcessing_404() throws Exception {

		doThrow(new IllegalArgumentException()).when(requestRepository).deleteFailedProcessing(1);

		uut.perform(
				delete("/api/v1/failedProcessings/1").contentType(MediaType.APPLICATION_JSON).header("ApiKey", API_KEY))
				.andExpect(status().isNotFound());
	}

	@Test
	public void test_deleteFailedProcessing_500() throws Exception {

		doThrow(new RuntimeException()).when(requestRepository).deleteFailedProcessing(1);

		uut.perform(
				delete("/api/v1/failedProcessings/1").contentType(MediaType.APPLICATION_JSON).header("ApiKey", API_KEY))
				.andExpect(status().isInternalServerError());
	}
	
	@Test
	public void test_restartAndDeleteFailedProcessing_200() throws Exception {

		uut.perform(post("/api/v1/failedProcessings/1/restart").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				API_KEY)).andExpect(status().isOk());
	}

	@Test
	public void test_restartAndDeleteFailedProcessing_400() throws Exception {
		uut.perform(post("/api/v1/failedProcessings/invalidParameter/restart").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				API_KEY)).andExpect(status().isBadRequest());
	}

	@Test
	public void test_restartAndDeleteFailedProcessing_403() throws Exception {

		uut.perform(post("/api/v1/failedProcessings/1/restart").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				"wrong key")).andExpect(status().isForbidden());
	}

	@Test
	public void test_restartAndDeleteFailedProcessing_404() throws Exception {

		doThrow(new IllegalArgumentException()).when(requestRepository).restartAndDeleteFailedProcessing(1);
		uut.perform(post("/api/v1/failedProcessings/1/restart").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				API_KEY)).andExpect(status().isNotFound());
	}

	@Test
	public void test_restartAndDeleteFailedProcessing_500() throws Exception {

		doThrow(new RuntimeException()).when(requestRepository).restartAndDeleteFailedProcessing(1);
		uut.perform(post("/api/v1/failedProcessings/1/restart").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				API_KEY)).andExpect(status().isInternalServerError());
	}
	
	@Test
	public void test_countProcessings() throws Exception {
		doReturn(42L).when(requestRepository).getProcessingsCount(Mockito.any(), Mockito.any());		
		uut.perform(get("/api/v1/processings/count")
				.contentType(MediaType.APPLICATION_JSON)
				.header("ApiKey",API_KEY))
			.andExpect(status().isOk())
			.andExpect(content().string("42"));
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
