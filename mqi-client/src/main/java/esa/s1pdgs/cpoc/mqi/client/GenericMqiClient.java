package esa.s1pdgs.cpoc.mqi.client;

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

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiAckApiError;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiNextApiError;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublishApiError;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;

/**
 * @author Viveris Technologies
 * @param <T>
 */
public class GenericMqiClient implements MqiClient {

    /**
     * Logger
     */
    protected static final Log LOGGER = LogFactory.getLog(GenericMqiClient.class);

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
    public GenericMqiClient(
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
    
    private final String publishUri(final ProductCategory category)
    {
    	return hostUri + "/messages/" + category.name().toLowerCase() + "/publish";
    }
    
    private final String ackUri(final ProductCategory category)
    {
    	return hostUri  + "/messages/" + category.name().toLowerCase() + "/ack";
    }
    
    private final String nextUri(final ProductCategory category)
    {
    	return hostUri  + "/messages/" + category.name().toLowerCase() + "/next";
    }

    /**
     * Wait or throw an error according the number of retries
     * 
     * @param retries
     * @param cause
     * @throws AbstractCodedException
     */
    protected void waitOrThrow(
    		final int retries,
            final AbstractCodedException cause, 
            final String api
    )
    	throws AbstractCodedException {
        LOGGER.debug(String.format("[api %s] %s Retry %d/%d", api, cause.getLogMessage(), retries, maxRetries));
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
    @Override
	public <T> GenericMessageDto<T> next(final ProductCategory category) throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            final String uri = nextUri(category);
            try {
            	final Class<T> clazz = category.getDtoClass();
            	final ResolvableType type = ResolvableType.forClassWithGenerics(
            			GenericMessageDto.class, 
            			clazz
            	);   
				final ResponseEntity<GenericMessageDto<T>> response = restTemplate.exchange(
                		uri, 
                		HttpMethod.GET, 
                		null, 
                		ParameterizedTypeReference.forType(type.getType())
                );
                if (response.getStatusCode() == HttpStatus.OK) {
                    return response.getBody();
                } 
                else {
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
    @Override
	public boolean ack(final AckMessageDto ack, final ProductCategory category) throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            final String uri = ackUri(category);
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
    @Override
	public <E extends AbstractMessage> void publish(final GenericPublicationMessageDto<E> message, final ProductCategory category)
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            final String uri = publishUri(category);
            LogUtils.traceLog(LOGGER,
                    String.format("[uri %s] [body %s]", uri, message));
            try {
                ResponseEntity<Void> response =
                        restTemplate.exchange(
                        		uri, 
                        		HttpMethod.POST,
                                new HttpEntity<GenericPublicationMessageDto<E>>(message),
                                Void.class
                );
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
            	LOGGER.error(rce);
                waitOrThrow(retries, new MqiPublishApiError(category, message,
                        "RestClientException occurred: " + rce.getMessage(),
                        rce), "publish");
            }
        }
    }
}
