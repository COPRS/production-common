package esa.s1pdgs.cpoc.appcatalog.client.job;

import java.util.List;

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

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
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
	@FunctionalInterface
	static interface RestCommand<E> {
		E execute() throws HttpStatusCodeException, RestClientException, AbstractCodedException;
	}
	

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
        
    public final AppDataJob findById(final long jobId) throws AbstractCodedException {
        final String uri = hostUri + "/jobs/" + Long.toString(jobId);
        
        return performWithRetries(
        		"get", 
        		uri, 
        		() -> restTemplate.exchange(
                    		uri, 
                    		HttpMethod.GET, 
                    		null,
                    		new ParameterizedTypeReference<AppDataJob>() {}
                )        		
        );
    }

    public final List<AppDataJob> findByMessagesId(final long messageId)
            throws AbstractCodedException {
    	return findAppDataJobsBy("findByMessagesId", Long.toString(messageId));
    }

    public final List<AppDataJob> findByProductSessionId(final String sessionId)
            throws AbstractCodedException {
    	return findAppDataJobsBy("findByProductSessionId", sessionId);
    }

    public final List<AppDataJob> findByProductDataTakeId(final String dataTakeId)
            throws AbstractCodedException {
    	return findAppDataJobsBy("findByProductDataTakeId", dataTakeId);
    }

    public final List<AppDataJob> findJobInStateGenerating(final String taskTable) 
    		throws AbstractCodedException {
    	return findAppDataJobsBy("findJobInStateGenerating", taskTable);
    }

    public final AppDataJob newJob(final AppDataJob job) throws AbstractCodedException {
        final String uri = hostUri + "/jobs";
        LogUtils.traceLog(LOGGER, String.format("[uri %s]", uri));
        
        return performWithRetries(
        		"new", 
        		uri, 
        		() -> restTemplate.exchange(
                		uri, 
                		HttpMethod.POST,
                		new HttpEntity<AppDataJob>(job),
                		new ParameterizedTypeReference<AppDataJob>() {}
                )        		
        );
    }

	public AppDataJob updateJob(final AppDataJob job) throws AbstractCodedException {  		
        final String uri = hostUri + "/jobs/" + job.getId();
        LogUtils.traceLog(LOGGER, String.format("[uri %s]", uri));
        
        return performWithRetries(
        		"patch", 
        		uri, 
        		() -> restTemplate.exchange(
                		uri, 
                		HttpMethod.PATCH,
                		new HttpEntity<AppDataJob>(job),
                		new ParameterizedTypeReference<AppDataJob>() {}
                )        		
        );
    }
	
	public void deleteJob(final AppDataJob job) throws AbstractCodedException {  
		final String uri = hostUri + "/jobs/" + job.getId();
		performWithRetries(
				"delete", 
				uri, 
				() -> {
					restTemplate.delete(uri);
					return null;
				}
		);  
	}
	
    private final List<AppDataJob> findAppDataJobsBy(final String apiMethod, final String apiParameterValue) throws AbstractCodedException {        
        final String uri = hostUri + "/jobs/" + apiMethod + "/" + apiParameterValue;
        LogUtils.traceLog(LOGGER, String.format("[uri %s]", uri));
        
        return performWithRetries(
        		apiMethod, 
        		uri, 
        		() -> restTemplate.exchange(
                    		uri, 
                    		HttpMethod.GET, 
                    		null,
                    		new ParameterizedTypeReference<List<AppDataJob>>() {}
                )        		
        );
    }
	
    private final <E> E performWithRetries(
    		final String name,
    		final String uri,
    		final RestCommand<ResponseEntity<E>> command
    		
    ) throws AbstractCodedException {
        int retries = 0;
        
        while (true) {
            retries++;
            try {
                final ResponseEntity<E> response = command.execute();
            
                if (response.getStatusCode() == HttpStatus.OK) {
                    LogUtils.traceLog(LOGGER, String.format("[uri %s] [ret %s]", uri, response.getBody()));
                    return response.getBody();
                } else {
                    waitOrThrow(retries,
                            new AppCatalogJobSearchApiError(uri,
                                    "HTTP status code "
                                            + response.getStatusCode()),
                            name);
                }
            } catch (final HttpStatusCodeException hsce) {
                waitOrThrow(retries, new AppCatalogJobSearchApiError(
                        uri,
                        String.format(
                                "HttpStatusCodeException occured: %s - %s",
                                hsce.getStatusCode(),
                                hsce.getResponseBodyAsString())),
                		name);
            } catch (final RestClientException rce) {
                waitOrThrow(retries,
                        new AppCatalogJobSearchApiError(uri,
                                String.format(
                                        "RestClientException occured: %s",
                                        rce.getMessage()),
                                rce),
                        name);
            }
        }
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
    
}
