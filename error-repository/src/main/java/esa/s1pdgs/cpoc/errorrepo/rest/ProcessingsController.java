package esa.s1pdgs.cpoc.errorrepo.rest;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.errorrepo.model.rest.ProcessingDto;
import esa.s1pdgs.cpoc.errorrepo.service.ProcessingsRepository;

@RestController
public class ProcessingsController {	
	
	private static final Logger LOGGER = LogManager.getLogger(ProcessingsRepository.class);

	// TODO: get api_key from configuration
	private static final String API_KEY = "processingRepositorySecretKey";
	
	private final ProcessingsRepository processingRepository;
	
	@Autowired 
	public ProcessingsController(final ProcessingsRepository processingRepository) {
		this.processingRepository = processingRepository;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/processingTypes")
	public ResponseEntity<List<String>> getProcessingTypes(@RequestHeader("ApiKey") String apiKey) {
		LOGGER.info("get the list of processing types");

		if (!API_KEY.equals(apiKey)) {
			LOGGER.warn("invalid API key supplied");
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		try {
			return new ResponseEntity<List<String>>(processingRepository.getProcessingTypes(), HttpStatus.OK);
		} catch (RuntimeException e) {
			LOGGER.error("error while getting the list of processing types", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}	
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/processings")
	public ResponseEntity<List<ProcessingDto>> getProcessings(@RequestHeader("ApiKey") String apiKey) {
		LOGGER.info("get the list of processings");

		if (!API_KEY.equals(apiKey)) {
			LOGGER.warn("invalid API key supplied");
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		try {
			return new ResponseEntity<List<ProcessingDto>>(processingRepository.getProcessings(), HttpStatus.OK);
		} catch (RuntimeException e) {
			LOGGER.error("error while getting the list of processings", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}	
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/processings/{id}")
	public ResponseEntity<ProcessingDto> getProcessing(
			@RequestHeader("ApiKey") final String apiKey,
			@PathVariable("id") final String id
	) {
		LOGGER.info("get processing with id {}", id);

		if (!API_KEY.equals(apiKey)) {
			LOGGER.warn("invalid API key supplied");
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		try {
			final ProcessingDto result = processingRepository.getProcessing(Long.parseLong(id));
			if (result == null) {
				LOGGER.warn("processing not found, id {}", id);
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}			
			return new ResponseEntity<ProcessingDto>(result, HttpStatus.OK);
		} catch (RuntimeException e) {
			LOGGER.error("error while getting the list of processings", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}	
	}
}
