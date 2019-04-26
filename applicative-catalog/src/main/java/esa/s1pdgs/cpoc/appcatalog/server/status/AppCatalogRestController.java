package esa.s1pdgs.cpoc.appcatalog.server.status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.appcatalog.server.status.AppStatus.AppCatalogStatus;
import esa.s1pdgs.cpoc.appcatalog.server.status.dto.AppCatalogStatusDto;

@RestController
@RequestMapping(path = "/app")
public class AppCatalogRestController {

    /**
     * Logger
     */
    protected static final Logger LOGGER =
            LogManager.getLogger(AppCatalogRestController.class);

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
    public AppCatalogRestController(final AppStatus appStatus) {
        this.appStatus = appStatus;
    }

    /**
     * Get application status
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = "/status")
    public ResponseEntity<AppCatalogStatusDto> getStatusRest() {
        AppCatalogStatus currentStatus = appStatus.getStatus();
        long currentTimestamp = System.currentTimeMillis();
        long timeSinceLastChange =
                currentTimestamp - currentStatus.getDateLastChangeMs();
        AppCatalogStatusDto jobStatus =
                new AppCatalogStatusDto(currentStatus.getState(),
                        timeSinceLastChange, currentStatus.getErrorCounterMqi());
        if (currentStatus.isFatalError()) {
            return new ResponseEntity<AppCatalogStatusDto>(jobStatus,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<AppCatalogStatusDto>(jobStatus,
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
                "[MONITOR] Job Generator is scheduled to stop after the end of current process");
        appStatus.setStopping();
        return new ResponseEntity<String>(
                "Job Generator is scheduled to stop after the end of current process",
                HttpStatus.OK);
    }
}
