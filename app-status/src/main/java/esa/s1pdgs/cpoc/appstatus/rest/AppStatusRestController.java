package esa.s1pdgs.cpoc.appstatus.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.appstatus.dto.AppStatusDto;
import esa.s1pdgs.cpoc.status.AppStatus;
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
    public ResponseEntity<AppStatusDto> getStatusRest() {
    	Status status = appStatus.getStatus();
    	long msSinceLastChange = System.currentTimeMillis() - status.getDateLastChangeMs();
        AppStatusDto dto = new AppStatusDto(status.getState(), msSinceLastChange, status.getErrorCounterProcessing());
        HttpStatus httpStatus = status.isFatalError() ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK;
		return new ResponseEntity<AppStatusDto>(dto, httpStatus);
    }

    /**
     * Stop application
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, path = "/stop")
    public ResponseEntity<String> postStop() {
        LOGGER.info(
                "[MONITOR] Service is scheduled to stop");
        appStatus.setStopping();
        return new ResponseEntity<String>(
                "Service is scheduled to stop",
                HttpStatus.OK);
    }
    
}
