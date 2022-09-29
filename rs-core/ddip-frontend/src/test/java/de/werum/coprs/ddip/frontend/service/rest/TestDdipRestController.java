package de.werum.coprs.ddip.frontend.service.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.werum.coprs.ddip.frontend.config.DdipProperties;

@RunWith(SpringRunner.class)
@WebMvcTest(DdipRestController.class)
public class TestDdipRestController {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private DdipProperties ddipProperties;

	@Test
	public void testPing() throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/app/ping") //
				.contentType(MediaType.APPLICATION_JSON))
				//.andDo(MockMvcResultHandlers.print()) //
				.andExpect(status().isOk()).andReturn();
		
		final String expectedJson = String.format("{\"apiVersion\":\"%s\"}", ddipProperties.getVersion()); 
		ObjectMapper mapper = new ObjectMapper();
		JsonNode expected = mapper.readTree(expectedJson);
		JsonNode actual = mapper.readTree(result.getResponse().getContentAsString());
		assertEquals(expected, actual);
	}

}
