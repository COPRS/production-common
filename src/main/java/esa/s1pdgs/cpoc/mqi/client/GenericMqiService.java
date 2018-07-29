package esa.s1pdgs.cpoc.mqi.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiAckApiError;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublishApiError;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
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
    protected static final Log LOGGER =
            LogFactory.getLog(GenericMqiService.class);

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
    public GenericMqiService(final RestTemplate restTemplate,
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
     * Get the next message to proceed
     * 
     * @return
     * @throws AbstractCodedException
     */
    public abstract GenericMessageDto<T> next() throws AbstractCodedException;

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
        int retries = 0;
        while (true) {
            retries++;
            String uri = hostUri + "/messages/" + category.name().toLowerCase()
                    + "/ack";
            LogUtils.traceLog(LOGGER,
                    String.format("[uri %s] [body %s]", uri, ack));
            try {
                ResponseEntity<Boolean> response = restTemplate.exchange(uri,
                        HttpMethod.POST, new HttpEntity<AckMessageDto>(ack),
                        Boolean.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    Boolean ret = response.getBody();
                    LogUtils.traceLog(LOGGER, String.format(
                            "[uri %s] [body %s] [ret %s]", uri, ack, ret));
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
    }

    /**
     * Publish a message
     * 
     * @param message
     * @throws AbstractCodedException
     */
    public void publish(final GenericPublicationMessageDto<T> message)
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri = hostUri + "/messages/" + category.name().toLowerCase()
                    + "/publish";
            LogUtils.traceLog(LOGGER,
                    String.format("[uri %s] [body %s]", uri, message));
            try {
                ResponseEntity<Void> response =
                        restTemplate.exchange(uri, HttpMethod.POST,
                                new HttpEntity<GenericPublicationMessageDto<T>>(
                                        message),
                                Void.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    LogUtils.traceLog(LOGGER, String.format(
                            "[uri %s] [body %s] [ret OK]", uri, message));
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
    }
}
