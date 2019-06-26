package esa.s1pdgs.cpoc.errorrepo.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.errorrepo.service.ErrorRepository;

/**
 * Provides information about failed processings and the ability to restart and
 * delete failed processings.
 * 
 * @author birol_colak@net.werum
 *
 */
@RestController
public class ErrorRepositoryController {

	// TODO: put api_key in a crypted place
	public static final String API_KEY = "LdbEo2020tffcEGS";

	private static final Logger LOGGER = LogManager.getLogger(ErrorRepositoryController.class);

	private final ErrorRepository errorRepository;

	public ErrorRepositoryController(@Autowired ErrorRepository errorRepository) {
		this.errorRepository = errorRepository;
	}

	/**
	 * Gets the list of failed processings ordered by creation time (ascending).
	 * 
	 * @param apiKey token agreed by server and client for authentication
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/api/v1/failedProcessings")
	public ResponseEntity<List<FailedProcessingDto>> getFailedProcessings(@RequestHeader("ApiKey") String apiKey) {

		LOGGER.info("get the list of failed processings");

		if (!API_KEY.equals(apiKey)) {
			LOGGER.warn("invalid API key supplied");
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} else {
			// TODO test if it can be null
		}

		List<FailedProcessingDto> failedProcessings = new ArrayList<>();

		try {
			failedProcessings = errorRepository.getFailedProcessings();
		} catch (RuntimeException e) {
			LOGGER.error("error while getting the list of failed processings", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (failedProcessings.size() > 1) {
			Collections.sort(failedProcessings, FailedProcessingDto.ASCENDING_CREATION_TIME_COMPERATOR);
		}

		return new ResponseEntity<List<FailedProcessingDto>>(failedProcessings, HttpStatus.OK);
	}

	/**
	 * Gets the failed processing by Id
	 * 
	 * @param apiKey token agreed by server and client for authentication
	 * @param id     failed processing Id
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/api/v1/failedProcessings/{id}")
	public ResponseEntity<FailedProcessingDto> getFailedProcessingsById(@RequestHeader("ApiKey") String apiKey,
			@PathVariable("id") String id) {

		LOGGER.info("get the failed processing with id {}", id);

		if (!API_KEY.equals(apiKey)) {
			LOGGER.warn("invalid API key supplied");
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		FailedProcessingDto failedProcessing = null;

		try {

			failedProcessing = errorRepository.getFailedProcessingById(Long.parseLong(id));

			if (failedProcessing == null) {
				LOGGER.warn("failed processing not found, id {}", id);
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} catch (NumberFormatException e) {
			LOGGER.error("invalid id error while getting the failed processings with id {}:{}", id, e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (RuntimeException e) {
			LOGGER.error("error while getting the failed processings with id {}:{}", id, e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<FailedProcessingDto>(failedProcessing, HttpStatus.OK);
	}

	/**
	 * Restarts the failed processing with Id
	 * 
	 * @param apiKey token agreed by server and client for authentication
	 * @param id     failed processing Id
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/api/v1/failedProcessings/{id}/restart")
	public ResponseEntity<ModelApiResponse> restartFailedProcessing(@RequestHeader("ApiKey") String apiKey,
			@PathVariable("id") String id) {

		LOGGER.info("restart the failed processing with id {}", id);

		if (!API_KEY.equals(apiKey)) {
			LOGGER.warn("invalid API key supplied");
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		try {
			errorRepository.restartAndDeleteFailedProcessing(Long.parseLong(id));
		} catch (NumberFormatException e) {
			LOGGER.error("invalid id error while getting the failed processings with id {}:{}", id, e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (IllegalArgumentException e) {
			LOGGER.warn("failed processing not found, id {}: {}", id, e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);

		} catch (RuntimeException e) {
			LOGGER.error("error while restarting the failed processings with id {}:{}", id, e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		final ModelApiResponse response = new ModelApiResponse();
		response.setCode(UUID.randomUUID());
		response.setType("restart");
		response.setMessage("restart success");

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Deletes the failed processing with Id
	 * 
	 * @param apiKey token agreed by server and client for authentication
	 * @param id     failed processing Id
	 * @return
	 */
	@RequestMapping(method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/api/v1/failedProcessings/{id}")
	public ResponseEntity<ModelApiResponse> deleteFailedProcessing(@RequestHeader("ApiKey") String apiKey,
			@PathVariable("id") String id) {

		LOGGER.info("delete the failed processing with id {}", id);

		if (!API_KEY.equals(apiKey)) {
			LOGGER.warn("invalid API key supplied");
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		try {
			errorRepository.deleteFailedProcessing(Long.parseLong(id));
		} catch (NumberFormatException e) {
			LOGGER.error("invalid id error while getting the failed processings with id {}:{}", id, e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (IllegalArgumentException e) {
			LOGGER.warn("failed processing not found, id {}", id);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (RuntimeException e) {
			LOGGER.error("error while deleting the failed processings with id {}:{}", id, e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		final ModelApiResponse response = new ModelApiResponse();
		response.setCode(UUID.randomUUID());
		response.setType("delete");
		response.setMessage("delete success");

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
