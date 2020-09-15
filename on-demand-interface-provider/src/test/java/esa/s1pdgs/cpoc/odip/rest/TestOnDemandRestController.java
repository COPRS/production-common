package esa.s1pdgs.cpoc.odip.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@RunWith(SpringRunner.class)
@WebMvcTest(OnDemandRestController.class)
@ActiveProfiles("test")
public class TestOnDemandRestController {
	
	@Autowired
	private MockMvc mockMvc;	
//	
//	@Test
//	public final void foo() throws Exception {
//		final CatalogEvent ev = new CatalogEvent();
//		
//		System.out.println(asJsonString(ev));
//	}
//	public static String asJsonString(final Object obj) {
//	    try {
//	        final ObjectMapper mapper = new ObjectMapper();
//	        final String jsonContent = mapper.writeValueAsString(obj);
//	        return jsonContent;
//	    } catch (final Exception e) {
//	        throw new RuntimeException(e);
//	    }
//	}  
	
	@Test
	public final void test() throws Exception {
		final String jsonContent = "{\"@class\":\"esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent\",\"productFamily\":\"L0_SLICE\",\"keyObjectStorage\":\"S1B_IW_RAW__0SDV_20200120T160840_20200120T160913_019902_025A5F_94E8.SAFE\",\"uid\":\"a96c4751-c4a0-4c8b-8bc0-54eaec607bef\",\"creationDate\":\"2020-09-13T16:37:30.458Z\",\"hostname\":\"s1pro-metadata-catalog-worker-0\",\"allowedActions\":[\"RESUBMIT\"],\"demandType\":\"NOMINAL\",\"retryCounter\":0,\"debug\":false,\"productName\":\"S1B_IW_RAW__0SDV_20200120T160840_20200120T160913_019902_025A5F_94E8.SAFE\",\"productType\":\"IW_RAW__0S\",\"metadata\":{\"missionDataTakeId\":154207,\"productFamily\":\"L0_SLICE\",\"theoreticalSliceLength\":25,\"sliceCoordinates\":{\"orientation\":\"counterclockwise\",\"coordinates\":[[[35.662,-14.1325],[35.1823,-12.1907],[32.9457,-12.5528],[33.407,-14.5035],[35.662,-14.1325]]],\"type\":\"polygon\"},\"insertionTime\":\"2020-09-13T16:37:30.406000Z\",\"creationTime\":\"2020-09-13T16:37:30.406000Z\",\"polarisation\":\"DV\",\"sliceNumber\":5,\"absoluteStopOrbit\":19902,\"resolution\":\"_\",\"circulationFlag\":3,\"productName\":\"S1B_IW_RAW__0SDV_20200120T160840_20200120T160913_019902_025A5F_94E8.SAFE\",\"dataTakeId\":\"025A5F\",\"productConsolidation\":\"SLICE\",\"absoluteStartOrbit\":19902,\"validityStopTime\":\"2020-01-20T16:09:13.204826Z\",\"instrumentConfigurationId\":1,\"relativeStopOrbit\":101,\"relativeStartOrbit\":101,\"startTime\":\"2020-01-20T16:08:40.804848Z\",\"stopTime\":\"2020-01-20T16:09:13.204826Z\",\"productType\":\"IW_RAW__0S\",\"productClass\":\"S\",\"missionId\":\"S1\",\"swathtype\":\"IW\",\"pass\":\"ASCENDING\",\"satelliteId\":\"B\",\"stopTimeANX\":5709038.494,\"url\":\"S1B_IW_RAW__0SDV_20200120T160840_20200120T160913_019902_025A5F_94E8.SAFE\",\"timeliness\":\"FAST24\",\"oqcFlag\":\"CHECKED_OK\",\"sliceOverlap\":7.4,\"startTimeANX\":5676638.516,\"validityStartTime\":\"2020-01-20T16:08:40.804848Z\",\"processMode\":\"NOMINAL\"}}";
	
		final MockHttpServletRequestBuilder requestBuilder = post("/odip/v1/myTopic")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonContent);
		
		mockMvc.perform(requestBuilder);	
	}
}
