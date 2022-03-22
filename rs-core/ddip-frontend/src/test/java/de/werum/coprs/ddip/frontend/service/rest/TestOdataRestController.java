package de.werum.coprs.ddip.frontend.service.rest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import de.werum.coprs.ddip.frontend.config.DdipProperties;
import de.werum.coprs.ddip.frontend.util.DdipUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(OdataRestController.class)
public class TestOdataRestController {

	@Autowired
	private DdipProperties ddipProperties;

	@Before
	public void init() {
	}

	@Test
	public void testBuildDispatchPripUrl() throws Exception {
		final URL referenceUrl = new URL(this.ddipProperties.getDispatchPripProtocol(), this.ddipProperties.getDispatchPripHost(),
				this.ddipProperties.getDispatchPripPort(), "");
		final URL dispatchPripUrl = OdataRestController.buildDispatchPripUrl(this.ddipProperties);

		assertTrue(referenceUrl.toURI().equals(dispatchPripUrl.toURI()));
	}

	@Test
	public void testGetHeaders() throws Exception {
		final URI findAllProductsUrl = OdataRestController.buildDispatchPripUrl(this.ddipProperties).toURI().resolve("/odata/v1/Products");

		final MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.GET.name(), findAllProductsUrl.toString());
		request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);

		final HttpHeaders headers = OdataRestController.getHeaders(request);
		assertTrue(headers.size() == 1);
		assertTrue(headers.containsKey(HttpHeaders.ACCEPT));
		assertTrue(headers.getAccept().size() == 1);
		assertTrue(headers.getAccept().contains(MediaType.APPLICATION_JSON));
	}

	@Test
	public void testResponseMapping() throws Exception {
		final String body = "{\"body\":\"content\"}";

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		final ResponseEntity<String> responseEntity = new ResponseEntity<String>(body, headers, HttpStatus.OK);

		final MockHttpServletResponse servletResponse = new MockHttpServletResponse();

		OdataRestController.mapResponse(responseEntity, servletResponse);

		// check header
		assertTrue(servletResponse.getHeaderNames().size() == 1);
		assertTrue(servletResponse.containsHeader(HttpHeaders.CONTENT_TYPE));
		assertTrue(servletResponse.getHeaders(HttpHeaders.CONTENT_TYPE).size() == 1);
		assertTrue(servletResponse.getHeaders(HttpHeaders.CONTENT_TYPE).contains(MediaType.APPLICATION_JSON.toString()));

		// check status
		assertTrue(HttpStatus.OK.value() == servletResponse.getStatus());

		// check body
		assertTrue(body.equals(servletResponse.getContentAsString()));
	}

	@Test
	public void testGetFirstOccuranceOf() throws Exception {
		final String queryParams = "filter=Collection/Name eq 'SampleCollection' and startswith(Name,'S1A_') and ContentDate/Start gt 2020-04-04T16:00:00.000000Z or Collection/Name eq 'AnotherCollection' and startswith(Name,'S1B_') and ContentDate/Start gt 2020-06-06T08:00:00.000000Z&$expand=Attributes";

		final int firstOccurance = DdipUtil.getFirstOccuranceOf(Pattern.compile(" |\\)|&|$"), queryParams);

		assertTrue(firstOccurance == 22);
	}

	@Test
	public void testGetNextEnd() throws Exception {
		final String queryParams = "filter=Collection/Name eq 'SampleCollection' and startswith(Name,'S1A_') and ContentDate/Start gt 2020-04-04T16:00:00.000000Z or Collection/Name eq 'AnotherCollection' and startswith(Name,'S1B_') and ContentDate/Start gt 2020-06-06T08:00:00.000000Z&$expand=Attributes";

		final int nextEnd = OdataRestController.getNextEnd(queryParams.substring(26));

		assertTrue(nextEnd == 18); // length of 'SampleCollection'
	}

	@Test
	public void testTranslateCollectionQueryParameters() throws Exception {
		assertTrue(this.ddipProperties.getCollections().containsKey("SampleCollection"));
		assertTrue(this.ddipProperties.getCollections().containsKey("AnotherCollection"));

		final String queryParams = "filter=Collection/Name eq 'SampleCollection' and startswith(Name,'S1A_') and ContentDate/Start gt 2020-04-04T16:00:00.000000Z or Collection/Name eq 'AnotherCollection' and startswith(Name,'S1B_') and ContentDate/Start gt 2020-06-06T08:00:00.000000Z&$expand=Attributes";
		final String expectedQueryParams = "filter=(" + this.ddipProperties.getCollections().get("SampleCollection")
				+ ") and startswith(Name,'S1A_') and ContentDate/Start gt 2020-04-04T16:00:00.000000Z or ("
				+ this.ddipProperties.getCollections().get("AnotherCollection")
				+ ") and startswith(Name,'S1B_') and ContentDate/Start gt 2020-06-06T08:00:00.000000Z&$expand=Attributes";

		final String transletedQueryParams = OdataRestController.translateCollectionQueryParameters(queryParams, this.ddipProperties.getCollections());

		assertTrue(expectedQueryParams.equals(transletedQueryParams));
	}

	@Test
	public void testDecodingQueryParameters() throws Exception {
		assertTrue(this.ddipProperties.getCollections().containsKey("SampleCollection"));
		assertTrue(this.ddipProperties.getCollections().containsKey("AnotherCollection"));

		final String queryParams = "filter=Collection/Name eq 'SampleCollection' and startswith(Name,'S1A_') and ContentDate/Start gt 2020-04-04T16:00:00.000000Z or Collection/Name eq 'AnotherCollection' and startswith(Name,'S1B_') and ContentDate/Start gt 2020-06-06T08:00:00.000000Z&$expand=Attributes";
		final String expectedQueryParams = "filter=(" + this.ddipProperties.getCollections().get("SampleCollection")
				+ ") and startswith(Name,'S1A_') and ContentDate/Start gt 2020-04-04T16:00:00.000000Z or ("
				+ this.ddipProperties.getCollections().get("AnotherCollection")
				+ ") and startswith(Name,'S1B_') and ContentDate/Start gt 2020-06-06T08:00:00.000000Z&$expand=Attributes";
		final String plusEncodedQueryParams = URLEncoder.encode(queryParams, StandardCharsets.UTF_8.toString());
		final String twentyEncodedQueryParams = "filter=Collection/Name%20eq%20'SampleCollection'%20and%20startswith(Name,'S1A_')%20and%20ContentDate/Start%20gt%202020-04-04T16:00:00.000000Z%20or%20Collection/Name%20eq%20'AnotherCollection'%20and%20startswith(Name,'S1B_')%20and%20ContentDate/Start%20gt%202020-06-06T08:00:00.000000Z&$expand=Attributes";

		final String translatedFromPlusEncodedQueryParams = OdataRestController.translateCollectionQueryParameters(plusEncodedQueryParams,
				this.ddipProperties.getCollections());
		assertTrue(expectedQueryParams.equals(translatedFromPlusEncodedQueryParams));

		final String translatedFromTwentyEncodedQueryParams = OdataRestController.translateCollectionQueryParameters(twentyEncodedQueryParams,
				this.ddipProperties.getCollections());
		assertTrue(expectedQueryParams.equals(translatedFromTwentyEncodedQueryParams));
	}

}
