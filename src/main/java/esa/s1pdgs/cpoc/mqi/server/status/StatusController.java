package esa.s1pdgs.cpoc.mqi.server.status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.mqi.model.rest.StatusDto;
import esa.s1pdgs.cpoc.mqi.server.status.AppStatus.MqiServerStatus;

/**
 * @author Olivier Bex-Chauvet
 *
 */
@RestController
@RequestMapping(path = "/app")
public class StatusController {

	/**
	 * Logger
	 */
	protected static final Logger LOGGER = LogManager.getLogger(StatusController.class);

	/**
	 * Application status
	 */
	private final AppStatus appStatus;
	
	/**
	 * Constructor
	 * @param appStatus
	 */
	@Autowired
    public StatusController (final AppStatus appStatus) {
	    this.appStatus = appStatus;
	}

	/**
	 * Get application status
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/status")
	public ResponseEntity<StatusDto> getStatusRest() {
		MqiServerStatus currentStatus = appStatus.getStatus();
		long currentTimestamp = System.currentTimeMillis();
		long timeSinceLastChange = currentTimestamp - currentStatus.getDateLastChangeMs();
		StatusDto wrapperStatus = new StatusDto(currentStatus.getState(),
				timeSinceLastChange, currentStatus.getErrorCounter());
		if (currentStatus.isFatalError()) {
			return new ResponseEntity<StatusDto>(wrapperStatus, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<StatusDto>(wrapperStatus, HttpStatus.OK);
	}

	/**
	 * Stop application
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/stop")
	public ResponseEntity<String> postStop() {
		LOGGER.info("[MONITOR] MQI is scheduled to stop after the end of current process");
		appStatus.setStopping();
		return new ResponseEntity<String>("MQI is scheduled to stop after the end of current process",
				HttpStatus.OK);
	}

}
