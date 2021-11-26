package de.werum.coprs.ddip.frontend.service.rest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import de.werum.coprs.ddip.frontend.config.DdipProperties;

@CrossOrigin
@RestController
@RequestMapping(value = "/odata")
public class OdataRestController {

	private static final Logger LOGGER = LoggerFactory.getLogger(OdataRestController.class);

	private final RestTemplate restTemplate;

	private final DdipProperties ddipProperties;

	private final URL dispatchPripUrl;

	@Autowired
	public OdataRestController(final DdipProperties ddipProperties, final RestTemplate restTemplate) {
		this.ddipProperties = ddipProperties;
		this.dispatchPripUrl = buildDispatchPripUrl(ddipProperties);
		this.restTemplate = restTemplate;
	}

	static URL buildDispatchPripUrl(final DdipProperties ddipProperties) {
		try {
			return UriComponentsBuilder
					.fromHttpUrl(String.format("%s://%s:%d",
							Objects.requireNonNull(ddipProperties).getDispatchPripProtocol(),
							ddipProperties.getDispatchPripHost(),
							ddipProperties.getDispatchPripPort()))
					.build().toUri().toURL();
		} catch (final MalformedURLException e) {
			final String msg = String.format("could not initialize PRIP URL for request disptaching; protocol (%s), host (%s) and port (%s) must be valid: %s",
					ddipProperties.getDispatchPripProtocol(), ddipProperties.getDispatchPripHost(), ddipProperties.getDispatchPripPort(), e.getMessage());
			throw new IllegalArgumentException(msg, e);
		}
	}

	static HttpHeaders getHeaders(final HttpServletRequest request) {
		return Collections.list(request.getHeaderNames()).stream() //
				.collect(Collectors.toMap( //
						Function.identity(), //
						headerName -> Collections.list(request.getHeaders(headerName)), //
						(oldVal, newVal) -> newVal, //
						HttpHeaders::new //
						));
	}

	static void mapResponse(final ResponseEntity<String> responseEntity, final HttpServletResponse servletResponse) throws IOException {
		if (null != responseEntity) {
			for (final Map.Entry<String, List<String>> headers : responseEntity.getHeaders().entrySet()) {
				final String headerKey = headers.getKey();

				for (final String headerValue : headers.getValue()) {
					servletResponse.addHeader(headerKey, headerValue);
				}
			}

			servletResponse.setStatus(responseEntity.getStatusCodeValue());

			final String responseBody = responseEntity.getBody();
			if (null != responseBody) {
				LOGGER.debug(String.format("writing response body: %s", responseBody.length() > 256 ? responseBody.substring(0, 252) + "..." : responseBody));
				servletResponse.getWriter().write(responseBody);
			}
			servletResponse.flushBuffer();
		}
	}

	@RequestMapping(value = "/v1/**")
	public void handleOdataRequest(HttpServletRequest request, HttpServletResponse response) {
		final String queryParams = request.getQueryString() == null ? "" : "?" + request.getQueryString();
		LOGGER.info("Received HTTP request for URL: {}{}", request.getRequestURL().toString(), queryParams);

		final String queryUrl = String.format("%s%s%s", this.dispatchPripUrl, request.getRequestURI(), queryParams);
		LOGGER.info("Redirecting HTTP request to URL: {}", queryUrl);

		final HttpHeaders httpHeaders = OdataRestController.getHeaders(request);
		final HttpEntity<String> requestEntity = new HttpEntity<>(null, httpHeaders);

		final ResponseEntity<String> responseEntity = this.restTemplate.exchange(queryUrl, HttpMethod.resolve(request.getMethod()), requestEntity, String.class);

		try {
			OdataRestController.mapResponse(responseEntity, response);
		} catch (final IOException e) {
			throw new RuntimeException(String.format("error processing PRIP response: %s", e.getMessage()), e);
		}
	}

}
