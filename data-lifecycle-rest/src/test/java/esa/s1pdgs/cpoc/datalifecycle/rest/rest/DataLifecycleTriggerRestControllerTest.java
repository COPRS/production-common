package esa.s1pdgs.cpoc.datalifecycle.rest.rest;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.trigger.service.DataLifecycleService;

@RunWith(SpringRunner.class)
@WebMvcTest(DataLifecycleTriggerRestController.class)
//@ActiveProfiles("test")
//@SpringBootTest
@DirtiesContext
public class DataLifecycleTriggerRestControllerTest {

	private static final String API_KEY = DataLifecycleTriggerRestController.API_KEY;

	@MockBean
	private DataLifecycleService service;

	@Autowired
	private MockMvc uut;

	@Before
	public void before() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	private DataLifecycleMetadata newDataLifecycleMetadata() throws Exception {
		final DataLifecycleMetadata dataLifecycleMetadataToReturn = new DataLifecycleMetadata();
		dataLifecycleMetadataToReturn.setAvailableInLta(true);
		dataLifecycleMetadataToReturn.setEvictionDateInCompressedStorage(DateUtils.parse("2019-06-18T11:09:03.805Z"));
		dataLifecycleMetadataToReturn.setEvictionDateInUncompressedStorage(DateUtils.parse("2019-06-18T11:09:03.805Z"));
		dataLifecycleMetadataToReturn.setLastModified(DateUtils.parse("2019-06-18T11:09:03.805Z"));
		dataLifecycleMetadataToReturn.setPathInCompressedStorage("dummyPath");
		dataLifecycleMetadataToReturn.setPathInUncompressedStorage("dummyPath");
		dataLifecycleMetadataToReturn.setPersistentInCompressedStorage(false);
		dataLifecycleMetadataToReturn.setPersistentInUncompressedStorage(true);
		dataLifecycleMetadataToReturn.setProductFamilyInUncompressedStorage(ProductFamily.L0_ACN);
		dataLifecycleMetadataToReturn.setProductFamilyInCompressedStorage(ProductFamily.L0_ACN);
		dataLifecycleMetadataToReturn.setProductName("dummyProductName");
		return dataLifecycleMetadataToReturn;
	}
	
	@Test
	public void test_getProduct_200() throws Exception {
		doReturn(Collections.singletonList(newDataLifecycleMetadata()))
			.when(service)
			.getProduct("dummyProductName");
		
		final String jsonContent = "[{\n" + 
				// TODO
				"  }]";
		
		uut.perform(get("/api/v1/products/dummyProductName")
			      .contentType(MediaType.APPLICATION_JSON)
			      .header("ApiKey", API_KEY)
        ).andExpect(status().isOk())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
		.andExpect(content().json(jsonContent));
	}
}
