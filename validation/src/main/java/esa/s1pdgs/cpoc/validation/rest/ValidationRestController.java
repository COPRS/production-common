package esa.s1pdgs.cpoc.validation.rest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.validation.service.ValidationService;

@RestController
@RequestMapping("api/v1")
public class ValidationRestController {

	@Autowired
	private ValidationService validationService;
	
	private static final Logger LOGGER = LogManager.getLogger(ValidationRestController.class);

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{productFamily}/validate")
	public void validate(@PathVariable(name = "productFamily") String productFamily,
			@RequestParam(name = "intervalStart", defaultValue = "NONE") String intervalStart,
			@RequestParam(name = "intervalStop", defaultValue = "NONE") String intervalStop) {

		LOGGER.info("Received validation request for family '{}' within interval '{}' and '{}'", productFamily, intervalStart, intervalStop);
		
		LocalDateTime startTime = LocalDateTime.parse(intervalStart,
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"));
		
		LocalDateTime stopTime = LocalDateTime.parse(intervalStop,
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"));
		
		ProductFamily family = ProductFamily.valueOf(productFamily);
		LOGGER.debug("family={}, startTime={}, stopTime={}", family, startTime, stopTime);
		
		validationService.process(family, startTime, stopTime);
	}
}
