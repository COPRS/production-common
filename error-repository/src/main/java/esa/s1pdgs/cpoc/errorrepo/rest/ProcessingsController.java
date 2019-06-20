package esa.s1pdgs.cpoc.errorrepo.rest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.errorrepo.model.rest.ProcessingDto;
import esa.s1pdgs.cpoc.errorrepo.service.ProcessingsRepository;

@RestController
public class ProcessingsController {	
	
	private static final Logger LOGGER = LogManager.getLogger(ProcessingsRepository.class);

	// TODO: get api_key from configuration
	private static final String API_KEY = "LdbEo2020tffcEGS";
	
	private final ProcessingsRepository processingRepository;
	
	@Autowired 
	public ProcessingsController(final ProcessingsRepository processingRepository) {
		this.processingRepository = processingRepository;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/api/v1/processingTypes")
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
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/api/v1/processings/{id}")
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
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/api/v1/processings")
	public ResponseEntity<List<ProcessingDto>> getProcessings(
			@RequestHeader(value="ApiKey") String apiKey,
			@RequestParam(value = "processingType", required = false) List<String> processingType,
			@RequestParam(value = "processingStatus", required = false) List<String> processingStatus,
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "pageNumber", required = false) Integer pageNumber) {
		
		LOGGER.info("get the list of processings");

		if (!API_KEY.equals(apiKey)) {
			LOGGER.warn("invalid API key supplied");
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		try {
			final List<ProcessingDto> result = processingRepository.getProcessings().stream()
					.filter(p -> (processingType==null || processingType.isEmpty() || processingType.contains(p.getProcessingType())))
					.filter(p -> (processingStatus==null || processingStatus.isEmpty() || processingStatus.contains(p.getState().toString())))
					.collect(Collectors.toList());
			
			if (pageSize == null || pageNumber==null)
			{
				return new ResponseEntity<List<ProcessingDto>>(result, HttpStatus.OK);				
			}	
			
			final int startIndex = pageNumber*pageSize;
			final int endIndex = (pageNumber+1)*pageSize;
			
			if (startIndex < 0 || endIndex < 0)
			{
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}			
			if (startIndex >= result.size() || endIndex > result.size())
			{
				return new ResponseEntity<List<ProcessingDto>>(Collections.emptyList(), HttpStatus.OK);				
			}			
			return new ResponseEntity<List<ProcessingDto>>(result.subList(startIndex, endIndex), HttpStatus.OK);
			
		} catch (RuntimeException e) {
			LOGGER.error("error while getting the list of processings", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}	

    }
}
