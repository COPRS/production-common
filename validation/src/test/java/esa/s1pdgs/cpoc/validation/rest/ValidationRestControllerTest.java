package esa.s1pdgs.cpoc.validation.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import esa.s1pdgs.cpoc.validation.config.ApplicationProperties;
import esa.s1pdgs.cpoc.validation.service.ValidationService;

@RunWith(SpringRunner.class)
@WebMvcTest(ValidationRestController.class)
public class ValidationRestControllerTest {

	@MockBean
	private ValidationService validationService;

	@MockBean
	private ApplicationProperties properties;

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void testValidate200() throws Exception {

		mockMvc.perform(post("/api/v1/validate").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	public void testValidate500whenRuntimeException() throws Exception {

		doThrow(new RuntimeException("test generated exception")).when(validationService)
				.checkConsistencyForInterval(any(), any());

		mockMvc.perform(post("/api/v1/validate").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());
	}

}
