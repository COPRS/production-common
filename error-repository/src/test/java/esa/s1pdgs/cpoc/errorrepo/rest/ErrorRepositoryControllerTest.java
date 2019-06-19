package esa.s1pdgs.cpoc.errorrepo.rest;

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

import esa.s1pdgs.cpoc.appcatalog.rest.MqiStateMessageEnum;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.errorrepo.service.ErrorRepository;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
@RunWith(SpringRunner.class)
@WebMvcTest(ErrorRepositoryController.class)
public class ErrorRepositoryControllerTest {
	
	private static final String API_KEY = "errorRepositorySecretKey";

	@MockBean
	ErrorRepository errorRepository;
	
	@Autowired
	private MockMvc uut;
	
	@Before
	public void before() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
	
	@Test
	public void test_getFailedProcessings_200() throws Exception {
		@SuppressWarnings("rawtypes")
		List<FailedProcessingDto> failedProcessingsToReturn = new ArrayList<>();
		doReturn(failedProcessingsToReturn).when(errorRepository).getFailedProcessings();
		String jsonContent = "[]";
		uut.perform(get("/errors/failedProcessings")
			      .contentType(MediaType.APPLICATION_JSON)
			      .header("ApiKey", API_KEY)
        ).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andExpect(content().json(jsonContent));
	}
	
	@Test
	public void test_getFailedProcessings_403() throws Exception {
	    uut.perform(get("/errors/failedProcessings").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
	        "wrong key")).andExpect(status().isForbidden());
	}

	@Test
	public void test_getFailedProcessings_500() throws Exception {
	    doThrow(new RuntimeException()).when(errorRepository).getFailedProcessings();
	    uut.perform(get("/errors/failedProcessings").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
	        API_KEY)).andExpect(status().isInternalServerError());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_getFailedProcessingsById_200() throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
		@SuppressWarnings("rawtypes")
		FailedProcessingDto failedProcessingToReturn = new FailedProcessingDto();
		failedProcessingToReturn.setIdentifier(1001);
		failedProcessingToReturn.processingType("dummyProcessingType")
		.processingStatus(MqiStateMessageEnum.READ)
		.productCategory(ProductCategory.AUXILIARY_FILES)
		.partition(9)
		.offset(1234)
		.group("dummyGroup")
		.failedPod("pod1234")
		.lastAssignmentDate(dateFormat.parse("2019-06-18T11:09:03.805Z"))
		.sendingPod("pod5678")
		.lastSendDate(dateFormat.parse("2019-06-18T11:09:03.805Z"))
		.lastAckDate(dateFormat.parse("2019-06-18T11:09:03.805Z"))
		.nbRetries(3)
		.creationDate(dateFormat.parse("2019-06-18T11:09:03.805Z"))
		.failureDate(dateFormat.parse("2019-06-18T11:09:03.805Z"))
		.failureMessage("dummyMessage")
		.processingDetails(new GenericMessageDto<Object>()); // TODO create more detailed dummy object
		doReturn(failedProcessingToReturn).when(errorRepository).getFailedProcessingById(Mockito.anyLong());
		
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
		uut.perform(get("/errors/failedProcessings/1")
			      .contentType(MediaType.APPLICATION_JSON)
			      .header("ApiKey", API_KEY)
        ).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
		.andExpect(content().json(jsonContent));
	}

	@Test
	public void test_getFailedProcessingById_400() throws Exception {
	    uut.perform(get("/errors/failedProcessings/invalidParameter").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
	        API_KEY)).andExpect(status().isBadRequest());
	}
	
	@Test
	public void test_getFailedProcessingById_403() throws Exception {
	    uut.perform(get("/errors/failedProcessings/1").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
	        "wrong key")).andExpect(status().isForbidden());
	}

	@Test
	public void test_getFailedProcessingById_404() throws Exception {
		doReturn(null).when(errorRepository).getFailedProcessingById(Mockito.anyLong());
	    uut.perform(get("/errors/failedProcessings/1").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
	        API_KEY)).andExpect(status().isNotFound());
	}

	@Test
	public void test_getFailedProcessingById_500() throws Exception {
	    doThrow(new RuntimeException()).when(errorRepository).getFailedProcessingById(1);
	    uut.perform(get("/errors/failedProcessings/1").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
	        API_KEY)).andExpect(status().isInternalServerError());
	}

	@Test
	public void test_restartFailedProcessing_200() throws Exception {
		uut.perform(post("/errors/failedProcessings/1/restart")
			      .contentType(MediaType.APPLICATION_JSON)
			      .header("ApiKey", API_KEY)
        ).andExpect(status().isOk());
	}

	@Test
	public void test_deleteFailedProcessing_200() throws Exception {

		uut.perform(
				delete("/errors/failedProcessings/1").contentType(MediaType.APPLICATION_JSON).header("ApiKey", API_KEY))
				.andExpect(status().isOk());
	}

	@Test
	public void test_deleteFailedProcessing_400() throws Exception {
		uut.perform(delete("/errors/failedProcessings/invalidParameter").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				API_KEY)).andExpect(status().isBadRequest());
	}
	
	@Test
	public void test_deleteFailedProcessing_403() throws Exception {
		uut.perform(delete("/errors/failedProcessings/1").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				"wrong Key")).andExpect(status().isForbidden());
	}

	@Test
	public void test_deleteFailedProcessing_404() throws Exception {

		doThrow(new IllegalArgumentException()).when(errorRepository).deleteFailedProcessing(1);

		uut.perform(
				delete("/errors/failedProcessings/1").contentType(MediaType.APPLICATION_JSON).header("ApiKey", API_KEY))
				.andExpect(status().isNotFound());
	}

	@Test
	public void test_deleteFailedProcessing_500() throws Exception {

		doThrow(new RuntimeException()).when(errorRepository).deleteFailedProcessing(1);

		uut.perform(
				delete("/errors/failedProcessings/1").contentType(MediaType.APPLICATION_JSON).header("ApiKey", API_KEY))
				.andExpect(status().isInternalServerError());
	}

	@Test
	public void test_restartAndDeleteFailedProcessing_200() throws Exception {

		uut.perform(post("/errors/failedProcessings/1/restart").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				API_KEY)).andExpect(status().isOk());
	}

	@Test
	public void test_restartAndDeleteFailedProcessing_400() throws Exception {
		uut.perform(post("/errors/failedProcessings/invalidParameter/restart").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				API_KEY)).andExpect(status().isBadRequest());
	}

	@Test
	public void test_restartAndDeleteFailedProcessing_403() throws Exception {

		uut.perform(post("/errors/failedProcessings/1/restart").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				"wrong key")).andExpect(status().isForbidden());
	}

	@Test
	public void test_restartAndDeleteFailedProcessing_404() throws Exception {

		doThrow(new IllegalArgumentException()).when(errorRepository).restartAndDeleteFailedProcessing(1);
		uut.perform(post("/errors/failedProcessings/1/restart").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				API_KEY)).andExpect(status().isNotFound());
	}

	@Test
	public void test_restartAndDeleteFailedProcessing_500() throws Exception {

		doThrow(new RuntimeException()).when(errorRepository).restartAndDeleteFailedProcessing(1);
		uut.perform(post("/errors/failedProcessings/1/restart").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
				API_KEY)).andExpect(status().isInternalServerError());
	}
	

}
