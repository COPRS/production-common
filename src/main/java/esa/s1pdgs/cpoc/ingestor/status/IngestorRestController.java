package esa.s1pdgs.cpoc.ingestor.status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.ingestor.status.AppStatus.IngestorStatus;
import esa.s1pdgs.cpoc.ingestor.status.dto.IngestorStatusDto;


/**
 * Rest controller for the ingestor service
 *
 * @author Viveris Technologies
 */
@RestController
@RequestMapping(path = "/app")
public class IngestorRestController {
    
    /**
     * Logger
     */
    protected static final Logger LOGGER =
            LogManager.getLogger(IngestorRestController.class);

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
    public IngestorRestController(final AppStatus appStatus) {
        this.appStatus = appStatus;
    }

    /**
     * Get application status
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = "/status")
    public ResponseEntity<IngestorStatusDto> getStatusRest() {
        IngestorStatus currentStatus = appStatus.getStatus();
        long currentTimestamp = System.currentTimeMillis();
        long timeSinceLastChange =
                currentTimestamp - currentStatus.getDateLastChangeMs();
        IngestorStatusDto jobStatus =
                new IngestorStatusDto(currentStatus.getState(),
                        timeSinceLastChange, currentStatus.getErrorCounterAux());
        if (currentStatus.isFatalError()) {
            return new ResponseEntity<IngestorStatusDto>(jobStatus,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<IngestorStatusDto>(jobStatus,
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
