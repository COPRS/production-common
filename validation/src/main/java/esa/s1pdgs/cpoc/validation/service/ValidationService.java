package esa.s1pdgs.cpoc.validation.service;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {
	private static final Logger LOGGER = LogManager.getLogger(ValidationService.class);

	public void process(Date start, Date end) {
		LOGGER.info("Validating for inconsistancy between time interval from {} and {}", start, end );
		retrieveMetadata(start, end);
		retrieveObjectStorage();
	}

	private void retrieveMetadata(Date start, Date end) {

	}
	
	private void retrieveObjectStorage() {
		
	}
}
