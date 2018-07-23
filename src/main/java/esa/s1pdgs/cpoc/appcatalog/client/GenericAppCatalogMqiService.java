package esa.s1pdgs.cpoc.appcatalog.client;

import java.net.URI;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import esa.s1pdgs.cpoc.appcatalog.rest.MqiGenericMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiGenericReadMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiLightMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiSendMessageDto;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiGetOffsetApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiReadApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiSendApiError;

/**
 * @author Viveris Technologies
 * @param <T>
 */
public abstract class GenericAppCatalogMqiService<T> {

    /**
     * Logger
     */
    protected static final Log LOGGER =
            LogFactory.getLog(GenericAppCatalogMqiService.class);

    /**
     * Rest template
     */
    protected final RestTemplate restTemplate;

    /**
     * Product category
     */
    protected final ProductCategory category;

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
     * Constructor
     * 
     * @param restTemplate
     * @param category
     * @param hostUri
     * @param maxRetries
     * @param tempoRetryMs
     */
    public GenericAppCatalogMqiService(final RestTemplate restTemplate,
            final ProductCategory category, final String hostUri,
            final int maxRetries, final int tempoRetryMs) {
        this.restTemplate = restTemplate;
        this.category = category;
        this.hostUri = hostUri;
        if (maxRetries < 0 || maxRetries > 20) {
            this.maxRetries = 0;
        } else {
            this.maxRetries = maxRetries;
        }
        this.tempoRetryMs = tempoRetryMs;
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
    public MqiLightMessageDto read(final String topic, final int partition,
            final long offset, final MqiGenericReadMessageDto<T> body)
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri = hostUri + "/mqi/" + category.name().toLowerCase() + "/"
                    + topic + "/" + partition + "/" + offset + "/read";
            try {
                ResponseEntity<MqiLightMessageDto> response =
                        restTemplate.exchange(uri, HttpMethod.POST,
                                new HttpEntity<MqiGenericReadMessageDto<T>>(
                                        body),
                                MqiLightMessageDto.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    if (response.getBody() == null) {
                        waitOrThrow(
                                retries, new AppCatalogMqiReadApiError(category,
                                        uri, body, "Null return object"),
                                "read");
                    } else {
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
     * Get the next message to proceed
     * 
     * @return
     * @throws AbstractCodedException
     */
    public abstract List<MqiGenericMessageDto<T>> next()
            throws AbstractCodedException;

    /**
     * Publish a message
     * 
     * @param message
     * @throws AbstractCodedException
     */
    public boolean send(final long messageId, final MqiSendMessageDto body)
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri = hostUri + "/mqi/" + category.name().toLowerCase() + "/"
                    + messageId + "/send";
            try {
                ResponseEntity<Boolean> response = restTemplate.exchange(uri,
                        HttpMethod.POST,
                        new HttpEntity<MqiSendMessageDto>(body), Boolean.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    Boolean ret = response.getBody();
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
    public abstract MqiGenericMessageDto<T> ack(final long messageId)
            throws AbstractCodedException;

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
            String uriStr = hostUri + "/mqi/" + category.name().toLowerCase()
                    + "/" + topic + "/" + partition + "/earliestOffset";
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(uriStr).queryParam("group", group);
            URI uri = builder.build().toUri();
            try {
                ResponseEntity<Long> response = restTemplate.exchange(uri,
                        HttpMethod.GET, null, Long.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    Long ret = response.getBody();
                    if (ret == null) {
                        //TODO default value
                        return 0;
                    } else {
                        return ret.longValue();
                    }
                } else {
                    waitOrThrow(retries,
                            new AppCatalogMqiGetOffsetApiError(category,
                                    uri.toString(),
                                    "HTTP status code "
                                            + response.getStatusCode()),
                            "send");
                }
            } catch (RestClientException rce) {
                waitOrThrow(retries, new AppCatalogMqiGetOffsetApiError(
                        category, uri.toString(),
                        "RestClientException occurred: " + rce.getMessage(),
                        rce), "send");
            }
        }
    }
}
