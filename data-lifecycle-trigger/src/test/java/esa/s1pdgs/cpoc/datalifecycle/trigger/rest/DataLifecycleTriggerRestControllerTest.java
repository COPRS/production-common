package esa.s1pdgs.cpoc.datalifecycle.trigger.rest;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.datalifecycle.trigger.rest.model.Product;

public class DataLifecycleTriggerRestControllerTest extends RestControllerTest {

	private static final String API_KEY = DataLifecycleTriggerRestController.API_KEY;

	@Mock
	private DataLifecycleServiceDelegator delegator;

	private DataLifecycleTriggerRestController controller;

	@Before
	public void init() throws IOException {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		MockitoAnnotations.initMocks(this);

		this.controller = new DataLifecycleTriggerRestController(delegator);

		this.initMockMvc(this.controller);
	}
	
	private Product newProduct() throws Exception {
		final Product productToReturn = new Product();
		productToReturn.setAvailableInLta(true);
		productToReturn.setEvictionTimeInCompressedStorage("2019-06-18T11:09:03.805Z");
		productToReturn.setEvictionTimeInUncompressedStorage("2019-06-18T11:09:03.805Z");
		productToReturn.setLastModificationTime("2019-06-18T11:09:03.805Z");
		productToReturn.setPathInCompressedStorage("dummyPath");
		productToReturn.setPathInUncompressedStorage("dummyPath");
		productToReturn.setPersistentInCompressedStorage(false);
		productToReturn.setPersistentInUncompressedStorage(true);
		productToReturn.setProductFamilyInCompressedStorage(ProductFamily.L0_ACN.toString());
		productToReturn.setProductFamilyInUncompressedStorage(ProductFamily.L0_ACN.toString());
		productToReturn.setProductname("dummyProductName");
		return productToReturn;
	}

	@Test
	public void test_getProduct_200() throws Exception {
		doReturn(newProduct()).when(delegator).getProduct("dummyProductName");

		final String jsonContent = "{\n" +
		// TODO
				"  }";

		mockMvc.perform(get("/api/v1/products/dummyProductName").contentType(MediaType.APPLICATION_JSON)
				.header("ApiKey", API_KEY)).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json(jsonContent));
	}
	
//	@Test
//	public void test_getProduct_400() throws Exception {
//		mockMvc.perform(get("/api/v1/products/invalidParameter").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
//	        API_KEY)).andExpect(status().isBadRequest());
//	}
//	
//	@Test
//	public void test_getProduct_403() throws Exception {
//		mockMvc.perform(get("/api/v1/products/1").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
//	        "wrong key")).andExpect(status().isForbidden());
//	}

//	@Test
//	public void test_getProduct_404() throws Exception {
//		doThrow(new DataLifecycleMetadataNotFoundException("error")).when(delegator).getProduct("foo");
//		mockMvc.perform(get("/api/v1/products/foo").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
//	        API_KEY)).andExpect(status().isNotFound());
//	}

//	@Test
//	public void test_getProduct_500() throws Exception {
//	    doThrow(new DataLifecycleTriggerInternalServerErrorException("error")).when(delegator).getProduct("foo");
//	    mockMvc.perform(get("/api/v1/products/foo").contentType(MediaType.APPLICATION_JSON).header("ApiKey",
//	        API_KEY)).andExpect(status().isInternalServerError());
//	}
	
//	@Test
//	public void test_getProducts_200() throws Exception {
//		doReturn(newProduct()).when(delegator).getProduct("dummyProductName");
//
//		final String jsonContent = "[{\n" +
//		// TODO
//				"  }]";
//
//		mockMvc.perform(get("/api/v1/products/dummyProductName").contentType(MediaType.APPLICATION_JSON)
//				.header("ApiKey", API_KEY)).andExpect(status().isOk())
//				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//				.andExpect(content().json(jsonContent));
//	}
}
