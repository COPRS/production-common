package esa.s1pdgs.cpoc.prip.status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.prip.status.AppStatus.PripStatus;
import esa.s1pdgs.cpoc.prip.status.dto.PripStatusDto;

@RestController
@RequestMapping(path = "/app")
public class PripRestController {

    /**
     * Logger
     */
    protected static final Logger LOGGER =
            LogManager.getLogger(PripRestController.class);

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
    public PripRestController(final AppStatus appStatus) {
        this.appStatus = appStatus;
    }

    /**
     * Get application status
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = "/status")
    public ResponseEntity<PripStatusDto> getStatusRest() {
    	PripStatus currentStatus = appStatus.getStatus();
        long currentTimestamp = System.currentTimeMillis();
        long timeSinceLastChange =
                currentTimestamp - currentStatus.getDateLastChangeMs();
        PripStatusDto pripStatus =
                new PripStatusDto(currentStatus.getState(),
                        timeSinceLastChange, currentStatus.getErrorCounter());
        if (currentStatus.isFatalError()) {
            return new ResponseEntity<PripStatusDto>(pripStatus,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<PripStatusDto>(pripStatus,
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
                "[MONITOR] PRIP is scheduled to stop");
        appStatus.setStopping();
        return new ResponseEntity<String>(
                "PRIP is scheduled to stop",
                HttpStatus.OK);
    }
    
}
