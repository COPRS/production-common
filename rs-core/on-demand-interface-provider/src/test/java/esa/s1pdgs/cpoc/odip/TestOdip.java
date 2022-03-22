//package esa.s1pdgs.cpoc.odip;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
//
//import esa.s1pdgs.cpoc.appcatalog.common.OnDemandProcessingRequest;
//import esa.s1pdgs.cpoc.appstatus.AppStatus;
//import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
//import esa.s1pdgs.cpoc.odip.config.OdipConfigurationProperties;
//import esa.s1pdgs.cpoc.odip.kafka.producer.SubmissionClient;
//import esa.s1pdgs.cpoc.odip.rest.TestOnDemandRestController;
//import esa.s1pdgs.cpoc.odip.service.OnDemandService;
//
//@Configuration
//public class TestOdip {
//	
////	@Autowired
////	private MockMvc mockMvc;
//	
//	@Mock
//	private MetadataClient metadataClient;	
//	@Mock
//	private SubmissionClient submissionClient;
//
//	private OnDemandService uut;
//
//	@Before
//	public void setUp() {
//		MockitoAnnotations.initMocks(this);
//		
//		submissionClient = new SubmissionClient() {
//			@Override
//			public void resubmit(Object message, AppStatus appStatus) {
//				System.out.println(message);
//			}
//		};
//		
//		this.uut = new OnDemandService(
//				new OdipConfigurationProperties(), 
//				submissionClient, AppStatus.NULL,
//				metadataClient
//		);
//	}
//
//	@Test
//	public final void test() throws Exception {
//		final String jsonContent = TestOnDemandRestController.asJsonString(new OnDemandProcessingRequest("ProductNameA", false, "ModeA", "L1"));	
//		final MockHttpServletRequestBuilder requestBuilder = post("/odip/v1/")
//				.contentType(MediaType.APPLICATION_JSON)
//				.content(jsonContent);
//		
//		
//		
//		mockMvc.perform(requestBuilder);	
//	}
//}
