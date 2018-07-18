package esa.s1pdgs.cpoc.mqi.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiAckApiError;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiNextApiError;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublishApiError;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;

/**
 * @author Viveris Technologies
 * @param <T>
 */
public abstract class GenericMqiService<T> {

    /**
     * Logger
     */
    private static final Log LOGGER =
            LogFactory.getLog(GenericMqiService.class);

    /**
     * Rest template
     */
    protected final RestTemplate restTemplate;

    /**
     * Product category
     */
    private final ProductCategory category;

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
    public GenericMqiService(final RestTemplate restTemplate,
            final ProductCategory category, final String hostUri,
            final int maxRetries, final int tempoRetryMs) {
        this.restTemplate = restTemplate;
        this.category = category;
        this.hostUri = hostUri;
        this.maxRetries = maxRetries;
        this.tempoRetryMs = tempoRetryMs;
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
     * Get the next message to proceed
     * 
     * @return
     * @throws AbstractCodedException
     */
    public GenericMessageDto<T> next() throws AbstractCodedException {
        int retries = -1;
        while (retries < maxRetries) {
            retries++;
            String uri =
                    hostUri + "/messages/" + category.name().toLowerCase() + "/next";
            try {
                ResponseEntity<GenericMessageDto<T>> response =
                        restTemplate.exchange(uri, HttpMethod.GET, null,
                                new ParameterizedTypeReference<GenericMessageDto<T>>() {
                                });
                if (response.getStatusCode() == HttpStatus.OK) {
                    return response.getBody();
                } else {
                    waitOrThrow(retries,
                            new MqiNextApiError(category,
                                    "HTTP status code "
                                            + response.getStatusCode()),
                            "next");
                }
            } catch (RestClientException rce) {
                waitOrThrow(retries, new MqiNextApiError(category,
                        "RestClientException occurred: " + rce.getMessage(),
                        rce), "next");
            }
        }
        throw new MqiNextApiError(category, "Timeout on query execution");
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
    public boolean ack(final AckMessageDto ack) throws AbstractCodedException {
        int retries = -1;
        while (retries < maxRetries) {
            retries++;
            String uri = hostUri + "/messages/" + category.name().toLowerCase() + "/ack";
            try {
                ResponseEntity<Boolean> response = restTemplate.exchange(uri,
                        HttpMethod.POST, new HttpEntity<AckMessageDto>(ack),
                        Boolean.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    Boolean ret = response.getBody();
                    if (ret == null) {
                        return false;
                    } else {
                        return ret.booleanValue();
                    }
                } else {
                    waitOrThrow(retries, new MqiAckApiError(category,
                            ack.getMessageId(),
                            ack.getAck().name() + " " + ack.getMessage(),
                            "HTTP status code " + response.getStatusCode()),
                            "ack");
                }
            } catch (RestClientException rce) {
                waitOrThrow(retries, new MqiAckApiError(category,
                        ack.getMessageId(),
                        ack.getAck().name() + " " + ack.getMessage(),
                        "RestClientException occurred: " + rce.getMessage(),
                        rce), "ack");
            }
        }
        throw new MqiAckApiError(category, ack.getMessageId(),
                ack.getAck().name() + " " + ack.getMessage(),
                "Timeout on query execution");
    }

    /**
     * Publish a message
     * 
     * @param message
     * @throws AbstractCodedException
     */
    public void publish(final GenericPublicationMessageDto<T> message)
            throws AbstractCodedException {
        int retries = -1;
        while (retries < maxRetries) {
            retries++;
            String uri = hostUri + "/messages/" + category.name().toLowerCase() + "/ack";
            try {
                ResponseEntity<Void> response =
                        restTemplate.exchange(uri, HttpMethod.POST,
                                new HttpEntity<GenericPublicationMessageDto<T>>(
                                        message),
                                Void.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    return;
                } else {
                    waitOrThrow(retries,
                            new MqiPublishApiError(category, message,
                                    "HTTP status code "
                                            + response.getStatusCode()),
                            "publish");
                }
            } catch (RestClientException rce) {
                waitOrThrow(retries, new MqiPublishApiError(category, message,
                        "RestClientException occurred: " + rce.getMessage(),
                        rce), "publish");
            }
        }
        throw new MqiPublishApiError(category, message,
                "Timeout on query execution");
    }
    
    protected abstract ResponseEntity<GenericMessageDto<T>> queryNext(String uri);
}
