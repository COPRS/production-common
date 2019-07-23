package esa.s1pdgs.cpoc.validation.rest;

import java.time.format.DateTimeParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.validation.service.ValidationService;

@RestController
@RequestMapping("api/v1")
public class ValidationRestController {

	@Autowired
	private ValidationService validationService;

	static final Logger LOGGER = LogManager.getLogger(ValidationRestController.class);

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{productFamily}/validate")
	public void validate(@PathVariable(name = "productFamily") String productFamily,
			@RequestParam(name = "ingestionStart") String ingestionStart,
			@RequestParam(name = "ingestionStop") String ingestionStop) {

		LOGGER.info("Received validation request for family '{}' within interval '{}' and '{}'", productFamily,
				ingestionStart, ingestionStop);

		assertValidProductFamily("productFamily", productFamily);
		assertValidDateTime("ingestionStart", ingestionStart);
		assertValidDateTime("ingestionStop", ingestionStop);

		ProductFamily family = ProductFamily.valueOf(productFamily);
		LOGGER.debug("family={}, startTime={}, stopTime={}", family, ingestionStart, ingestionStop);

		try {
			validationService.checkConsistencyForFamilyAndTimeFrame(family, ingestionStart, ingestionStop);
		} catch (AbstractCodedException | SdkClientException e) {
			throw new RuntimeException(e);
		}
	}

	static final void assertValidProductFamily(String paramName, String productFamily) {

		if (productFamily == null) {
			throw new ValidationRestControllerException(String.format("%s provided is null", paramName),
					HttpStatus.BAD_REQUEST);
		}
		try {
			ProductFamily.valueOf(productFamily);
		} catch (IllegalArgumentException e) {
			throw new ValidationRestControllerException(
					String.format("invalid %s provided: %s", paramName, productFamily), HttpStatus.BAD_REQUEST);
		}
	}

	static final void assertValidDateTime(String paramName, String dateTime) {

		if (dateTime == null) {
			throw new ValidationRestControllerException(String.format("%s provided is null", paramName),
					HttpStatus.BAD_REQUEST);
		}
		try {
			DateUtils.METADATA_DATE_FORMATTER.parse(dateTime);
		} catch (DateTimeParseException e) {
			throw new ValidationRestControllerException(String.format("invalid %s provided: %s", paramName, dateTime),
					HttpStatus.BAD_REQUEST);
		}
	}
}
