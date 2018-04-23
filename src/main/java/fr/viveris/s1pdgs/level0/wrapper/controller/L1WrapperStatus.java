package fr.viveris.s1pdgs.level0.wrapper.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.viveris.s1pdgs.level0.wrapper.AppStatus;
import fr.viveris.s1pdgs.level0.wrapper.AppStatus.WrapperStatus;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.WrapperStatusDto;

/**
 * @author Olivier Bex-Chauvet
 *
 */
@RestController
@RequestMapping(path = "/wrapper")
public class L1WrapperStatus {

	@Autowired
	private AppStatus appStatus;

	@RequestMapping(method = RequestMethod.GET, path = "/status")
	public ResponseEntity<WrapperStatusDto> getStatusRest() {
		WrapperStatus currentStatus = appStatus.getStatus();
		long currentTimestamp = System.currentTimeMillis();
		long timeSinceLastChange = currentTimestamp - currentStatus.getDateLastChangeMs();
		WrapperStatusDto wrapperStatus = new WrapperStatusDto(currentStatus.getState(),
				timeSinceLastChange, currentStatus.getErrorCounter());
		if (currentStatus.isFatalError()) {
			return new ResponseEntity<WrapperStatusDto>(wrapperStatus, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<WrapperStatusDto>(wrapperStatus, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, path = "/stop")
	public ResponseEntity<String> postStop() {
		appStatus.setStopping();
		return new ResponseEntity<String>("L1 Wrapper is scheduled to stop after the end of current process",
				HttpStatus.OK);
	}

}
