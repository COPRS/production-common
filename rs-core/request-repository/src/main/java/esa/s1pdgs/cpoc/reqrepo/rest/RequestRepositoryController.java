package esa.s1pdgs.cpoc.reqrepo.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.appcatalog.common.FailedProcessing;
import esa.s1pdgs.cpoc.appcatalog.common.Processing;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.reqrepo.rest.model.IdListDto;
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
		LOGGER.debug("get the list of failed processings");
		assertValidApiKey(apiKey);
		return requestRepository.getFailedProcessings();
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "failedProcessings/count")
	public Long getFailedProcessingsCount(
			@RequestHeader(name="ApiKey", required=true) final String apiKey
	) {
		LOGGER.debug("get failed processings count");
		assertValidApiKey(apiKey);
		return requestRepository.getFailedProcessingsCount();
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
		LOGGER.debug("get the failed processing with id {}", id);
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
	public ApiResponse restartFailedProcessing(
			@RequestHeader("ApiKey") final String apiKey,
			@PathVariable("id") final String id
	) {
		LOGGER.debug("restart the failed processing with id {}", id);
		assertValidApiKey(apiKey);				
		final long idInt = parseId(id);
		try {
			requestRepository.restartAndDeleteFailedProcessing(idInt);
		} catch (final IllegalArgumentException e) {
			assertElementFound("failed processing", null, String.format("%s: %s", id, e));
		}
		return new ApiResponse("FailedProcessing", "restart", Collections.singletonList(idInt), Collections.emptyList());
	}
	
	/**
	 * Resubmits the failed processing with Id
	 * 
	 * @param apiKey token agreed by server and client for authentication
	 * @param id     failed processing Id
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "failedProcessings/{id}/resubmit")
	public ApiResponse resubmitFailedProcessing(
			@RequestHeader("ApiKey") final String apiKey,
			@PathVariable("id") final String id
	) {
		LOGGER.debug("resubmit the failed processing with id {}", id);
		assertValidApiKey(apiKey);				
		final long idInt = parseId(id);
		try {
			requestRepository.restartAndDeleteFailedProcessing(idInt);
		} catch (final IllegalArgumentException e) {
			assertElementFound("failed processing", null, String.format("%s: %s", id, e));
		}
		return new ApiResponse("FailedProcessing", "resubmit", Collections.singletonList(idInt), Collections.emptyList());
	}
	
	/**
	 * Reevaluates the failed processing with Id
	 * 
	 * @param apiKey token agreed by server and client for authentication
	 * @param id     failed processing Id
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "failedProcessings/{id}/reevaluate")
	public ApiResponse reevaluateFailedProcessing(
			@RequestHeader("ApiKey") final String apiKey,
			@PathVariable("id") final String id
	) {
		LOGGER.debug("reevaluate the failed processing with id {}", id);
		assertValidApiKey(apiKey);				
		final long idInt = parseId(id);
		try {
			requestRepository.reevaluateAndDeleteFailedProcessing(idInt);
		} catch (final IllegalArgumentException e) {
			assertElementFound("failed processing", null, String.format("%s: %s", id, e));
		}
		return new ApiResponse("FailedProcessing", "reevaluate", Collections.singletonList(idInt), Collections.emptyList());
	}
	
	/**
	 * Deletes the failed processing with Id
	 * 
	 * @param apiKey token agreed by server and client for authentication
	 * @param id     failed processing Id
	 * @return
	 */
	@RequestMapping(method = RequestMethod.DELETE,  path = "failedProcessings/{id}")
	public ApiResponse deleteFailedProcessing(
			@RequestHeader("ApiKey") final String apiKey,
			@PathVariable("id") final String id
	) {
		LOGGER.debug("delete the failed processing with id {}", id);
		assertValidApiKey(apiKey);
		final long idInt = parseId(id);
		try {
			requestRepository.deleteFailedProcessing(idInt);
		} catch (final IllegalArgumentException e) {
			assertElementFound("failed processing", null, id);
		}
		return new ApiResponse("FailedProcessing", "delete", Collections.singletonList(idInt), Collections.emptyList());
	}
	
	@RequestMapping(method = RequestMethod.POST,  path = "failedProcessings/delete")
	public ApiResponse deleteFailedProcessings(
			@RequestHeader("ApiKey") final String apiKey,
			@RequestBody final IdListDto ids
	) {
		LOGGER.debug("delete the failed processings with id {}", ids);
		assertValidApiKey(apiKey);
		
		final List<Long> success = new ArrayList<>();
		final List<Long> failed = new ArrayList<>();
		
		for (final String id : ids.getIds()) {
			final long idInt = parseId(id);
			try {
				requestRepository.deleteFailedProcessing(idInt);
				success.add(idInt);
			} catch (final IllegalArgumentException e) {
				failed.add(idInt);
			}
		}
		return new ApiResponse("FailedProcessing", "delete", success, failed);
	}
	
	@RequestMapping(method = RequestMethod.POST,  path = "failedProcessings/restart")
	public ApiResponse restartFailedProcessings(
			@RequestHeader("ApiKey") final String apiKey,
			@RequestBody final IdListDto ids
	) {
		LOGGER.debug("restart the failed processings with id {}", ids);
		assertValidApiKey(apiKey);
		
		final List<Long> success = new ArrayList<>();
		final List<Long> failed = new ArrayList<>();
		
		for (final String id : ids.getIds()) {
			final long idInt = parseId(id);
			try {
				requestRepository.restartAndDeleteFailedProcessing(idInt);
				success.add(idInt);
			} catch (final IllegalArgumentException e) {
				failed.add(idInt);
			}
		}
		return new ApiResponse("FailedProcessing", "restart", success, failed);
	}
	
	@RequestMapping(method = RequestMethod.POST,  path = "failedProcessings/resubmit")
	public ApiResponse resubmitFailedProcessings(
			@RequestHeader("ApiKey") final String apiKey,
			@RequestBody final IdListDto ids
	) {
		LOGGER.debug("resubmit the failed processings with id {}", ids);
		assertValidApiKey(apiKey);
		
		final List<Long> success = new ArrayList<>();
		final List<Long> failed = new ArrayList<>();
		
		for (final String id : ids.getIds()) {
			final long idInt = parseId(id);
			try {
				requestRepository.restartAndDeleteFailedProcessing(idInt);
				success.add(idInt);
			} catch (final IllegalArgumentException e) {
				failed.add(idInt);
			}
		}
		return new ApiResponse("FailedProcessing", "resubmit", success, failed);
	}
	
	@RequestMapping(method = RequestMethod.POST,  path = "failedProcessings/reevaluate")
	public ApiResponse reevaluateFailedProcessings(
			@RequestHeader("ApiKey") final String apiKey,
			@RequestBody final IdListDto ids
	) {
		LOGGER.debug("reevaluate the failed processings with id {}", ids);
		assertValidApiKey(apiKey);
		
		final List<Long> success = new ArrayList<>();
		final List<Long> failed = new ArrayList<>();
		
		for (final String id : ids.getIds()) {
			final long idInt = parseId(id);
			try {
				requestRepository.reevaluateAndDeleteFailedProcessing(idInt);
				success.add(idInt);
			} catch (final IllegalArgumentException e) {
				failed.add(idInt);
			}
		}
		return new ApiResponse("FailedProcessing", "reevaluate", success, failed);
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "processingTypes")
	public List<String> getProcessingTypes(
			@RequestHeader("ApiKey") final String apiKey
	) {
		LOGGER.debug("get the list of processing types");
		assertValidApiKey(apiKey);
		return requestRepository.getProcessingTypes();
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "processings/{id}")
	public Processing getProcessing(
			@RequestHeader("ApiKey") final String apiKey,
			@PathVariable("id") final String id
	) {
		LOGGER.debug("get processing with id {}", id);
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
		LOGGER.debug("get the list of processings");
		assertValidApiKey(apiKey);		
		return requestRepository.getProcessings(pageSize, pageNumber, processingType, toMessageStates(processingStatus));
    }
	
	@RequestMapping(method = RequestMethod.GET, path = "processings/count")
	public Long getProcessingsCount(
			@RequestHeader(value="ApiKey") final String apiKey,
			@RequestParam(value = "processingType", required = false) final List<String> processingType,
			@RequestParam(value = "processingStatus", required = false) final List<String> processingStatus
	) {		
		LOGGER.debug("get processings count");
		assertValidApiKey(apiKey);		
		return requestRepository.getProcessingsCount(processingType, toMessageStates(processingStatus));
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
		} catch (final Exception e) {
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
		} catch (final NumberFormatException e) {
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
	
	static final void assertElementFound(final String name, final Object element, final String id) {
		if (element == null) {
			throw new RequestRepositoryControllerException(
					String.format("%s not found, id %s", name, id), 
					HttpStatus.NOT_FOUND
			);
		}		
	}
	
}
