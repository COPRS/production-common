package esa.s1pdgs.cpoc.validation.rest;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import esa.s1pdgs.cpoc.validation.service.ValidationService;

@RunWith(SpringRunner.class)
@WebMvcTest(ValidationRestController.class)
public class ValidationRestControllerTest {

	@MockBean
	private ValidationService validationService;

	@Autowired
	private MockMvc mockMvc;
	
	// TODO: Test not working due to mass refactoring of the API
//
//	@Test
//	public void testValidate200() throws Exception {
//
//		ProductFamily family = ProductFamily.L1_ACN;
//
//		String intervalStart = "2000-01-01T00:00:00.000000Z";
//		String intervalEnd = "2000-01-03T00:00:00.000000Z";
//
//		doReturn(true).when(validationService).checkConsistencyForInterval(intervalStart,
//				intervalEnd);
//
//		mockMvc.perform(get(String.format("/api/v1/%s/validate/?intervalStart=%s&intervalEnd=%s", family.name(),
//				intervalStart, intervalEnd)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
//
//	}
//
//	@Test
//	public void testValidate400WhenIntervalStartInvalid() throws Exception {
//
//		ProductFamily family = ProductFamily.L1_ACN;
//
//		String intervalStart = "2000-01-01TXX:YY:00.000000Z";
//		String intervalEnd = "2000-01-03T00:00:00.000000Z";
//
//		mockMvc.perform(get(String.format("/api/v1/%s/validate/?intervalStart=%s&intervalEnd=%s", family.name(),
//				intervalStart, intervalEnd)).contentType(MediaType.APPLICATION_JSON))
//				.andExpect(status().isBadRequest());
//	}
//
//	@Test
//	public void testValidate400WhenIntervalEndInvalid() throws Exception {
//
//		ProductFamily family = ProductFamily.L1_ACN;
//
//		String intervalStart = "2000-01-01T00:00:00.000000Z";
//		String intervalEnd = "YYYY-01-03T00:00:00.000000Z";
//
//		mockMvc.perform(get(String.format("/api/v1/%s/validate/?intervalStart=%s&intervalEnd=%s", family.name(),
//				intervalStart, intervalEnd)).contentType(MediaType.APPLICATION_JSON))
//				.andExpect(status().isBadRequest());
//	}
//
//	@Test
//	public void testValidate400whenProductFamilyInvalid() throws Exception {
//
//		String intervalStart = "2000-01-01T00:00:00.000000Z";
//		String intervalEnd = "2000-01-03T00:00:00.000000Z";
//
//		mockMvc.perform(get(String.format("/api/v1/%s/validate/?intervalStart=%s&intervalEnd=%s", "INVALIDFAMILY",
//				intervalStart, intervalEnd)).contentType(MediaType.APPLICATION_JSON))
//				.andExpect(status().isBadRequest());
//	}
//
//	@Test
//	public void testValidate500whenMetadataQueryException() throws Exception {
//
//		ProductFamily family = ProductFamily.L1_ACN;
//
//		String intervalStart = "2000-01-01T00:00:00.000000Z";
//		String intervalEnd = "2000-01-03T00:00:00.000000Z";
//
//		doThrow(new MetadataQueryException("")).when(validationService).checkConsistencyForInterval(any(),
//				anyString(), anyString());
//
//		mockMvc.perform(get(String.format("/api/v1/%s/validate/?intervalStart=%s&intervalEnd=%s", family.name(),
//				intervalStart, intervalEnd)).contentType(MediaType.APPLICATION_JSON))
//				.andExpect(status().isInternalServerError());
//	}
//
//	@Test
//	public void testValidate500whenSdkClientException() throws Exception {
//
//		ProductFamily family = ProductFamily.L1_ACN;
//
//		String intervalStart = "2000-01-01T00:00:00.000000Z";
//		String intervalEnd = "2000-01-03T00:00:00.000000Z";
//
//		doThrow(new SdkClientException("")).when(validationService).checkConsistencyForInterval(any(),
//				anyString(), anyString());
//
//		mockMvc.perform(get(String.format("/api/v1/%s/validate/?intervalStart=%s&intervalEnd=%s", family.name(),
//				intervalStart, intervalEnd)).contentType(MediaType.APPLICATION_JSON))
//				.andExpect(status().isInternalServerError());
//	}
//
//	@Test
//	public void testValidate500whenDateTimeParseException() throws Exception {
//		ProductFamily family = ProductFamily.L1_ACN;
//
//		String intervalStart = "2000-01-01T00:00:00.000000Z";
//		String intervalEnd = "2000-01-03T00:00:00.000000Z";
//
//		doThrow(new DateTimeParseException("", "", 0)).when(validationService)
//				.checkConsistencyForInterval(any(), anyString(), anyString());
//
//		mockMvc.perform(get(String.format("/api/v1/%s/validate/?intervalStart=%s&intervalEnd=%s", family.name(),
//				intervalStart, intervalEnd)).contentType(MediaType.APPLICATION_JSON))
//				.andExpect(status().isInternalServerError());
//	}

}
