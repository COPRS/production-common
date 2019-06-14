package esa.s1pdgs.cpoc.errorrepo.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.errorrepo.service.ErrorRepository;

@RestController
@RequestMapping(path = "/errors")
public class ErrorRepositoryController {

	private final ErrorRepository errorRepository;

	private static final String API_KEY = "errorRepositorySecretKey";

	public ErrorRepositoryController(@Autowired ErrorRepository errorRepository) {
		this.errorRepository = errorRepository;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/failedProcessings")
	public ResponseEntity<List<FailedProcessingDto>> getFailedProcessings(@RequestHeader("ApiKey") String apiKey) {

		if (!API_KEY.equals(apiKey)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} else {
			// TODO test if it can be null
		}

		List<FailedProcessingDto> failedProcessings = new ArrayList<>();

		try {
			failedProcessings = errorRepository.getFailedProcessings();
		} catch (RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<List<FailedProcessingDto>>(failedProcessings, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/failedProcessings/{id}")
	public ResponseEntity<FailedProcessingDto> getFailedProcessingsById(@RequestHeader("ApiKey") String apiKey,
			@PathVariable("id") String id) {

		if (!API_KEY.equals(apiKey)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		FailedProcessingDto failedProcessing = null;

		try {

			failedProcessing = errorRepository.getFailedProcessingsById(id);

			if (failedProcessing == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}

		} catch (RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<FailedProcessingDto>(failedProcessing, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/failedProcessings/{id}/restart")
	public ResponseEntity restartFailedProcessing(@RequestHeader("ApiKey") String apiKey,
			@PathVariable("id") String id) {

		if (!API_KEY.equals(apiKey)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		try {
			errorRepository.restartAndDeleteFailedProcessing(id);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(HttpStatus.OK);

	}

	@RequestMapping(method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/failedProcessings/{id}")
	public ResponseEntity deleteFailedProcessing(@RequestHeader("ApiKey") String apiKey,
			@PathVariable("id") String id) {

		if (!API_KEY.equals(apiKey)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		try {
			errorRepository.deleteFailedProcessing(id);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
