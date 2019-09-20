package esa.s1pdgs.cpoc.appcatalog.client.job;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogJobNewApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogJobPatchApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogJobPatchGenerationApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogJobSearchApiError;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;

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
     * Product category
     */
    private final ProductCategory category;

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
            final int tempoRetryMs,
            final ProductCategory category
    ) {
        this.restTemplate = restTemplate;
        this.hostUri = hostUri;
        this.maxRetries = maxRetries;
        this.tempoRetryMs = tempoRetryMs;
        this.category = category;
    }    
    
	static final <T> ParameterizedTypeReference<T> forCategory(final ProductCategory category)
	{
		final ResolvableType appCatMessageType = ResolvableType.forClass(
				AppDataJob.class, 
				category.getDtoClass()
		);   
		
		final ResolvableType type = ResolvableType.forClassWithGenerics(
				List.class, 
				appCatMessageType
		);   
		return ParameterizedTypeReference.forType(type.getType());
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
     * @return the category
     */
    ProductCategory getCategory() {
        return category;
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
    public List<AppDataJob<?>> search(final Map<String, String> filters)
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(
            		hostUri + "/" + category.name().toLowerCase() + "/jobs/search"
            );
            
            for (final Map.Entry<String, String> entry : filters.entrySet()) {
            	builder.queryParam(entry.getKey(), entry.getValue());
            }
            final URI uri = builder.build().toUri();
            LogUtils.traceLog(LOGGER, String.format("[uri %s]", uri));
            try {
                final ResponseEntity<List<AppDataJob<?>>> response = restTemplate.exchange(
                		uri, 
                		HttpMethod.GET, 
                		null,
                		forCategory(category)
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
    public List<AppDataJob<?>> findByMessagesIdentifier(final long messageId)
            throws AbstractCodedException {   	
        return search(Collections.singletonMap("messages.identifier", Long.toString(messageId)));        
    }

    /**
     * Search by message identifier
     * 
     * @param messageId
     * @return
     * @throws AbstractCodedException
     */
    public List<AppDataJob<?>> findByProductSessionId(final String sessionId)
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
    public List<AppDataJob<?>> findByProductDataTakeId(final String dataTakeId)
            throws AbstractCodedException {
        return search(Collections.singletonMap("product.dataTakeId", dataTakeId));
    }

    /**
     * Search by pod and state
     * 
     * @param pod
     * @param state
     * @return
     * @throws AbstractCodedException
     */
    public List<AppDataJob<?>> findByPodAndState(final String pod,
            final AppDataJobState state) throws AbstractCodedException {
    	final Map<String, String> filters = new HashMap<>();
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
    public List<AppDataJob<?>> findNByPodAndGenerationTaskTableWithNotSentGeneration(
            final String pod, final String taskTable)
            throws AbstractCodedException {       
    	final Map<String, String> filters = new HashMap<>();  
        filters.put("pod", pod);
        filters.put("generations.state[neq]", AppDataJobGenerationState.SENT.name());
        filters.put("generations.taskTable", taskTable);
        filters.put("[orderByAsc]", "generations.lastUpdateDate");
        return search(filters);     
    }

    /**
     * Create a new job from its identifier
     * 
     * @param identifier
     * @return
     * @throws AbstractCodedException
     */
//    public <E extends AbstractDto> AppDataJob<E> newJob(final AppDataJob<E> job)
//            throws AbstractCodedException {
//        int retries = 0;
//        while (true) {
//            retries++;
//            String uri = hostUri + "/" + category.name().toLowerCase() + "/jobs";
//            LogUtils.traceLog(LOGGER, String.format("[uri %s]", uri));
//            try {
//        		final ResolvableType appCatMessageType = ResolvableType.forClassWithGenerics(AppDataJob.class, category.getDtoClass());               	
//                final ResponseEntity<AppDataJob<E>> response = restTemplate.exchange(
//                		uri, 
//                		HttpMethod.POST,
//                		new HttpEntity<AppDataJob<E>>(job),
//                		ParameterizedTypeReference.forType(appCatMessageType.getType())
//                );
//                
//                if (response.getStatusCode() == HttpStatus.OK) {
//                    LogUtils.traceLog(LOGGER, String.format("[uri %s] [ret %s]",
//                            uri, response.getBody()));
//                    return response.getBody();
//                } else {
//                    waitOrThrow(retries, new AppCatalogJobNewApiError(uri, job,
//                            "HTTP status code " + response.getStatusCode()),
//                            "new");
//                }
//            } catch (HttpStatusCodeException hsce) {
//                waitOrThrow(retries,
//                        new AppCatalogJobNewApiError(uri, job, String.format(
//                                "HttpStatusCodeException occured: %s - %s",
//                                hsce.getStatusCode(),
//                                hsce.getResponseBodyAsString())),
//                        "new");
//            } catch (RestClientException rce) {
//                waitOrThrow(retries,
//                        new AppCatalogJobNewApiError(uri, job,
//                                String.format(
//                                        "HttpStatusCodeException occured: %s",
//                                        rce.getMessage()),
//                                rce),
//                        "new");
//            }
//        }
//    }
    
    public <E extends AbstractDto> AppDataJob<E> newJob(final AppDataJob<E> job)
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri = hostUri + "/" + category.name().toLowerCase() + "/jobs";
            LogUtils.traceLog(LOGGER, String.format("[uri %s]", uri));
            try {
        		final ResponseEntity<JsonNode> response = restTemplate.exchange(
                		uri, 
                		HttpMethod.POST,
                		new HttpEntity<AppDataJob<E>>(job),
                		JsonNode.class
                );
        		
                if (response.getStatusCode() == HttpStatus.OK) {
                    LogUtils.traceLog(LOGGER, String.format("[uri %s] [ret %s]",
                            uri, response.getBody()));
                    
                	final ObjectMapper objMapper = new ObjectMapper();
                	final TypeFactory typeFactory = objMapper.getTypeFactory();
                	final JavaType javaType = typeFactory.constructParametricType(
                			AppDataJob.class, 
                			category.getDtoClass()
                	);            		
                	return objMapper
                			.readValue(objMapper.treeAsTokens(response.getBody()), javaType);
                	
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
            } catch (IOException | RestClientException rce) {
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

    @SuppressWarnings("unchecked")
	public <E extends AbstractDto> AppDataJob<E> patchJob(final long identifier,
            final AppDataJob<?> job, final boolean patchMessages,
            final boolean patchProduct, final boolean patchGenerations)
            throws AbstractCodedException {
    	job.setIdentifier(identifier);
        if (!patchMessages) {
        	job.setMessages(new ArrayList<>());
        }
        if (!patchProduct) {
        	job.setProduct(null);
        }
        if (!patchGenerations) {
        	job.setGenerations(new ArrayList<>());
        }        
        int retries = 0;
        while (true) {
            retries++;
            String uri = hostUri + "/" + category.name().toLowerCase() + "/jobs/" + identifier;
            LogUtils.traceLog(LOGGER, String.format("[uri %s]", uri));
            try {
            	final ResolvableType appCatMessageType = ResolvableType.forClassWithGenerics(
        				AppDataJob.class, 
        				category.getDtoClass()
        		);  
            	
                final ResponseEntity<AppDataJob<?>> response = restTemplate.exchange(
                		uri, 
                		HttpMethod.PATCH,
                		new HttpEntity<AppDataJob<?>>(job),
                		ParameterizedTypeReference.forType(appCatMessageType.getType())
                );
                if (response.getStatusCode() == HttpStatus.OK) {
                    LogUtils.traceLog(LOGGER, String.format("[uri %s] [ret %s]",
                            uri, response.getBody()));
                    return (AppDataJob<E>) response.getBody();
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
     * Patch a generation of a given job
     * 
     * @param identifier
     * @return
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
	public <E extends AbstractDto> AppDataJob<E> patchTaskTableOfJob(final long identifier,
            final String taskTable, final AppDataJobGenerationState state)
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri = hostUri + "/" + category.name().toLowerCase() + "/jobs/" + identifier
                    + "/generations/" + taskTable;
            AppDataJobGeneration body = new AppDataJobGeneration();
            body.setTaskTable(taskTable);
            body.setState(state);
            LogUtils.traceLog(LOGGER, String.format("[uri %s]", uri));
            try {
            	final ResolvableType appCatMessageType = ResolvableType.forClassWithGenerics(
        				AppDataJob.class, 
        				category.getDtoClass()
        		);              	
                final ResponseEntity<AppDataJob<?>> response = restTemplate.exchange(
                		uri, 
                		HttpMethod.PATCH,
                		new HttpEntity<AppDataJobGeneration>(body),
                		ParameterizedTypeReference.forType(appCatMessageType.getType())
                );
                if (response.getStatusCode() == HttpStatus.OK) {
                    LogUtils.traceLog(LOGGER, String.format("[uri %s] [ret %s]",
                            uri, response.getBody()));
                    return (AppDataJob<E>) response.getBody();
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
}
