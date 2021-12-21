package de.werum.coprs.nativeapi.rest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.werum.coprs.nativeapi.service.NativeApiStacService;
import de.werum.coprs.nativeapi.service.exception.NativeApiBadRequestException;

@CrossOrigin
@RestController
@RequestMapping("search")
public class NativeApiStacRestController {

	public static final Logger LOGGER = LogManager.getLogger(NativeApiStacRestController.class);

	public final NativeApiStacService nativeApiStacService;

	@Autowired
	public NativeApiStacRestController(final NativeApiStacService nativeApiStacService) {
		this.nativeApiStacService = nativeApiStacService;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<String> handleStacItemSearch(
			final HttpServletRequest request,
			@RequestParam(value = "datetime", required = false) final String datetime) {

		final String queryParams = request.getQueryString() == null ? "" : request.getQueryString();
		LOGGER.info("Received STAC item search request: /search?{}", queryParams);
		// TODO: issue a bad request response if other than datetime query params are used as they are not supported yet

		final List<String> result;
		// TODO: change response type using GeoJSON, see staccato-commons com.planet.staccato.model.GeoJson and com.planet.staccato.model.ItemCollection
		try {
			if (null != datetime) {
				final String decodedDatetimeStr = URLDecoder.decode(datetime, StandardCharsets.UTF_8.toString());
				result = this.nativeApiStacService.find(decodedDatetimeStr);
			}
		} catch (final NativeApiBadRequestException e) {
			throw new NativeApiRestControllerException(String.format("Bad request: %s", e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			throw new NativeApiRestControllerException(String.format("Internal server error: %s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return null == result ? Collections.emptyList() : result;
	}

}
