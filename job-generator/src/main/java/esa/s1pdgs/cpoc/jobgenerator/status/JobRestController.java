package esa.s1pdgs.cpoc.jobgenerator.status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.jobgenerator.status.AppStatus;
import esa.s1pdgs.cpoc.jobgenerator.status.JobRestController;
import esa.s1pdgs.cpoc.jobgenerator.status.AppStatus.JobStatus;
import esa.s1pdgs.cpoc.jobgenerator.status.dto.JobStatusDto;

@RestController
@RequestMapping(path = "/app")
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
        JobStatusDto jobStatus =
                new JobStatusDto(currentStatus.getState(),
                        timeSinceLastChange, currentStatus.getErrorCounterProcessing());
        if (currentStatus.isFatalError()) {
            return new ResponseEntity<JobStatusDto>(jobStatus,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<JobStatusDto>(jobStatus,
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
    @RequestMapping(method = RequestMethod.GET, path = "/{category}/process/{messageId}")
    public ResponseEntity<Boolean> isProcessing(
            @PathVariable(name = "category") final String category,
            @PathVariable(name = "messageId") final long messageId) {
        LOGGER.info("tutu");
        boolean ret = false;
        if (ProductCategory.EDRS_SESSIONS.name().toLowerCase().equals(category) ||
                ProductCategory.LEVEL_PRODUCTS.name().toLowerCase().equals(category)) {
            if (appStatus.getProcessingMsgId() == messageId) {
                ret = true;
            }
        } else {
            LOGGER.warn(
                    "[category {}] [messageId {}] Ask for message processing on a not manageable category",
                    category, messageId);
        }
        return new ResponseEntity<Boolean>(ret, HttpStatus.OK);
    }
}
