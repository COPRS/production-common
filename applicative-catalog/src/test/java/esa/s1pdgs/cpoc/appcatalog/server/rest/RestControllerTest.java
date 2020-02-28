package esa.s1pdgs.cpoc.appcatalog.server.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import javax.annotation.Resource;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class RestControllerTest {
	@Resource
	public WebApplicationContext wac;

	protected MockMvc mockMvc;

	protected void initMockMvc(Object... controllers) {
		mockMvc = MockMvcBuilders.standaloneSetup(controllers).build();
	}

	protected ResultActions doGet(final String url) throws Exception {
		return mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON_VALUE));
	}

	protected ResultActions request(final MockHttpServletRequestBuilder builder) throws Exception {
		return mockMvc.perform(builder.accept(MediaType.APPLICATION_JSON_VALUE));
	}

	protected <T> T createFromResult(MvcResult result, Class<T> clazz) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		T response = mapper.readValue(result.getResponse().getContentAsString(), clazz);
		return response;
	}
}