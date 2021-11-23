package de.werum.coprs.ddip.frontend.service.rest;

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

import de.werum.coprs.ddip.frontend.config.DdipProperties;

@CrossOrigin
@RestController
@RequestMapping(value = "/odata")
public class OdataRestController {

	private static final Logger LOGGER = LoggerFactory.getLogger(OdataRestController.class);

	private final DdipProperties ddipProperties;

	@Autowired
	public OdataRestController(final DdipProperties ddipProperties) {
		this.ddipProperties = ddipProperties;
	}

	@RequestMapping(value = "/v1/**")
	public void processOdataRequest(HttpServletRequest request, HttpServletResponse response) {
		final String queryParams = request.getQueryString() == null ? "" : "?" + request.getQueryString();
		LOGGER.info("Received HTTP request for URL: {}{}", request.getRequestURL().toString(), queryParams);

		final URL dispatchPripUrl = this.ddipProperties.getDispatchPripUrl();
		final String queryUrl = String.format("%s%s", dispatchPripUrl, queryParams);
		LOGGER.info("Redirecting HTTP request to URL: {}", queryUrl);
		response.setHeader(HttpHeaders.LOCATION, queryUrl);
		response.setStatus(HttpStatus.FOUND.value());
	}

}
