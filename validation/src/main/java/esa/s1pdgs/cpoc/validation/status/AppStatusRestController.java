package esa.s1pdgs.cpoc.validation.status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.validation.status.AppStatus.ValidationStatus;
import esa.s1pdgs.cpoc.validation.status.dto.ValidationStatusDto;

@RestController
@RequestMapping(path = "/app")
public class AppStatusRestController {

    /**
     * Logger
     */
    protected static final Logger LOGGER =
            LogManager.getLogger(AppStatusRestController.class);

    /**
     * Application status
     */
    private final AppStatus appStatus;

    /**
     * Constructor
     * 
     * @param appStatus
     */
    @Autowired
    public AppStatusRestController(final AppStatus appStatus) {
        this.appStatus = appStatus;
    }

    /**
     * Get application status
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = "/status")
    public ResponseEntity<ValidationStatusDto> getStatusRest() {
    	ValidationStatus currentStatus = appStatus.getStatus();
        long currentTimestamp = System.currentTimeMillis();
        long timeSinceLastChange =
                currentTimestamp - currentStatus.getDateLastChangeMs();
        ValidationStatusDto validationStatus =
                new ValidationStatusDto(currentStatus.getState(),
                        timeSinceLastChange, currentStatus.getErrorCounter());
        if (currentStatus.isFatalError()) {
            return new ResponseEntity<ValidationStatusDto>(validationStatus,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<ValidationStatusDto>(validationStatus,
                HttpStatus.OK);
    }

    /**
     * Stop application
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, path = "/stop")
    public ResponseEntity<String> postStop() {
        LOGGER.info(
                "[MONITOR] Validation service is scheduled to stop");
        appStatus.setStopping();
        return new ResponseEntity<String>(
                "Validation service is scheduled to stop",
                HttpStatus.OK);
    }
    
}
