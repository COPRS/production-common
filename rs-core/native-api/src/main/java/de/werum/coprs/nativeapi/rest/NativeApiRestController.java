package de.werum.coprs.nativeapi.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.werum.coprs.nativeapi.rest.model.PingResponse;
import de.werum.coprs.nativeapi.service.NativeApiService;

@CrossOrigin
@RestController
@RequestMapping("api")
public class NativeApiRestController {

	public static final Logger LOGGER = LogManager.getLogger(NativeApiRestController.class);

	public final NativeApiService nativeApiService;

	@Autowired
	public NativeApiRestController(final NativeApiService nativeApiService) {
		this.nativeApiService = nativeApiService;
	}

	@RequestMapping(method = RequestMethod.GET, path = "/ping", produces = MediaType.APPLICATION_JSON_VALUE)
	public PingResponse ping() {
		LOGGER.debug("Received ping request");
		final String version = this.nativeApiService.getNativeApiVersion();
		return new PingResponse(null != version && !version.isEmpty() ? version : "UNKNOWN");
	}

}
