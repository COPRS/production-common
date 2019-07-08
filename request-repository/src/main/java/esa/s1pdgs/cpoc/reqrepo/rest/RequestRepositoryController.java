package esa.s1pdgs.cpoc.reqrepo.rest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.appcatalog.common.FailedProcessing;
import esa.s1pdgs.cpoc.appcatalog.common.Processing;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.reqrepo.service.RequestRepository;

@RestController
@RequestMapping("api/v1")
public class RequestRepositoryController {	
	// TODO: put api_key in a crypted place
	public static final String API_KEY = "LdbEo2020tffcEGS";

	static final Logger LOGGER = LogManager.getLogger(RequestRepositoryController.class);

	private final RequestRepository requestRepository;
		
	@Autowired 
	public RequestRepositoryController(final RequestRepository requestRepository) {
		this.requestRepository = requestRepository;
	}
	
	/**
	 * Gets the list of failed processings ordered by creation time (ascending).
	 * 
	 * @param apiKey token agreed by server and client for authentication
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "failedProcessings")
	public List<FailedProcessing> getFailedProcessings(
			@RequestHeader(name="ApiKey", required=true) final String apiKey
	) {
		LOGGER.info("get the list of failed processings");
		assertValidApiKey(apiKey);
		return requestRepository.getFailedProcessings();
	}

	/**
	 * Gets the failed processing by Id
	 * 
	 * @param apiKey token agreed by server and client for authentication
	 * @param id     failed processing Id
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "failedProcessings/{id}")
	public FailedProcessing getFailedProcessingsById(
			@RequestHeader("ApiKey") final String apiKey,
			@PathVariable("id") final String id
	) {
		LOGGER.info("get the failed processing with id {}", id);
		assertValidApiKey(apiKey);
		final FailedProcessing failedProcessing = requestRepository.getFailedProcessingById(parseId(id));
		assertElementFound("failed request", failedProcessing, id);
		return failedProcessing;
	}
	
	/**
	 * Restarts the failed processing with Id
	 * 
	 * @param apiKey token agreed by server and client for authentication
	 * @param id     failed processing Id
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "failedProcessings/{id}/restart")
	public ModelApiResponse restartFailedProcessing(
			@RequestHeader("ApiKey") final String apiKey,
			@PathVariable("id") final String id
	) {
		LOGGER.info("restart the failed processing with id {}", id);
		assertValidApiKey(apiKey);				
		try {
			requestRepository.restartAndDeleteFailedProcessing(parseId(id));
		} catch (IllegalArgumentException e) {
			assertElementFound("failed processing", null, String.format("%s: %s", id, e));
		}
		return newModelApiResponse("restart");
	}
	
	/**
	 * Deletes the failed processing with Id
	 * 
	 * @param apiKey token agreed by server and client for authentication
	 * @param id     failed processing Id
	 * @return
	 */
	@RequestMapping(method = RequestMethod.DELETE,  path = "failedProcessings/{id}")
	public ModelApiResponse deleteFailedProcessing(
			@RequestHeader("ApiKey") final String apiKey,
			@PathVariable("id") final String id
	) {
		LOGGER.info("delete the failed processing with id {}", id);
		assertValidApiKey(apiKey);
		try {
			requestRepository.deleteFailedProcessing(parseId(id));
		} catch (IllegalArgumentException e) {
			assertElementFound("failed processing", null, id);
		}
		return newModelApiResponse("delete");
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "processingTypes")
	public List<String> getProcessingTypes(
			@RequestHeader("ApiKey") final String apiKey
	) {
		LOGGER.info("get the list of processing types");
		assertValidApiKey(apiKey);
		return requestRepository.getProcessingTypes();
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "processings/{id}")
	public Processing getProcessing(
			@RequestHeader("ApiKey") final String apiKey,
			@PathVariable("id") final String id
	) {
		LOGGER.info("get processing with id {}", id);
		assertValidApiKey(apiKey);		
		final Processing result = requestRepository.getProcessing(parseId(id));
		assertElementFound("processing", result, id);
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "processings")
	public List<Processing> getProcessings(
			@RequestHeader(value="ApiKey") final String apiKey,
			@RequestParam(value = "processingType", required = false) final List<String> processingType,
			@RequestParam(value = "processingStatus", required = false) final List<String> processingStatus,
			@RequestParam(value = "pageSize", required = false) final Integer pageSize,
			@RequestParam(value = "pageNumber", required = false, defaultValue = "0") final Integer pageNumber
	) {		
		LOGGER.info("get the list of processings");
		assertValidApiKey(apiKey);		
		return requestRepository.getProcessings(pageSize, pageNumber, processingType, toMessageStates(processingStatus));
    }
	
	static final List<MessageState> toMessageStates(final List<String> processingStatus)
	{
		if (processingStatus == null)
		{
			return Collections.emptyList();
		}
		
		try {
			return processingStatus.stream()
					.map(s -> MessageState.valueOf(s))
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new RequestRepositoryControllerException(
					String.format(
							"invalid message states provided: %s (allowed are: %s)", 
							processingStatus, 
							Arrays.toString(MessageState.values())
					), 
					HttpStatus.BAD_REQUEST
			);
		}
	}
	
	static final ModelApiResponse newModelApiResponse(final String action)
	{
		final ModelApiResponse response = new ModelApiResponse();
		response.setCode(UUID.randomUUID());
		response.setType(action);
		response.setMessage(action + " success");
		return response;
	}
	
	static final void assertValidApiKey(final String apiKey) throws RequestRepositoryControllerException {
		if (!API_KEY.equals(apiKey)) {
			throw new RequestRepositoryControllerException(
					"invalid API key supplied",
					HttpStatus.FORBIDDEN
			);
		}
	}
	
	static final long parseId(final String idString) throws RequestRepositoryControllerException {
		try {
			return Long.parseLong(idString);
		} catch (NumberFormatException e) {
			throw new RequestRepositoryControllerException(
					String.format(
							"invalid id error while getting the failed processings with id %s: %s", 
							idString, 
							e
					), 
					HttpStatus.BAD_REQUEST
			);
		}
	}
	
	static final void assertElementFound(String name, Object element, String id) {
		if (element == null) {
			throw new RequestRepositoryControllerException(
					String.format("%s not found, id %s", name, id), 
					HttpStatus.NOT_FOUND
			);
		}		
	}
	
}
