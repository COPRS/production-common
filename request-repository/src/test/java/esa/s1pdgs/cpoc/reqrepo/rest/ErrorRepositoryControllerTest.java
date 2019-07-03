package esa.s1pdgs.cpoc.reqrepo.rest;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import esa.s1pdgs.cpoc.appcatalog.common.FailedProcessing;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.reqrepo.kafka.producer.SubmissionClient;
import esa.s1pdgs.cpoc.reqrepo.repo.FailedProcessingRepo;
import esa.s1pdgs.cpoc.reqrepo.repo.MqiMessageRepo;
import esa.s1pdgs.cpoc.reqrepo.rest.FailedRequestController;
import esa.s1pdgs.cpoc.reqrepo.service.ErrorRepository;

@RunWith(SpringRunner.class)
@WebMvcTest(FailedRequestController.class)
public class ErrorRepositoryControllerTest {
	
	private static final String API_KEY = FailedRequestController.API_KEY;

	@MockBean
	private MqiMessageRepo mqiMessageRepository;
	
	@MockBean
	private FailedProcessingRepo failedProcessingRepo;

	@MockBean
	private SubmissionClient submissionClient;
	
	@MockBean
	private ErrorRepository errorRepository;
	
	@Autowired
	private MockMvc uut;
	
	@Before
	public void before() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
	
	private final FailedProcessing newFailedProcessing() throws Exception
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
		failedProcessingToReturn.setDto(new GenericMessageDto<Object>()); 
		return failedProcessingToReturn;
	}

	@Test
	public void test_getFailedProcessings_200() throws Exception {
		List<FailedProcessing> failedProcessingsToReturn = new ArrayList<>();

		final FailedProcessing failedProcessing = newFailedProcessing();		
		failedProcessingsToReturn.add(failedProcessing);

		doReturn(failedProcessingsToReturn).when(errorRepository).getFailedProcessings();

		String jsonContent = "[{\n" + 
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
				"    \"processingDetails\": {}\n" + 
				"  }]";
		
		uut.perform(get("/api/v1/failedProcessings")
			      .contentType(MediaType.APPLICATION_JSON)
			      .header("ApiKey", API_KEY)
        ).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andExpect(content().json(jsonContent));
	}
	
	@Test
	public void test_getFailedProcessings_403() throws Exception {
	    uut.perform(get("/api/v1/failedProcessings").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
	        "wrong key")).andExpect(status().isForbidden());
	}

	@Test
	public void test_getFailedProcessings_500() throws Exception {
	    doThrow(new RuntimeException()).when(errorRepository).getFailedProcessings();
	    uut.perform(get("/api/v1/failedProcessings").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
	        API_KEY)).andExpect(status().isInternalServerError());
	}

	@Test
	public void test_getFailedProcessingById_200() throws Exception {

		final FailedProcessing failedProcessing = newFailedProcessing();		

		doReturn(failedProcessing).when(errorRepository).getFailedProcessingById(Mockito.anyLong());
		
		String jsonContent = "{\n" + 
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
				"    \"processingDetails\": {}\n" + 
				"  }";
		uut.perform(get("/api/v1/failedProcessings/1")
			      .contentType(MediaType.APPLICATION_JSON)
			      .header("ApiKey", API_KEY)
        ).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
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
		doReturn(null).when(errorRepository).getFailedProcessingById(Mockito.anyLong());
	    uut.perform(get("/api/v1/failedProcessings/1").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
	        API_KEY)).andExpect(status().isNotFound());
	}

	@Test
	public void test_getFailedProcessingById_500() throws Exception {
	    doThrow(new RuntimeException()).when(errorRepository).getFailedProcessingById(1);
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

		doThrow(new IllegalArgumentException()).when(errorRepository).deleteFailedProcessing(1);

		uut.perform(
				delete("/api/v1/failedProcessings/1").contentType(MediaType.APPLICATION_JSON).header("ApiKey", API_KEY))
				.andExpect(status().isNotFound());
	}

	@Test
	public void test_deleteFailedProcessing_500() throws Exception {

		doThrow(new RuntimeException()).when(errorRepository).deleteFailedProcessing(1);

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

		doThrow(new IllegalArgumentException()).when(errorRepository).restartAndDeleteFailedProcessing(1);
		uut.perform(post("/api/v1/failedProcessings/1/restart").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				API_KEY)).andExpect(status().isNotFound());
	}

	@Test
	public void test_restartAndDeleteFailedProcessing_500() throws Exception {

		doThrow(new RuntimeException()).when(errorRepository).restartAndDeleteFailedProcessing(1);
		uut.perform(post("/api/v1/failedProcessings/1/restart").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				API_KEY)).andExpect(status().isInternalServerError());
	}
	

}
