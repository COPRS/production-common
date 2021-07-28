package de.werum.csgrs.nativeapi.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
public class NativeApiRestController {

	public static final Logger LOGGER = LogManager.getLogger(NativeApiRestController.class);

	@RequestMapping(method = RequestMethod.GET, path = "/ping", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> ping() {
		LOGGER.debug("Received ping request");
		return ResponseEntity.ok("{\"apiVersion\":\"v1\"}");
	}

}
