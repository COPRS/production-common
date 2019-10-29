package esa.s1pdgs.cpoc.appcatalog.server.job.rest;

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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobService;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationInvalidStateException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationInvalidTransitionStateException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationNotFoundException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationTerminatedException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobInvalidStateException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobNotFoundException;
import esa.s1pdgs.cpoc.appcatalog.server.job.rest.JobControllerConfiguration.Generations;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.filter.FilterCriterion;
import esa.s1pdgs.cpoc.common.filter.FilterUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;

/**
 * @author Viveris Technologies
 */
@RestController
@EnableConfigurationProperties(JobControllerConfiguration.class)
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
    
    private final JobControllerConfiguration config;

    /**
     * Constructor
     * 
     * @param appDataJobService
     */
    public JobController(
    		final AppDataJobService appDataJobService,
            final JobControllerConfiguration config
    ) {
        this.appDataJobService = appDataJobService;
        this.config = config;
    }

    /**
     * Search for jobs
     * 
     * @param params
     * @return
     * @throws AppCatalogJobNotFoundException
     * @throws AppCatalogJobInvalidStateException
     * @throws AppCatalogJobGenerationInvalidStateException
     * @throws InternalErrorException
     */
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{category}/jobs/search")
    public List<AppDataJob> search(
    		@PathVariable(name = "category") final String categoryName,
            @RequestParam Map<String, String> params)
            throws AppCatalogJobNotFoundException,
            AppCatalogJobInvalidStateException,
            AppCatalogJobGenerationInvalidStateException,
            InternalErrorException {
        // Extract criterion
    	final ProductCategory category = ProductCategory.valueOf(categoryName.toUpperCase());
        List<FilterCriterion> filters = new ArrayList<>();
        Sort sort = null;
        for (String keyFilter : params.keySet()) {
            String valueFilter = params.get(keyFilter);
            switch (keyFilter) {
                case PK_ORDER_BY_ASC:
                    sort = new Sort(Direction.ASC, valueFilter);
                    break;
                case PK_ORDER_BY_DESC:
                    sort = new Sort(Direction.DESC, valueFilter);
                    break;
                default:
                    FilterCriterion criterion = FilterUtils
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
        // Search
        LOGGER.trace ("performing search for input: {} {} {}", filters, category, sort);
        List<AppDataJob> result = appDataJobService.search(filters, category, sort);
        LOGGER.trace ("search result: {}", result);
        return result;
    }

    /**
     * Get one job
     * 
     * @param jobId
     * @return
     * @throws AppCatalogJobNotFoundException
     * @throws AppCatalogJobGenerationInvalidStateException
     * @throws AppCatalogJobInvalidStateException
     */
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{category}/jobs/{jobId}")
    public AppDataJob one(
    		@PathVariable(name = "category") final String categoryName,
    		@PathVariable(name = "jobId") final Long jobId)
            throws AppCatalogJobNotFoundException,
            AppCatalogJobInvalidStateException,
            AppCatalogJobGenerationInvalidStateException {
    	AppDataJob result= appDataJobService.getJob(jobId);
        LOGGER.debug ("Result found for AppDataJob: {}", jobId);
    	return result;
    }

    /**
     * Create a job
     * 
     * @param newJob
     * @return
     * @throws AppCatalogJobInvalidStateException
     * @throws AppCatalogJobGenerationInvalidStateException
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{category}/jobs")
    public AppDataJob newJob(
    		@PathVariable(name = "category") final String categoryName,
    		@RequestBody final JsonNode node)
            throws AppCatalogJobInvalidStateException,
            AppCatalogJobGenerationInvalidStateException, IOException {
    	
    	final ProductCategory cat = ProductCategory.valueOf(categoryName.toUpperCase());
    	
     	final ObjectMapper objMapper = new ObjectMapper();
    	final TypeFactory typeFactory = objMapper.getTypeFactory();
    	final JavaType javaType = typeFactory.constructParametricType(
    			AppDataJob.class, 
    			cat.getDtoClass()
    	); 
    	final AppDataJob<?> newJob = objMapper
    			.readValue(objMapper.treeAsTokens(node), javaType);
    	
    	newJob.setCategory(cat);    	
    	LOGGER.debug ("== newJob {}",newJob.toString());
    	// Create it
    	AppDataJob<?> jobResult = appDataJobService.newJob(newJob);
    	LOGGER.debug ("== jobResult {}", jobResult.toString());

        return jobResult;
    }

    /**
     * Delete a job
     * 
     * @param jobId
     */
    @DeleteMapping("/{jobId}")
    public void deleteJob(@PathVariable final Long jobId) {
        appDataJobService.deleteJob(jobId);
    }

    /**
     * Patch a job
     * 
     * @param jobId
     * @param patchJob
     * @return
     * @throws AppCatalogJobInvalidStateException
     * @throws AppCatalogJobNotFoundException
     * @throws AppCatalogJobGenerationInvalidStateException
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     */
    @RequestMapping(method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{category}/jobs/{jobId}")
    public AppDataJob patchJob(
    		@PathVariable(name = "category") final String categoryName,
            @PathVariable(name = "jobId") final Long jobId,
            @RequestBody final JsonNode node)
            throws AppCatalogJobInvalidStateException,
            AppCatalogJobNotFoundException,
            AppCatalogJobGenerationInvalidStateException, JsonParseException, JsonMappingException, IOException {
    	
    	final ProductCategory cat = ProductCategory.valueOf(categoryName.toUpperCase());
     	final ObjectMapper objMapper = new ObjectMapper();
    	final TypeFactory typeFactory = objMapper.getTypeFactory();
    	final JavaType javaType = typeFactory.constructParametricType(
    			AppDataJob.class, 
    			cat.getDtoClass()
    	); 
    	final AppDataJob<?> patchJob = objMapper
    			.readValue(objMapper.treeAsTokens(node), javaType);    	
   
    	try {
    	 	LOGGER.debug ("patching Job {}, {}",jobId,patchJob);
			AppDataJob job = appDataJobService.patchJob(jobId,patchJob);
			job.setCategory(cat);			
    	 	LOGGER.debug ("job patched {}, {}",jobId,job);
			return job;
		} catch (Exception e) {
			LOGGER.error("Exception occured while patching job: {}", LogUtils.toString(e));
			throw new RuntimeException(
					String.format("Exception occured while patching job %s", jobId), 
					e
			);
		}
    }

    /**
     * Set messages of a given job
     * 
     * @param jobId
     * @param messages
     * @return
     * @throws AppCatalogJobInvalidStateException
     * @throws AppCatalogJobGenerationInvalidStateException
     * @throws AppCatalogJobNotFoundException
     * @throws AppCatalogJobGenerationNotFoundException
     * @throws AppCatalogJobGenerationInvalidTransitionStateException
     */
    @RequestMapping(method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{category}/jobs/{jobId}/generations/{taskTable}")
    public AppDataJob patchGenerationOfJob(
    		@PathVariable(name = "category") final String categoryName,
            @PathVariable(name = "jobId") final Long jobId,
            @PathVariable(name = "taskTable") final String taskTable,
            @RequestBody final AppDataJobGeneration generation)
            throws AppCatalogJobInvalidStateException,
            AppCatalogJobGenerationInvalidStateException,
            AppCatalogJobNotFoundException,
            AppCatalogJobGenerationInvalidTransitionStateException,
            AppCatalogJobGenerationNotFoundException {
    	final ProductCategory category = ProductCategory.valueOf(categoryName.toUpperCase());
        AppDataJob ret = null;
        try {
            ret = appDataJobService.patchGenerationToJob(jobId, taskTable, generation,
                            getFor(category, generation.getState()));
        } catch (AppCatalogJobGenerationTerminatedException e) {
            LOGGER.error("[jobId {}] [taskTable {}] [code {}] {}", jobId,
                    taskTable, e.getCode().getCode(), e.getLogMessage());
            // TODO publish error message in kafka
        }
        return ret;
    }
    
    final Date convertDateIso(final String dateStr)
            throws InternalErrorException {
        try {
		    final DateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		    return format.parse(dateStr);
		} catch (ParseException pe) {
		    throw new InternalErrorException("Cannot convert date " + dateStr, pe);
		}
    }
    
    private final int getFor(ProductCategory category, AppDataJobGenerationState state)
    {
    	try {
			final Generations gen = config.getFor(category);
			if (state == AppDataJobGenerationState.INITIAL)
			{
				return gen.getMaxErrorsInitial();
			}
			return gen.getMaxErrorsPrimaryCheck();
		} catch (Exception e) {
			// don't fail here but return some sane default.
	    	return 300;
		}
    }
}
