package esa.s1pdgs.cpoc.appcatalog.client.job;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogJobNewApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogJobPatchApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogJobSearchApiError;
import esa.s1pdgs.cpoc.common.utils.LogUtils;

/**
 * Generic client for requesting applicative catalog around job applicative data
 * 
 * @author Viveris Technologies
 * @param <T>
 *            the type of the DTO objects used for a product category
 */
public class AppCatalogJobClient {

    /**
     * Logger
     */
    private static final Log LOGGER = LogFactory.getLog(AppCatalogJobClient.class);

    /**
     * Rest template
     */
    private final RestTemplate restTemplate;

    /**
     * Host URI. Example: http://localhost:8081
     */
    private final String hostUri;

    /**
     * Maximal number of retries
     */
    private final int maxRetries;

    /**
     * Temporisation in ms betwenn 2 retries
     */
    private final int tempoRetryMs;

    /**
     * Constructor
     * 
     * @param restTemplate
     * @param category
     * @param hostUri
     * @param maxRetries
     * @param tempoRetryMs
     */
    public AppCatalogJobClient(
    		final RestTemplate restTemplate,
            final String hostUri, 
            final int maxRetries, 
            final int tempoRetryMs
    ) {
        this.restTemplate = restTemplate;
        this.hostUri = hostUri;
        this.maxRetries = maxRetries;
        this.tempoRetryMs = tempoRetryMs;
    }    

    /**
     * @return the hostUri
     */
    String getHostUri() {
        return hostUri;
    }

    /**
     * @return the maxRetries
     */
    int getMaxRetries() {
        return maxRetries;
    }

    /**
     * @return the tempoRetryMs
     */
    int getTempoRetryMs() {
        return tempoRetryMs;
    }

    /**
     * Wait or throw an error according the number of retries
     * 
     * @param retries
     * @param cause
     * @throws AbstractCodedException
     */
    private void waitOrThrow(final int retries,
            final AbstractCodedException cause, final String api)
            throws AbstractCodedException {
        LOGGER.debug(String.format("[api %s] %s Retry %d/%d", api,
                cause.getLogMessage(), retries, maxRetries));
        if (retries < maxRetries) {
            try {
                Thread.sleep(tempoRetryMs);
            } catch (final InterruptedException e) {
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
    public List<AppDataJob> search(final Map<String, String> filters) throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(hostUri + "/jobs/search");
            
            for (final Map.Entry<String, String> entry : filters.entrySet()) {
            	builder.queryParam(entry.getKey(), entry.getValue());
            }
            final URI uri = builder.build().toUri();
            LogUtils.traceLog(LOGGER, String.format("[uri %s]", uri));
            try {
                final ResponseEntity<List<AppDataJob>> response = restTemplate.exchange(
                		uri, 
                		HttpMethod.GET, 
                		null,
                		new ParameterizedTypeReference<List<AppDataJob>>() {}
                );             
                if (response.getStatusCode() == HttpStatus.OK) {
                    LogUtils.traceLog(LOGGER, String.format("[uri %s] [ret %s]", uri, response.getBody()));
                    return response.getBody();
                } else {
                    waitOrThrow(retries,
                            new AppCatalogJobSearchApiError(uri.toString(),
                                    "HTTP status code "
                                            + response.getStatusCode()),
                            "search");
                }
            } catch (final HttpStatusCodeException hsce) {
                waitOrThrow(retries, new AppCatalogJobSearchApiError(
                        uri.toString(),
                        String.format(
                                "HttpStatusCodeException occured: %s - %s",
                                hsce.getStatusCode(),
                                hsce.getResponseBodyAsString())),
                        "search");
            } catch (final RestClientException rce) {
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
    public List<AppDataJob> findByMessagesId(final long messageId)
            throws AbstractCodedException {   	
        return search(Collections.singletonMap("messages.id", Long.toString(messageId)));        
    }

    /**
     * Search by message identifier
     * 
     * @param messageId
     * @return
     * @throws AbstractCodedException
     */
    public List<AppDataJob> findByProductSessionId(final String sessionId)
            throws AbstractCodedException {
        return search(Collections.singletonMap("product.sessionId", sessionId));
    }

    /**
     * Search by product datatake identifier
     * 
     * @param messageId
     * @return
     * @throws AbstractCodedException
     */
    public List<AppDataJob> findByProductDataTakeId(final String dataTakeId)
            throws AbstractCodedException {
        return search(Collections.singletonMap("product.dataTakeId", dataTakeId));
    }

    /**
     * Search for job with generating tastables per pod and task table
     * 
     * @param pod
     * @param taskTable
     * @return
     * @throws AbstractCodedException
     */
    public List<AppDataJob> findJobInStateGeneratingForPod(final String taskTable, final String pod) 
    		throws AbstractCodedException {       
    	final Map<String, String> filters = new HashMap<>();  
    	filters.put("pod", pod);
        filters.put("state", AppDataJobState.GENERATING.name());
        filters.put("generations.state[neq]", AppDataJobGenerationState.SENT.name());
        filters.put("generations.taskTable", taskTable);
        filters.put("[orderByAsc]", "generations.lastUpdateDate");
        return search(filters);     
    }

    public AppDataJob newJob(final AppDataJob job) throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            final String uri = hostUri + "/jobs";
            LogUtils.traceLog(LOGGER, String.format("[uri %s]", uri));
            try {           	
                final ResponseEntity<AppDataJob> response = restTemplate.exchange(
                		uri, 
                		HttpMethod.POST,
                		new HttpEntity<AppDataJob>(job),
                		new ParameterizedTypeReference<AppDataJob>() {}
                );
                
                if (response.getStatusCode() == HttpStatus.OK) {
                    LogUtils.traceLog(LOGGER, String.format("[uri %s] [ret %s]",
                            uri, response.getBody()));
                    return response.getBody();
                } else {
                    waitOrThrow(retries, new AppCatalogJobNewApiError(uri, job,
                            "HTTP status code " + response.getStatusCode()),
                            "new");
                }
            } catch (final HttpStatusCodeException hsce) {
                waitOrThrow(retries,
                        new AppCatalogJobNewApiError(uri, job, String.format(
                                "HttpStatusCodeException occured: %s - %s",
                                hsce.getStatusCode(),
                                hsce.getResponseBodyAsString())),
                        "new");
            } catch (final RestClientException rce) {
                waitOrThrow(retries,
                        new AppCatalogJobNewApiError(uri, job,
                                String.format(
                                        "RestClientException occured: %s",
                                        rce.getMessage()),
                                rce),
                        "new");
            }
        }
    }

	public AppDataJob updateJob(final AppDataJob job) throws AbstractCodedException {  
        int retries = 0;
        while (true) {
            retries++;
            final String uri = hostUri + "/jobs/" + job.getId();
            LogUtils.traceLog(LOGGER, String.format("[uri %s]", uri));
            try {
                final ResponseEntity<AppDataJob> response = restTemplate.exchange(
                		uri, 
                		HttpMethod.PATCH,
                		new HttpEntity<AppDataJob>(job),
                		new ParameterizedTypeReference<AppDataJob>() {}
                );
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
            } catch (final HttpStatusCodeException hsce) {
                waitOrThrow(retries,
                        new AppCatalogJobPatchApiError(uri, job, String.format(
                                "HttpStatusCodeException occured: %s - %s",
                                hsce.getStatusCode(),
                                hsce.getResponseBodyAsString())),
                        "patch");
            } catch (final RestClientException rce) {
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
}
