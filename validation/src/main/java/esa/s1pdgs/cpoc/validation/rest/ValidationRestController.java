package esa.s1pdgs.cpoc.validation.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
public class ValidationRestController {

	private static final Logger LOGGER = LogManager.getLogger(ValidationRestController.class);
	
	public String hello() {
		LOGGER.info("hallo");		
		return "hallo";
	}
}
