package esa.s1pdgs.cpoc.prip.status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.prip.status.dto.PripStatusDto;
import esa.s1pdgs.cpoc.status.Status;

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
    private final AppStatusImpl appStatus;

    /**
     * Constructor
     * 
     * @param appStatus
     */
    @Autowired
    public AppStatusRestController(final AppStatusImpl appStatus) {
        this.appStatus = appStatus;
    }

    /**
     * Get application status
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = "/status")
    public ResponseEntity<PripStatusDto> getStatusRest() {
    	Status status = appStatus.getStatus();
    	long msSinceLastChange = System.currentTimeMillis() - status.getDateLastChangeMs();
        PripStatusDto dto = new PripStatusDto(status.getState(), msSinceLastChange, status.getErrorCounterProcessing());
        HttpStatus httpStatus = status.isFatalError() ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK;
		return new ResponseEntity<PripStatusDto>(dto, httpStatus);
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
