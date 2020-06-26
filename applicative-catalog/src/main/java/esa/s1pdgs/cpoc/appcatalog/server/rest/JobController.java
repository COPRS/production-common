package esa.s1pdgs.cpoc.appcatalog.server.rest;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationInvalidStateException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobInvalidStateException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobNotFoundException;
import esa.s1pdgs.cpoc.appcatalog.server.service.AppDataJobService;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.filter.FilterCriterion;
import esa.s1pdgs.cpoc.common.filter.FilterUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;

/**
 * @author Viveris Technologies
 */
@RestController
public class JobController {
    private static final Logger LOGGER = LogManager.getLogger(JobController.class);

    private final AppDataJobService appDataJobService;

    private final static String PK_ORDER_BY_ASC = "[orderByAsc]";
    private final static String PK_ORDER_BY_DESC = "[orderByDesc]";

    private final static String PK_ID = "_id";
    private final static String PK_CREATION = "creationDate";
    private final static String PK_UPDATE = "lastUpdateDate";
    private final static String PK_MESSAGES_ID = "messages.id";
    private final static String PK_PRODUCT_START = "product.startTime";
    private final static String PK_PRODUCT_STOP = "product.stopTime";

    public JobController(final AppDataJobService appDataJobService) {
        this.appDataJobService = appDataJobService;
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/jobs/search")
    public List<AppDataJob> search(@RequestParam final Map<String, String> params) throws  InternalErrorException {
        // Extract criterion
        final List<FilterCriterion> filters = new ArrayList<>();
        Sort sort = null;
        for (final String keyFilter : params.keySet()) {
            final String valueFilter = params.get(keyFilter);
            switch (keyFilter) {
                case PK_ORDER_BY_ASC:
                    sort = new Sort(Direction.ASC, valueFilter);
                    break;
                case PK_ORDER_BY_DESC:
                    sort = new Sort(Direction.DESC, valueFilter);
                    break;
                default:
                    final FilterCriterion criterion = FilterUtils
                            .extractCriterion(keyFilter, valueFilter);
                    switch (criterion.getKey()) {
                        case PK_ID:
                        case PK_MESSAGES_ID:
                            criterion.setValue(Long.decode(valueFilter));
                            break;
                        case PK_CREATION:
                        case PK_UPDATE:
                            criterion.setValue(convertDateIso(valueFilter));
                            break;
                        case PK_PRODUCT_START:
                        case PK_PRODUCT_STOP:
                            criterion.setValue(convertDateIso(valueFilter));
                            break;
                    }
                    filters.add(criterion);
                    break;
            }
        }
        LOGGER.trace("performing search for input: {} {} {}", filters, sort);
        final List<AppDataJob> result = appDataJobService.search(filters, sort);
        LOGGER.trace("search result: {}", result);
        return result;
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

    @DeleteMapping("/{jobId}")
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
