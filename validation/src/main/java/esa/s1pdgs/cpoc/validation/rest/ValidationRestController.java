package esa.s1pdgs.cpoc.validation.rest;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.validation.config.ApplicationProperties;
import esa.s1pdgs.cpoc.validation.service.ValidationService;

@RestController
@RequestMapping("api/v1")
public class ValidationRestController {
	@Autowired
	private ApplicationProperties properties;
	
	@Autowired
	private ValidationService validationService;

	static final Logger LOGGER = LogManager.getLogger(ValidationRestController.class);

	@Async
	@RequestMapping(method = RequestMethod.POST, path = "/validate")
	public void validate() {	
		LocalDateTime startInterval = LocalDateTime.now().minusSeconds(properties.getIntervalOffset());
		LocalDateTime endInterval = LocalDateTime.now().minusSeconds(properties.getIntervalDelay());

		LOGGER.info("Received validation request within interval '{}' and '{}'", startInterval, endInterval);
		
		int discrepancy = validationService.checkConsistencyForInterval(startInterval, endInterval);
		LOGGER.info("Finished validation request and finding {} discrepancies", discrepancy);
	}
}
