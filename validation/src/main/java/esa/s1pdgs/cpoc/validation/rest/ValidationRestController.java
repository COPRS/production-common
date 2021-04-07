package esa.s1pdgs.cpoc.validation.rest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.validation.service.DataLifecycleSyncService;
import esa.s1pdgs.cpoc.validation.service.ValidationService;

@RestController
@RequestMapping("api/v1")
public class ValidationRestController {
	@Autowired
	private ValidationService validationService;

	@Autowired
	private DataLifecycleSyncService syncService;

	static final Logger LOGGER = LogManager.getLogger(ValidationRestController.class);

	@Async
	@RequestMapping(method = RequestMethod.POST, path = "/validate")
	public void validate() {
		LOGGER.info("Received validation request");
		validationService.checkConsistencyForInterval();
	}

	@Async
	@RequestMapping(method = RequestMethod.POST, path = "/syncOBSwithDataLifecycleIndex")
	public void syncOBSwithDataLifecycleIndex(
			@RequestParam(value = "startDate", required = true) final String startDate,
			@RequestParam(value = "endDate", required = true) final String endDate) {
		
		LOGGER.info("Received sync request for synchronisation of OBS with Datalifecycle Index");		

		assertValidDateTimeString("startDate", startDate, false);
		assertValidDateTimeString("endDate", endDate, false);
		
		LocalDateTime lStart = convertDateTime(startDate);
		LocalDateTime lEnd = convertDateTime(endDate);
		
		if (lStart.isAfter(lEnd)) {
			throw new ValidationRestControllerException(
					String.format("startDate %s is after endDate %s", startDate, endDate),
					HttpStatus.BAD_REQUEST);
		}
		syncService.syncOBSwithDataLifecycleIndex(
				Date.from(lStart.atZone(ZoneId.systemDefault()).toInstant()),
				Date.from(lEnd.atZone(ZoneId.systemDefault()).toInstant()));

	}
	
	@Async
	@RequestMapping(method = RequestMethod.POST, path = "/syncDataLifecycleIndexWithOBS")
	public void syncDataLifecycleIndexWithOBS (
			@RequestParam(value = "startDate", required = true) final String startDate,
			@RequestParam(value = "endDate", required = true) final String endDate) {
		
		LOGGER.info("Received sync request for synchronisation of Datalifecycle Index with OBS");		

		assertValidDateTimeString("startDate", startDate, false);
		assertValidDateTimeString("endDate", endDate, false);
		
		LocalDateTime lStart = convertDateTime(startDate);
		LocalDateTime lEnd = convertDateTime(endDate);
		
		if (lStart.isAfter(lEnd)) {
			throw new ValidationRestControllerException(
					String.format("startDate %s is after endDate %s", startDate, endDate),
					HttpStatus.BAD_REQUEST);
		}
		//TODO:		syncService.syncDataLifecycleIndexWithOBS()

	}

	private void assertValidDateTimeString(final String attributeName, final String dateTimeAsString,
			boolean optional) {

		if (optional && (StringUtil.isEmpty(dateTimeAsString) || "null".equalsIgnoreCase(dateTimeAsString))) {
			return;
		}

		try {
			convertDateTime(dateTimeAsString);
		} catch (NumberFormatException e) {
			throw new ValidationRestControllerException(
					String.format("invalid dateTimeString on attribute %s: value: %s: %s", attributeName,
							dateTimeAsString, e),
					HttpStatus.BAD_REQUEST);
		}
	}

	private LocalDateTime convertDateTime(String dateTimeAsString) {
		return (null != dateTimeAsString) ? DateUtils.parse(dateTimeAsString) : null;
	}

}
