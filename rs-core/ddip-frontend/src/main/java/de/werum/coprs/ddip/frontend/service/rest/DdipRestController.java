package de.werum.coprs.ddip.frontend.service.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.werum.coprs.ddip.frontend.service.DdipService;
import de.werum.coprs.ddip.frontend.service.rest.model.PingResponse;

@CrossOrigin
@RestController
public class DdipRestController {

	public static final Logger LOGGER = LoggerFactory.getLogger(DdipRestController.class);

	public final DdipService ddipService;

	@Autowired
	public DdipRestController(final DdipService ddipService) {
		this.ddipService = ddipService;
	}

	@RequestMapping(method = RequestMethod.GET, path = "/app/ping", produces = MediaType.APPLICATION_JSON_VALUE)
	public PingResponse ping() {
		LOGGER.debug("Received ping request");
		final String version = this.ddipService.getDdipVersion();
		return new PingResponse(null != version && !version.isEmpty() ? version : "UNKNOWN");
	}

}
