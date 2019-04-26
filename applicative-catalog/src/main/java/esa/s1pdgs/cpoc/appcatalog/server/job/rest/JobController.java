package esa.s1pdgs.cpoc.appcatalog.server.job.rest;

import java.util.ArrayList;
import java.util.HashMap;
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

import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDtoState;
import esa.s1pdgs.cpoc.appcatalog.server.job.converter.JobConverter;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobService;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationInvalidStateException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationInvalidTransitionStateException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationNotFoundException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationTerminatedException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobInvalidStateException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobNotFoundException;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.filter.FilterCriterion;
import esa.s1pdgs.cpoc.common.filter.FilterUtils;
import esa.s1pdgs.cpoc.common.utils.DateUtils;

/**
 * @author Viveris Technologies
 */
public class JobController<T> {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(JobController.class);

    /**
     * Job service
     */
    private final AppDataJobService appDataJobService;

    /**
     * Job converter
     */
    private final JobConverter<T> jobConverter;

    /**
     * Product category
     */
    private final ProductCategory category;

    /**
     * Search key for job identifier
     */
    private final static String PK_ORDER_BY_ASC = "[orderByAsc]";

    /**
     * Search key for job identifier
     */
    private final static String PK_ORDER_BY_DESC = "[orderByDesc]";

    /**
     * Search key for job identifier
     */
    private final static String PK_ID = "_id";

    /**
     * Search key for job creation date
     */
    private final static String PK_CREATION = "creationDate";

    /**
     * Search key for job last update date
     */
    private final static String PK_UPDATE = "lastUpdateDate";

    /**
     * Search key for job messages identifiers
     */
    private final static String PK_MESSAGES_ID = "messages.identifier";

    /**
     * Search key for job product start time
     */
    private final static String PK_PRODUCT_START = "product.startTime";

    /**
     * Search key for job product stop time
     */
    private final static String PK_PRODUCT_STOP = "product.stopTime";

    /**
     * Maximal number of errors
     */
    private final Map<AppDataJobGenerationDtoState, Integer> maxNbErrors;

    /**
     * Constructor
     * 
     * @param appDataJobService
     */
    public JobController(final AppDataJobService appDataJobService,
            final JobConverter<T> jobConverter,
            final ProductCategory category) {
        this.appDataJobService = appDataJobService;
        this.jobConverter = jobConverter;
        this.category = category;
        this.maxNbErrors = new HashMap<>();
    }

    /**
     * @return the maxNbErrors
     */
    public Map<AppDataJobGenerationDtoState, Integer> getMaxNbErrors() {
        return maxNbErrors;
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
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/search")
    public List<AppDataJobDto<T>> search(
            @RequestParam Map<String, String> params)
            throws AppCatalogJobNotFoundException,
            AppCatalogJobInvalidStateException,
            AppCatalogJobGenerationInvalidStateException,
            InternalErrorException {
        // Extract criterion
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
                            criterion.setValue(Long.decode(valueFilter));
                            break;
                        case PK_MESSAGES_ID:
                            criterion.setValue(Long.decode(valueFilter));
                            break;
                        case PK_CREATION:
                        case PK_UPDATE:
                            criterion.setValue(
                                    DateUtils.convertDateIso(valueFilter));
                            break;
                        case PK_PRODUCT_START:
                        case PK_PRODUCT_STOP:
                            criterion.setValue(
                                    DateUtils.convertDateIso(valueFilter));
                            break;
                    }
                    filters.add(criterion);
                    break;
            }
        }
        // Search
        List<AppDataJob> jobsDb =
                appDataJobService.search(filters, category, sort);
        // Convert into DTO
        List<AppDataJobDto<T>> jobsDto = new ArrayList<>();
        for (AppDataJob jobDb : jobsDb) {
            jobsDto.add(jobConverter.convertJobFromDbToDto(jobDb));
        }
        return jobsDto;
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
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{jobId}")
    public AppDataJobDto<T> one(@PathVariable(name = "jobId") final Long jobId)
            throws AppCatalogJobNotFoundException,
            AppCatalogJobInvalidStateException,
            AppCatalogJobGenerationInvalidStateException {
        return jobConverter
                .convertJobFromDbToDto(appDataJobService.getJob(jobId));
    }

    /**
     * Create a job
     * 
     * @param newJob
     * @return
     * @throws AppCatalogJobInvalidStateException
     * @throws AppCatalogJobGenerationInvalidStateException
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AppDataJobDto<T> newJob(@RequestBody final AppDataJobDto<T> newJob)
            throws AppCatalogJobInvalidStateException,
            AppCatalogJobGenerationInvalidStateException {
        // Convert into database message
        AppDataJob newJobDb = jobConverter.convertJobFromDtoToDb(newJob);

        // Create it
        return jobConverter
                .convertJobFromDbToDto(appDataJobService.newJob(newJobDb));
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
     */
    @RequestMapping(method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{jobId}")
    public AppDataJobDto<T> patchJob(
            @PathVariable(name = "jobId") final Long jobId,
            @RequestBody final AppDataJobDto<T> patchJob)
            throws AppCatalogJobInvalidStateException,
            AppCatalogJobNotFoundException,
            AppCatalogJobGenerationInvalidStateException {
        return jobConverter.convertJobFromDbToDto(appDataJobService
                .patchJob(jobId, jobConverter.convertJobFromDtoToDb(patchJob)));
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
    @RequestMapping(method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{jobId}/generations/{taskTable}")
    public AppDataJobDto<T> patchGenerationOfJob(
            @PathVariable(name = "jobId") final Long jobId,
            @PathVariable(name = "taskTable") final String taskTable,
            @RequestBody final AppDataJobGenerationDto generation)
            throws AppCatalogJobInvalidStateException,
            AppCatalogJobGenerationInvalidStateException,
            AppCatalogJobNotFoundException,
            AppCatalogJobGenerationInvalidTransitionStateException,
            AppCatalogJobGenerationNotFoundException {
        AppDataJobDto<T> ret = null;
        try {
            ret = jobConverter.convertJobFromDbToDto(
                    appDataJobService.patchGenerationToJob(jobId, taskTable,
                            jobConverter.convertJobGenerationFromDtoToDb(
                                    generation),
                            maxNbErrors.get(generation.getState())));
        } catch (AppCatalogJobGenerationTerminatedException e) {
            LOGGER.error("[jobId {}] [taskTable {}] [code {}] {}", jobId,
                    taskTable, e.getCode().getCode(), e.getLogMessage());
            // TODO publish error message in kafka
        }
        return ret;
    }
}
