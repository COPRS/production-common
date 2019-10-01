package esa.s1pdgs.cpoc.validation.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
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
	
	@Autowired
	private ValidationRestControllerTestConfig config;

	@Test
	public void testValidate200() throws Exception {
		mockMvc.perform(post("/api/v1/validate").contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());
	}

}
