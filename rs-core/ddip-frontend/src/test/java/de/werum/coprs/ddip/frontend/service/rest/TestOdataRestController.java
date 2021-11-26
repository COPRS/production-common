package de.werum.coprs.ddip.frontend.service.rest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.net.URI;
import java.net.URL;

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

}
