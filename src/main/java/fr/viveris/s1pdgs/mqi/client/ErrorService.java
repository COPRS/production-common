package fr.viveris.s1pdgs.mqi.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import fr.viveris.s1pdgs.common.errors.AbstractCodedException;
import fr.viveris.s1pdgs.common.errors.mqi.MqiPublishErrorException;

/**
 * @author Viveris Technologies
 * @param <T>
 */
public class ErrorService {

    /**
     * Logger
     */
    private static final Log LOGGER = LogFactory.getLog(ErrorService.class);

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
    public ErrorService(final RestTemplate restTemplate, final String hostUri,
            final int maxRetries, final int tempoRetryMs) {
        this.restTemplate = restTemplate;
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
     * Publish a message
     * 
     * @param message
     * @throws AbstractCodedException
     */
    public void publish(final String message) throws AbstractCodedException {
        int retries = -1;
        while (retries < maxRetries) {
            retries++;
            String uri = hostUri + "/errors/publish";
            try {
                ResponseEntity<Void> response =
                        restTemplate.exchange(uri, HttpMethod.POST,
                                new HttpEntity<String>(message), Void.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    return;
                } else {
                    waitOrThrow(retries,
                            new MqiPublishErrorException(message,
                                    "HTTP status code "
                                            + response.getStatusCode()),
                            "publish");
                }
            } catch (RestClientException rce) {
                waitOrThrow(retries, new MqiPublishErrorException(message,
                        "RestClientException occurred: " + rce.getMessage(),
                        rce), "publish");
            }
        }
        throw new MqiPublishErrorException(message,
                "Timeout on query execution");
    }
}
