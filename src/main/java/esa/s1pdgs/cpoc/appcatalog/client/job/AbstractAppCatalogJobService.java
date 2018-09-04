package esa.s1pdgs.cpoc.appcatalog.client.job;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDtoState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogJobNewApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogJobPatchApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogJobPatchGenerationApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogJobSearchApiError;
import esa.s1pdgs.cpoc.common.utils.LogUtils;

/**
 * Generic client for requesting applicative catalog around job applicative data
 * 
 * @author Viveris Technologies
 * @param <T>
 *            the type of the DTO objects used for a product category
 */
public abstract class AbstractAppCatalogJobService<T> {

    /**
     * Logger
     */
    private static final Log LOGGER =
            LogFactory.getLog(AbstractAppCatalogJobService.class);

    /**
     * Rest template
     */
    protected final RestTemplate restTemplate;

    /**
     * Host URI. Example: http://localhost:8081
     */
    protected final String hostUri;

    /**
     * Maximal number of retries
     */
    protected final int maxRetries;

    /**
     * Temporisation in ms betwenn 2 retries
     */
    protected final int tempoRetryMs;

    /**
     * Product category
     */
    protected final ProductCategory category;

    /**
     * Constructor
     * 
     * @param restTemplate
     * @param category
     * @param hostUri
     * @param maxRetries
     * @param tempoRetryMs
     */
    public AbstractAppCatalogJobService(final RestTemplate restTemplate,
            final String hostUri, final int maxRetries, final int tempoRetryMs,
            final ProductCategory category) {
        this.restTemplate = restTemplate;
        this.hostUri = hostUri;
        if (maxRetries < 0 || maxRetries > 20) {
            this.maxRetries = 0;
        } else {
            this.maxRetries = maxRetries;
        }
        this.tempoRetryMs = tempoRetryMs;
        this.category = category;
    }

    /**
     * @return the hostUri
     */
    public String getHostUri() {
        return hostUri;
    }

    /**
     * @return the maxRetries
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * @return the tempoRetryMs
     */
    public int getTempoRetryMs() {
        return tempoRetryMs;
    }

    /**
     * @return the category
     */
    public ProductCategory getCategory() {
        return category;
    }

    /**
     * Wait or throw an error according the number of retries
     * 
     * @param retries
     * @param cause
     * @throws AbstractCodedException
     */
    protected void waitOrThrow(final int retries,
            final AbstractCodedException cause, final String api)
            throws AbstractCodedException {
        LOGGER.debug(String.format("[api %s] %s Retry %d/%d", api,
                cause.getLogMessage(), retries, maxRetries));
        if (retries < maxRetries) {
            try {
                Thread.sleep(tempoRetryMs);
            } catch (InterruptedException e) {
                throw cause;
            }
        } else {
            throw cause;
        }
    }

    /**
     * Search for jobs
     * 
     * @param filters
     * @return
     * @throws AbstractCodedException
     */
    public List<AppDataJobDto<T>> search(final Map<String, String> filters)
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uriStr = hostUri + "/" + category.name().toLowerCase() + "/jobs/search";
            UriComponentsBuilder builder =
                    UriComponentsBuilder.fromUriString(uriStr);
            for (String key : filters.keySet()) {
                builder = builder.queryParam(key, filters.get(key));
            }
            URI uri = builder.build().toUri();
            LogUtils.traceLog(LOGGER, String.format("[uri %s]", uri));
            try {
                ResponseEntity<List<AppDataJobDto<T>>> response =
                        internalExchangeSearch(uri);
                if (response.getStatusCode() == HttpStatus.OK) {
                    LogUtils.traceLog(LOGGER, String.format("[uri %s] [ret %s]",
                            uri, response.getBody()));
                    return response.getBody();
                } else {
                    waitOrThrow(retries,
                            new AppCatalogJobSearchApiError(uri.toString(),
                                    "HTTP status code "
                                            + response.getStatusCode()),
                            "search");
                }
            } catch (HttpStatusCodeException hsce) {
                waitOrThrow(retries, new AppCatalogJobSearchApiError(
                        uri.toString(),
                        String.format(
                                "HttpStatusCodeException occured: %s - %s",
                                hsce.getStatusCode(),
                                hsce.getResponseBodyAsString())),
                        "search");
            } catch (RestClientException rce) {
                waitOrThrow(retries,
                        new AppCatalogJobSearchApiError(uri.toString(),
                                String.format(
                                        "RestClientException occured: %s",
                                        rce.getMessage()),
                                rce),
                        "search");
            }
        }
    }

    /**
     * Search by message identifier
     * 
     * @param messageId
     * @return
     * @throws AbstractCodedException
     */
    public List<AppDataJobDto<T>> findByMessagesIdentifier(final long messageId)
            throws AbstractCodedException {
        Map<String, String> filters = new HashMap<>();
        filters.put("messages.identifier", Long.toString(messageId));
        return search(filters);
    }

    /**
     * Search by message identifier
     * 
     * @param messageId
     * @return
     * @throws AbstractCodedException
     */
    public List<AppDataJobDto<T>> findByProductSessionId(final String sessionId)
            throws AbstractCodedException {
        Map<String, String> filters = new HashMap<>();
        filters.put("product.sessionId", sessionId);
        return search(filters);
    }

    /**
     * Search by pod and state
     * 
     * @param pod
     * @param state
     * @return
     * @throws AbstractCodedException
     */
    public List<AppDataJobDto<T>> findByPodAndState(final String pod,
            final AppDataJobDtoState state) throws AbstractCodedException {
        Map<String, String> filters = new ConcurrentHashMap<>();
        filters.put("state", state.name());
        filters.put("pod", pod);
        return search(filters);
    }

    /**
     * Search for job with generating tastables per pod and task table
     * 
     * @param pod
     * @param taskTable
     * @return
     * @throws AbstractCodedException
     */
    public List<AppDataJobDto<T>> findNByPodAndGenerationTaskTableWithNotSentGeneration(
            final String pod, final String taskTable)
            throws AbstractCodedException {
        Map<String, String> filters = new HashMap<>();
        filters.put("pod", pod);
        filters.put("generations.state[neq]",
                AppDataJobGenerationDtoState.SENT.name());
        filters.put("generations.taskTable", taskTable);
        filters.put("[orderByAsc]", "generations.lastUpdateDate");
        return search(filters);
    }

    /**
     * Internal method for call search api
     * 
     * @param uri
     * @return
     */
    protected abstract ResponseEntity<List<AppDataJobDto<T>>> internalExchangeSearch(
            final URI uri);

    /**
     * Create a new job from its identifier
     * 
     * @param identifier
     * @return
     * @throws AbstractCodedException
     */
    public AppDataJobDto<T> newJob(final AppDataJobDto<T> job)
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri = hostUri + "/" + category.name().toLowerCase() + "/jobs";
            LogUtils.traceLog(LOGGER, String.format("[uri %s]", uri));
            try {
                ResponseEntity<AppDataJobDto<T>> response =
                        internalExchangeNewJob(uri, job);
                if (response.getStatusCode() == HttpStatus.OK) {
                    LogUtils.traceLog(LOGGER, String.format("[uri %s] [ret %s]",
                            uri, response.getBody()));
                    return response.getBody();
                } else {
                    waitOrThrow(retries, new AppCatalogJobNewApiError(uri, job,
                            "HTTP status code " + response.getStatusCode()),
                            "new");
                }
            } catch (HttpStatusCodeException hsce) {
                waitOrThrow(retries,
                        new AppCatalogJobNewApiError(uri, job, String.format(
                                "HttpStatusCodeException occured: %s - %s",
                                hsce.getStatusCode(),
                                hsce.getResponseBodyAsString())),
                        "new");
            } catch (RestClientException rce) {
                waitOrThrow(retries,
                        new AppCatalogJobNewApiError(uri, job,
                                String.format(
                                        "HttpStatusCodeException occured: %s",
                                        rce.getMessage()),
                                rce),
                        "new");
            }
        }
    }

    /**
     * Internal method for call rest API one
     * 
     * @param uri
     * @return
     */
    protected abstract ResponseEntity<AppDataJobDto<T>> internalExchangeNewJob(
            final String uri, final AppDataJobDto<T> body);

    public AppDataJobDto<T> patchJob(final long identifier,
            final AppDataJobDto<T> dto, final boolean patchMessages,
            final boolean patchProduct, final boolean patchGenerations)
            throws AbstractCodedException {
        dto.setIdentifier(identifier);
        if (!patchMessages) {
            dto.setMessages(new ArrayList<>());
        }
        if (!patchProduct) {
            dto.setProduct(null);
        }
        if (!patchGenerations) {
            dto.setGenerations(new ArrayList<>());
        }
        return patchJob(identifier, dto);
    }

    /**
     * Patch a job
     * 
     * @param identifier
     * @return
     * @throws AbstractCodedException
     */
    private AppDataJobDto<T> patchJob(final long identifier,
            final AppDataJobDto<T> job) throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri = hostUri + "/" + category.name().toLowerCase() + "/jobs/" + identifier;
            LogUtils.traceLog(LOGGER, String.format("[uri %s]", uri));
            try {
                ResponseEntity<AppDataJobDto<T>> response =
                        internalExchangePatchJob(uri, job);
                if (response.getStatusCode() == HttpStatus.OK) {
                    LogUtils.traceLog(LOGGER, String.format("[uri %s] [ret %s]",
                            uri, response.getBody()));
                    return response.getBody();
                } else {
                    waitOrThrow(retries,
                            new AppCatalogJobPatchApiError(uri, job,
                                    "HTTP status code "
                                            + response.getStatusCode()),
                            "patch");
                }
            } catch (HttpStatusCodeException hsce) {
                waitOrThrow(retries,
                        new AppCatalogJobPatchApiError(uri, job, String.format(
                                "HttpStatusCodeException occured: %s - %s",
                                hsce.getStatusCode(),
                                hsce.getResponseBodyAsString())),
                        "patch");
            } catch (RestClientException rce) {
                waitOrThrow(retries,
                        new AppCatalogJobPatchApiError(uri, job,
                                String.format(
                                        "HttpStatusCodeException occured: %s",
                                        rce.getMessage()),
                                rce),
                        "patch");
            }
        }
    }

    /**
     * Internal method for call rest API patch
     * 
     * @param uri
     * @return
     */
    protected abstract ResponseEntity<AppDataJobDto<T>> internalExchangePatchJob(
            final String uri, final AppDataJobDto<T> body);

    /**
     * Patch a generation of a given job
     * 
     * @param identifier
     * @return
     * @throws AbstractCodedException
     */
    public AppDataJobDto<T> patchTaskTableOfJob(final long identifier,
            final String taskTable, final AppDataJobGenerationDtoState state)
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri = hostUri + "/" + category.name().toLowerCase() + "/jobs/" + identifier
                    + "/generations/" + taskTable;
            AppDataJobGenerationDto body = new AppDataJobGenerationDto();
            body.setTaskTable(taskTable);
            body.setState(state);
            LogUtils.traceLog(LOGGER, String.format("[uri %s]", uri));
            try {
                ResponseEntity<AppDataJobDto<T>> response =
                        internalExchangePatchTaskTableOfJob(uri, body);
                if (response.getStatusCode() == HttpStatus.OK) {
                    LogUtils.traceLog(LOGGER, String.format("[uri %s] [ret %s]",
                            uri, response.getBody()));
                    return response.getBody();
                } else {
                    waitOrThrow(retries,
                            new AppCatalogJobPatchGenerationApiError(uri, body,
                                    "HTTP status code "
                                            + response.getStatusCode()),
                            "patch");
                }
            } catch (HttpStatusCodeException hsce) {
                waitOrThrow(retries, new AppCatalogJobPatchGenerationApiError(
                        uri, body,
                        String.format(
                                "HttpStatusCodeException occured: %s - %s",
                                hsce.getStatusCode(),
                                hsce.getResponseBodyAsString())),
                        "patch");
            } catch (RestClientException rce) {
                waitOrThrow(retries,
                        new AppCatalogJobPatchGenerationApiError(uri, body,
                                String.format(
                                        "HttpStatusCodeException occured: %s",
                                        rce.getMessage()),
                                rce),
                        "patch");
            }
        }
    }

    /**
     * Internal method for call rest API patchGeneration
     * 
     * @param uri
     * @return
     */
    protected abstract ResponseEntity<AppDataJobDto<T>> internalExchangePatchTaskTableOfJob(
            final String uri, final AppDataJobGenerationDto body);
}
