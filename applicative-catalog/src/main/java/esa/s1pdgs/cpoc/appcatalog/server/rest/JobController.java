package esa.s1pdgs.cpoc.appcatalog.server.rest;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationInvalidStateException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobInvalidStateException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobNotFoundException;
import esa.s1pdgs.cpoc.appcatalog.server.service.AppDataJobService;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;

/**
 * @author Viveris Technologies
 */
@RestController
public class JobController {
    private static final Logger LOGGER = LogManager.getLogger(JobController.class);

    private final AppDataJobService appDataJobService;

    public JobController(final AppDataJobService appDataJobService) {
        this.appDataJobService = appDataJobService;
    }
    
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/jobs/findByMessagesId/{messageId}")
    public List<AppDataJob> findByMessagesId(@PathVariable final long messageId) {
    	return appDataJobService.findByMessagesId(messageId);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/jobs/findByProductSessionId/{sessionId}")
    public List<AppDataJob> findByProductSessionId(@PathVariable final String sessionId) {
    	return appDataJobService.findByProductSessionId(sessionId);
    }
    
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/jobs/findByProductDataTakeId/{dataTakeId}")
    public List<AppDataJob> findByProductDataTakeId(@PathVariable final String dataTakeId) {
    	return appDataJobService.findByProductDataTakeId(dataTakeId);
    }
    
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/jobs/findJobInStateGenerating/{taskTable}")
    public List<AppDataJob> findJobInStateGenerating(@PathVariable final String taskTable) {
    	return appDataJobService.findJobInStateGenerating(taskTable);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/jobs/{jobId}")
    public AppDataJob get(@PathVariable(name = "jobId") final Long jobId)
            throws AppCatalogJobNotFoundException {
    	return appDataJobService.getJob(jobId);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/jobs")
    public AppDataJob newJob(@RequestBody final AppDataJob newJob) {  	
    	return appDataJobService.newJob(newJob);
    }

    @RequestMapping(method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/jobs/{jobId}")
    public void deleteJob(@PathVariable final Long jobId) {
        appDataJobService.deleteJob(jobId);
    }

    @RequestMapping(method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/jobs/{jobId}")
    public AppDataJob update(
            @PathVariable(name = "jobId") final Long jobId,
            @RequestBody final AppDataJob patchJob
    )
            throws AppCatalogJobInvalidStateException,
            AppCatalogJobNotFoundException,
            AppCatalogJobGenerationInvalidStateException, JsonParseException, JsonMappingException, IOException {

    	try {
    	 	LOGGER.trace("patching Job {}, {}",jobId, patchJob);
    	 	patchJob.setId(jobId);
			final AppDataJob job = appDataJobService.updateJob(patchJob);	
    	 	LOGGER.trace("job patched {}, {}", jobId, job);
			return job;
		} catch (final Exception e) {
			LOGGER.error("Exception occured while patching job: {}", LogUtils.toString(e));
			throw new RuntimeException(
					String.format("Exception occured while patching job %s", jobId), 
					e
			);
		}
    }
    
    final Date convertDateIso(final String dateStr)
            throws InternalErrorException {
        try {
		    final DateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		    return format.parse(dateStr);
		} catch (final ParseException pe) {
		    throw new InternalErrorException("Cannot convert date " + dateStr, pe);
		}
    }
}
