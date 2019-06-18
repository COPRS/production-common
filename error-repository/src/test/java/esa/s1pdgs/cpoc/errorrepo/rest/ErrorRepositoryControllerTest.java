package esa.s1pdgs.cpoc.errorrepo.rest;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.errorrepo.service.ErrorRepository;

@RunWith(SpringRunner.class)
@WebMvcTest(ErrorRepositoryController.class)
public class ErrorRepositoryControllerTest {
	
	private static final String API_KEY = "errorRepositorySecretKey";

	@MockBean
	ErrorRepository errorRepository;
	
	@Autowired
	private MockMvc uut;
	
	@Test
	public void test_getFailedProcessings_200() throws Exception {
		@SuppressWarnings("rawtypes")
		List<FailedProcessingDto> failedProcessingsToReturn = new ArrayList<>();
		doReturn(failedProcessingsToReturn).when(errorRepository).getFailedProcessings();
		uut.perform(get("/errors/failedProcessings")
			      .contentType(MediaType.APPLICATION_JSON)
			      .header("ApiKey", API_KEY)
        ).andExpect(status().isOk());
	}
	
	@Test
	public void test_getFailedProcessingsById_200() throws Exception {
		uut.perform(get("/errors/failedProcessings/1")
			      .contentType(MediaType.APPLICATION_JSON)
			      .header("ApiKey", API_KEY)
        ).andExpect(status().isOk());
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
