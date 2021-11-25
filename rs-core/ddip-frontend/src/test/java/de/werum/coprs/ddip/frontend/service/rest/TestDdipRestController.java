package de.werum.coprs.ddip.frontend.service.rest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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
		this.mockMvc.perform(MockMvcRequestBuilders.get("/app/ping") //
				.contentType(MediaType.APPLICATION_JSON))
				//.andDo(MockMvcResultHandlers.print()) //
				.andExpect(status().isOk()) //
				.andExpect(content().json(String.format("{\"apiVersion\":\"%s\"}", this.ddipProperties.getVersion())));
	}

}
