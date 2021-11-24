package de.werum.coprs.ddip.frontend.service.rest;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import de.werum.coprs.ddip.frontend.config.DdipProperties;

@CrossOrigin
@RestController
@RequestMapping(value = "/odata")
public class OdataRestController {

	private static final Logger LOGGER = LoggerFactory.getLogger(OdataRestController.class);

	private final DdipProperties ddipProperties;

	private final URL dispatchPripUrl;

	@Autowired
	public OdataRestController(final DdipProperties ddipProperties) {
		this.ddipProperties = ddipProperties;
		this.dispatchPripUrl = buildDispatchPripUrl(ddipProperties);
	}

	private static URL buildDispatchPripUrl(final DdipProperties ddipProperties) {
		try {
			return UriComponentsBuilder
					.fromHttpUrl(String.format("%s://%s:%d",
							ddipProperties.getDispatchPripProtocol(),
							ddipProperties.getDispatchPripHost(),
							ddipProperties.getDispatchPripPort()))
					.build().toUri().toURL();
		} catch (final MalformedURLException e) {
			final String msg = String.format("could not initialize PRIP URL for request disptaching; protocol (%s), host (%s) and port (%s) must be valid: %s",
					ddipProperties.getDispatchPripProtocol(), ddipProperties.getDispatchPripHost(), ddipProperties.getDispatchPripPort(), e.getMessage());
			throw new IllegalArgumentException(msg, e);
		}
	}

	@RequestMapping(value = "/v1/**")
	public void processOdataRequest(HttpServletRequest request, HttpServletResponse response) {
		final String queryParams = request.getQueryString() == null ? "" : "?" + request.getQueryString();
		LOGGER.info("Received HTTP request for URL: {}{}", request.getRequestURL().toString(), queryParams);

		final String queryUrl = String.format("%s%s%s", this.dispatchPripUrl, request.getRequestURI(), queryParams);
		LOGGER.info("Redirecting HTTP request to URL: {}", queryUrl);
		response.setHeader(HttpHeaders.LOCATION, queryUrl);
		response.setStatus(HttpStatus.FOUND.value());
	}

}
