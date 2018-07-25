package fr.viveris.s1pdgs.jobgenerator.status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.viveris.s1pdgs.jobgenerator.status.AppStatus;
import fr.viveris.s1pdgs.jobgenerator.status.JobRestController;
import fr.viveris.s1pdgs.jobgenerator.status.AppStatus.JobStatus;
import fr.viveris.s1pdgs.jobgenerator.status.dto.JobStatusDto;

@RestController
@RequestMapping(path = "/job")
public class JobRestController {

    /**
     * Logger
     */
    protected static final Logger LOGGER =
            LogManager.getLogger(JobRestController.class);

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
    public JobRestController(final AppStatus appStatus) {
        this.appStatus = appStatus;
    }

    /**
     * Get application status
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = "/status")
    public ResponseEntity<JobStatusDto> getStatusRest() {
        JobStatus currentStatus = appStatus.getStatus();
        long currentTimestamp = System.currentTimeMillis();
        long timeSinceLastChange =
                currentTimestamp - currentStatus.getDateLastChangeMs();
        JobStatusDto wrapperStatus =
                new JobStatusDto(currentStatus.getState(),
                        timeSinceLastChange, currentStatus.getErrorCounter());
        if (currentStatus.isFatalError()) {
            return new ResponseEntity<JobStatusDto>(wrapperStatus,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<JobStatusDto>(wrapperStatus,
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

    /**
     * Stop application
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = "/process/{messageId}")
    public ResponseEntity<Boolean> isProcessing(
            @PathVariable(name = "messageId") final long messageId) {
        LOGGER.info("tutu");
        boolean ret = false;
        if (appStatus.getProcessingMsgId() == messageId) {
            ret = true;
        }
        return new ResponseEntity<Boolean>(ret, HttpStatus.OK);
    }
}
