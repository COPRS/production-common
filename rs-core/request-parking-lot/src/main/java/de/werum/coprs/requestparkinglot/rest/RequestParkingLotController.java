package de.werum.coprs.requestparkinglot.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
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
import org.springframework.web.bind.annotation.RestController;

import de.werum.coprs.requestparkinglot.config.ApiConfiguration;
import de.werum.coprs.requestparkinglot.rest.model.IdListDto;
import de.werum.coprs.requestparkinglot.service.AllowedActionNotAvailableException;
import de.werum.coprs.requestparkinglot.service.RequestParkingLot;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;

@RestController
@RequestMapping("api/v1")
public class RequestParkingLotController {	
	
	public static final Pattern MONGODB_ID_PATTERN = Pattern.compile("[0-9a-f]{24}");

	static final Logger LOGGER = LogManager.getLogger(RequestParkingLotController.class);

	private final RequestParkingLot requestParkingLot;
		
	private final String apiKey;

	@Autowired 
	public RequestParkingLotController(final RequestParkingLot requestParkingLot,
			final ApiConfiguration apiConfiguration) {
		this.requestParkingLot = requestParkingLot;
		apiKey = apiConfiguration.getKey();
		LOGGER.debug("API key: {}", apiKey);
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
		return requestParkingLot.getFailedProcessings();
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "failedProcessings/count")
	public Long getFailedProcessingsCount(
			@RequestHeader(name="ApiKey", required=true) final String apiKey
	) {
		LOGGER.debug("get failed processings count");
		assertValidApiKey(apiKey);
		return requestParkingLot.getFailedProcessingsCount();
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
		assertValidId(id);
		final FailedProcessing failedProcessing = requestParkingLot.getFailedProcessingById(id);
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
		assertValidId(id);
		try {
			requestParkingLot.restartAndDeleteFailedProcessing(id);
		} catch (final AllowedActionNotAvailableException e1) {
			throw new RequestParkingLotControllerException(
					String.format("Action RESTART not allowed for id %s", id),
					HttpStatus.NOT_FOUND
			);	
		} catch (final IllegalArgumentException e2) {
			assertElementFound("failed processing", null, String.format("%s: %s", id, e2));
		}
		return new ApiResponse("FailedProcessing", "restart", Collections.singletonList(id), Collections.emptyList());
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
		assertValidId(id);
		try {
			requestParkingLot.resubmitAndDeleteFailedProcessing(id);
		} catch (final AllowedActionNotAvailableException e1) {
			throw new RequestParkingLotControllerException(
					String.format("Action RESUBMIT not allowed for id %s", id),
					HttpStatus.NOT_FOUND
			);
		} catch (final IllegalArgumentException e) {
			assertElementFound("failed processing", null, String.format("%s: %s", id, e));
		}
		return new ApiResponse("FailedProcessing", "resubmit", Collections.singletonList(id), Collections.emptyList());
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
		assertValidId(id);
		try {
			requestParkingLot.deleteFailedProcessing(id);
		} catch (final IllegalArgumentException e) {
			assertElementFound("failed processing", null, id);
		}
		return new ApiResponse("FailedProcessing", "delete", Collections.singletonList(id), Collections.emptyList());
	}
	
	@RequestMapping(method = RequestMethod.POST,  path = "failedProcessings/delete")
	public ApiResponse deleteFailedProcessings(
			@RequestHeader("ApiKey") final String apiKey,
			@RequestBody final IdListDto ids
	) {
		LOGGER.debug("delete the failed processings with id {}", ids);
		assertValidApiKey(apiKey);
		
		final List<String> success = new ArrayList<>();
		final List<String> failed = new ArrayList<>();
		
		for (final String id : ids.getIds()) {
			assertValidId(id);
			try {
				requestParkingLot.deleteFailedProcessing(id);
				success.add(id);
			} catch (final IllegalArgumentException e) {
				failed.add(id);
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
		
		final List<String> success = new ArrayList<>();
		final List<String> failed = new ArrayList<>();
		
		for (final String id : ids.getIds()) {
			assertValidId(id);
			try {
				requestParkingLot.restartAndDeleteFailedProcessing(id);
				success.add(id);
			} catch (final IllegalArgumentException | AllowedActionNotAvailableException e) {
				failed.add(id);
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
		
		final List<String> success = new ArrayList<>();
		final List<String> failed = new ArrayList<>();
		
		for (final String id : ids.getIds()) {
			assertValidId(id);
			try {
				requestParkingLot.resubmitAndDeleteFailedProcessing(id);
				success.add(id);
			} catch (final IllegalArgumentException | AllowedActionNotAvailableException e) {
				failed.add(id);
			}
		}
		return new ApiResponse("FailedProcessing", "resubmit", success, failed);
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
			throw new RequestParkingLotControllerException(
					String.format(
							"invalid message states provided: %s (allowed are: %s)", 
							processingStatus, 
							Arrays.toString(MessageState.values())
					), 
					HttpStatus.BAD_REQUEST
			);
		}
	}

	final void assertValidApiKey(final String apiKey) throws RequestParkingLotControllerException {
		if (!this.apiKey.equals(apiKey)) {
			throw new RequestParkingLotControllerException(
					"invalid API key supplied",
					HttpStatus.FORBIDDEN
			);
		}
	}
	
	static final void assertValidId(final String idString) throws RequestParkingLotControllerException {
		if (!MONGODB_ID_PATTERN.matcher(idString).matches()) {
			throw new RequestParkingLotControllerException(
					String.format(
							"invalid id error while getting the failed processings with id %s", 
							idString 
							), 
					HttpStatus.BAD_REQUEST
					);
		}
	}
	
	static final void assertElementFound(final String name, final Object element, final String id) {
		if (element == null) {
			throw new RequestParkingLotControllerException(
					String.format("%s not found, id %s", name, id), 
					HttpStatus.NOT_FOUND
			);
		}		
	}
	
}
