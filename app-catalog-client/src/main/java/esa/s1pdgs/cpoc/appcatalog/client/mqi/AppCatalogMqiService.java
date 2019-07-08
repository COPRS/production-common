package esa.s1pdgs.cpoc.appcatalog.client.mqi;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import esa.s1pdgs.cpoc.appcatalog.rest.AppCatMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatReadMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatSendMessageDto;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiAckApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiGetApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiGetNbReadingApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiGetOffsetApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiNextApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiReadApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiSendApiError;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;

/**
 * @author Viveris Technologies
 * @param <T>
 */
public class AppCatalogMqiService {
    /**
     * Logger
     */
    static final Log LOGGER = LogFactory.getLog(AppCatalogMqiService.class);

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
    public AppCatalogMqiService(final RestTemplate restTemplate,
            final String hostUri,
            final int maxRetries, final int tempoRetryMs) {
        this.restTemplate = restTemplate;
        this.hostUri = hostUri;
        this.maxRetries = maxRetries;
        this.tempoRetryMs = tempoRetryMs;
    }
    
    static final <T> ParameterizedTypeReference<T> forCategory(final ProductCategory category)
    {
    	final ResolvableType appCatMessageType = ResolvableType.forClassWithGenerics(
    			AppCatMessageDto.class, 
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
     * Wait or throw an error according the number of retries
     * 
     * @param retries
     * @param cause
     * @throws AbstractCodedException
     */
    private final void waitOrThrow(final int retries,
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
     * Inform that a consumer is reading a message.<br/>
     * Must not return null (throw an exception)
     * 
     * @param topic
     * @param partition
     * @param offset
     * @param body
     * @return
     * @throws AbstractCodedException
     */
	@SuppressWarnings("unchecked")
	public AppCatMessageDto<? extends AbstractDto> read(
    		final ProductCategory category,
    		final String topic, 
    		final int partition,
            final long offset, 
            final AppCatReadMessageDto<?> body
    )
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri = hostUri + "/mqi/" + category.name().toLowerCase() + "/"
                    + topic + "/" + partition + "/" + offset + "/read";
            LogUtils.traceLog(LOGGER,
                    String.format("[uri %s] [body %s]", uri, body));
            try {
                @SuppressWarnings("rawtypes")
				ResponseEntity<AppCatMessageDto> response =
                        restTemplate.exchange(uri, HttpMethod.POST,
                                new HttpEntity<AppCatReadMessageDto<?>>(body),
                                AppCatMessageDto.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    if (response.getBody() == null) {
                        waitOrThrow(
                                retries, new AppCatalogMqiReadApiError(category,
                                        uri, body, "Null return object"),
                                "read");
                    } else {
                        LogUtils.traceLog(LOGGER,
                                String.format("[uri %s] [body %s] [ret %s]",
                                        uri, body, response.getBody()));
                        return response.getBody();
                    }
                } else {
                    waitOrThrow(retries,
                            new AppCatalogMqiReadApiError(category, uri, body,
                                    "HTTP status code "
                                            + response.getStatusCode()),
                            "read");
                }
            } catch (RestClientException rce) {
                waitOrThrow(retries, new AppCatalogMqiReadApiError(category,
                        uri, body,
                        "RestClientException occurred: " + rce.getMessage(),
                        rce), "read");
            }
        }
    }

    /**
     * Publish a message
     * 
     * @param message
     * @throws AbstractCodedException
     */
    public boolean send(final ProductCategory category, final long messageId, final AppCatSendMessageDto body)
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri = hostUri + "/mqi/" + category.name().toLowerCase() + "/"
                    + messageId + "/send";
            try {
                ResponseEntity<Boolean> response = restTemplate.exchange(uri,
                        HttpMethod.POST,
                        new HttpEntity<AppCatSendMessageDto>(body), Boolean.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    Boolean ret = response.getBody();
                    LogUtils.traceLog(LOGGER,
                            String.format("[uri %s] [body %s] [ret %s]",
                                    uri, body, ret));
                    if (ret == null) {
                        return false;
                    } else {
                        return ret.booleanValue();
                    }
                } else {
                    waitOrThrow(retries,
                            new AppCatalogMqiSendApiError(category, uri, body,
                                    "HTTP status code "
                                            + response.getStatusCode()),
                            "send");
                }
            } catch (RestClientException rce) {
                waitOrThrow(retries, new AppCatalogMqiSendApiError(category,
                        uri, body,
                        "RestClientException occurred: " + rce.getMessage(),
                        rce), "send");
            }
        }
    }

    /**
     * Ack a message
     * 
     * @param identifier
     * @param ack
     * @param message
     * @return
     * @throws AbstractCodedException
     */
    public boolean ack(final ProductCategory category, final long messageId, final Ack ack)
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri = hostUri + "/mqi/" + category.name().toLowerCase() + "/"
                    + messageId + "/ack";
            LogUtils.traceLog(LOGGER,
                    String.format("[uri %s] [body %s]", uri, ack));
            try {
                ResponseEntity<Boolean> response =
                        restTemplate.exchange(uri, HttpMethod.POST,
                                new HttpEntity<Ack>(ack), Boolean.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    Boolean ret = response.getBody();
                    LogUtils.traceLog(LOGGER,
                            String.format("[uri %s] [body %s] [ret %s]",
                                    uri, ack, ret));
                    if (ret == null) {
                        return false;
                    } else {
                        return ret.booleanValue();
                    }
                } else {
                    waitOrThrow(retries, new AppCatalogMqiAckApiError(category,
                            uri, ack,
                            "HTTP status code " + response.getStatusCode()),
                            "ack");
                }
            } catch (RestClientException rce) {
                waitOrThrow(retries, new AppCatalogMqiAckApiError(category, uri,
                        ack,
                        "RestClientException occurred: " + rce.getMessage(),
                        rce), "ack");
            }
        }
    }

    /**
     * Publish a message
     * 
     * @param message
     * @throws AbstractCodedException
     */
    public long getEarliestOffset(final String topic, final int partition,
            final String group) throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            // TODO use URI builder
            String uriStr = hostUri + "/mqi/" + topic + "/" + partition + "/earliestOffset";
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(uriStr).queryParam("group", group);
            URI uri = builder.build().toUri();
            LogUtils.traceLog(LOGGER,
                    String.format("[uri %s]", uri));
            try {
                ResponseEntity<Long> response = restTemplate.exchange(uri,
                        HttpMethod.GET, null, Long.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    Long ret = response.getBody();
                    LogUtils.traceLog(LOGGER,
                            String.format("[uri %s] [ret %s]",
                                    uri, response.getBody()));
                    if (ret == null) {
                        // TODO default value
                        return 0;
                    } else {
                        return ret.longValue();
                    }
                } else {
                    waitOrThrow(retries,
                            new AppCatalogMqiGetOffsetApiError(
                                    uri.toString(),
                                    "HTTP status code "
                                            + response.getStatusCode()),
                            "send");
                }
            } catch (RestClientException rce) {
                waitOrThrow(retries, new AppCatalogMqiGetOffsetApiError(
                     uri.toString(),
                        "RestClientException occurred: " + rce.getMessage(),
                        rce), "send");
            }
        }
    }

    /**
     *
     */
    public int getNbReadingMessages(final String topic, final String podName)
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uriStr = hostUri + "/mqi/" + topic + "/nbReading";
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(uriStr).queryParam("pod", podName);
            URI uri = builder.build().toUri();
            LogUtils.traceLog(LOGGER,
                    String.format("[uri %s]", uri));
            try {
                ResponseEntity<Integer> response = restTemplate.exchange(uri,
                        HttpMethod.GET, null, Integer.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    Integer ret = response.getBody();
                    LogUtils.traceLog(LOGGER,
                            String.format("[uri %s] [ret %s]",
                                    uri, ret));
                    if (ret == null) {
                        return 0;
                    } else {
                        return ret.intValue();
                    }
                } else {
                    waitOrThrow(retries,
                            new AppCatalogMqiGetNbReadingApiError(
                                    uri.toString(),
                                    "HTTP status code "
                                            + response.getStatusCode()),
                            "getNbReadingMessages");
                }
            } catch (RestClientException rce) {
                waitOrThrow(retries,
                        new AppCatalogMqiGetNbReadingApiError(
                                uri.toString(),

                                "RestClientException occurred: "
                                        + rce.getMessage(),
                                rce),
                        "getNbReadingMessages");
            }
        }
    }

    public List<AppCatMessageDto<? extends AbstractDto>> next(final ProductCategory category, String podName)
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uriStr =
                    hostUri + "/mqi/" + category.name().toLowerCase() + "/next";
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(uriStr).queryParam("pod", podName);
            URI uri = builder.build().toUri();
            try {
    	
                final ResponseEntity<List<AppCatMessageDto<? extends AbstractDto>>> response =
                        restTemplate.exchange(
                        		uri, 
                        		HttpMethod.GET, 
                        		null, 
                        		forCategory(category)
                );
                if (response.getStatusCode() == HttpStatus.OK) {
                    List<AppCatMessageDto<? extends AbstractDto>> body = response.getBody();
                    if (body == null) {
                        return new ArrayList<>();
                    } else {
                    	List<AppCatMessageDto<? extends AbstractDto>> ret = new ArrayList<AppCatMessageDto<? extends AbstractDto>>();
                        ret.addAll(body);
                        return ret;
                    }
                } else {
                    waitOrThrow(retries,
                            new AppCatalogMqiNextApiError(category,
                                    "HTTP status code "
                                            + response.getStatusCode()),
                            "next");
                }
            } catch (RestClientException rce) {
                waitOrThrow(retries, new AppCatalogMqiNextApiError(category,
                        "RestClientException occurred: " + rce.getMessage(),
                        rce), "next");
            }
        }
    }

    public AppCatMessageDto<?> get(final ProductCategory category, final long messageId)
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri = hostUri + "/mqi/" + category.name().toLowerCase() + "/"
                    + messageId;
            LogUtils.traceLog(LOGGER, String.format("[uri %s]", uri));
            try {
                @SuppressWarnings("rawtypes")
				ResponseEntity<AppCatMessageDto> response =
                        restTemplate.exchange(uri, HttpMethod.GET, null,
                        		AppCatMessageDto.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    LogUtils.traceLog(LOGGER, String.format("[uri %s] [ret %s]",
                            uri, response.getBody()));
                    return response.getBody();
                } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                    LogUtils.traceLog(LOGGER,
                            String.format("[uri %s] [ret NOT_FOUND]", uri));
                    throw new AppCatalogMqiGetApiError(category, uri,
                            "Message not found");
                } else {
                    waitOrThrow(retries, new AppCatalogMqiGetApiError(category,
                            uri,
                            "HTTP status code " + response.getStatusCode()),
                            "ack");
                }
            } catch (RestClientException rce) {
                waitOrThrow(retries, new AppCatalogMqiGetApiError(category, uri,

                        "RestClientException occurred: " + rce.getMessage(),
                        rce), "ack");
            }
        }
    }
}
