package de.werum.csgrs.nativeapi.rest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.werum.csgrs.nativeapi.service.NativeApiService;

@CrossOrigin
@RestController
@RequestMapping("api/${native-api.major-version}")
public class NativeApiRestController {

	public static final Logger LOGGER = LogManager.getLogger(NativeApiRestController.class);

	public final NativeApiService nativeApiService;

	@Autowired
	public NativeApiRestController(final NativeApiService nativeApiService) {
		this.nativeApiService = nativeApiService;
	}

	@RequestMapping(method = RequestMethod.GET, path = "/ping", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> ping() {
		LOGGER.debug("Received ping request");
		final String version = this.nativeApiService.getNativeApiVersion();
		return ResponseEntity.ok("{\"apiVersion\":\"" + (null != version && !version.isEmpty() ? version : "UNKNOWN") + "\"}");
	}

	
	@RequestMapping(method = RequestMethod.GET, path = "/missions", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, List<String>> getMissions() {
		LOGGER.debug("request received: /missions");
		return Collections.singletonMap("missions", this.nativeApiService.getMissions());
	}

	@RequestMapping(method = RequestMethod.GET, path = "/missions/{missionName}/productTypes", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, List<String>> getProductTypes(@PathVariable final String missionName) {
		LOGGER.debug("request received: /missions/{}/productTypes", missionName);
		return Collections.singletonMap("productTypes", this.nativeApiService.getProductTypes(missionName));
	}

	@RequestMapping(method = RequestMethod.GET, path = "/missions/{missionName}/productTypes/{productType}/attributes",
			produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, List<String>> getAttributes(
			@PathVariable final String missionName,
			@PathVariable final String productType) {
		LOGGER.debug("request received: /missions/{}/productTypes/{}/attributes", missionName, productType);
		return Collections.singletonMap("attributes", this.nativeApiService.getAttributes(missionName, productType));
	}

}
