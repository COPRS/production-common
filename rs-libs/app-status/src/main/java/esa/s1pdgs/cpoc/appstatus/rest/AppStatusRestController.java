package esa.s1pdgs.cpoc.appstatus.rest;

import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.appstatus.Status;
import esa.s1pdgs.cpoc.appstatus.dto.AppStatusDto;

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
    	AppStatusDto statusDto = new AppStatusDto(status.getState());
    	if (status.getSubStatuses().isEmpty()) {
	        statusDto.setErrorCounter(status.getErrorCounterNextMessage() + status.getErrorCounterProcessing());
	        statusDto.setTimeSinceLastChange(System.currentTimeMillis() - status.getDateLastChangeMs());
    	} else {
    		for (Status subStatus : appStatus.getSubStatuses().values()) {
    			AppStatusDto subStatusDto = new AppStatusDto(subStatus.getCategory().get(), subStatus.getState());
    			subStatusDto.setErrorCounter(subStatus.getErrorCounterNextMessage() + subStatus.getErrorCounterProcessing());
    			subStatusDto.setTimeSinceLastChange(System.currentTimeMillis() - subStatus.getDateLastChangeMs());
    			statusDto.addSubStatuses(subStatusDto);
    		}
    	}
        HttpStatus httpStatus = status.isFatalError() ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK;
		return new ResponseEntity<AppStatusDto>(statusDto, httpStatus);
    }
    
    @RequestMapping(method = RequestMethod.GET, path = "/readiness")
    public ResponseEntity<String> getReadiness() {
    	HttpStatus httpStatus = appStatus.getKubernetesReadiness() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
    	return new ResponseEntity<String>("", httpStatus);    	
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
    
    @RequestMapping(method = RequestMethod.GET, path = "/{category}/process/{messageId}")
	public ResponseEntity<Boolean> isProcessing(@PathVariable(name = "category") final String category,
			@PathVariable(name = "messageId") final long messageId) {
		try {
			return new ResponseEntity<Boolean>(appStatus.isProcessing(category, messageId), HttpStatus.OK);
		} catch (UnsupportedOperationException e) {
			// service does not offer isProcessing()
			return new ResponseEntity<Boolean>(false, HttpStatus.NOT_FOUND);
		} catch (NoSuchElementException e) {
			// the given category does not exist in general or is not known by the called implementation
			LOGGER.warn(
					"[category {}] [messageId {}] Ask for message processing information on a not manageable category",
					category, messageId);
			return new ResponseEntity<Boolean>(false, HttpStatus.OK); // 200 for now to be conform with the existing tests (can we change this to 404?)   
		} catch (IllegalArgumentException e) {
			// invalid message id
			LOGGER.warn("[category {}] [messageId {}] Ask for message processing information for an invalid message id",
					category, messageId);
			return new ResponseEntity<Boolean>(false, HttpStatus.BAD_REQUEST);
		}
	}
    
}
