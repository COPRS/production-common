package esa.s1pdgs.cpoc.mqi.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiStatusApiError;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiStopApiError;
import esa.s1pdgs.cpoc.mqi.model.rest.StatusDto;

/**
 * @author Viveris Technologies
 */
public class StatusService {

    /**
     * Logger
     */
    private static final Log LOGGER = LogFactory.getLog(StatusService.class);

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
    protected final int maxRetries;

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
    public StatusService(final RestTemplate restTemplate, final String hostUri,
            final int maxRetries, final int tempoRetryMs) {
        this.restTemplate = restTemplate;
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
    public StatusDto status() throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri = hostUri + "/mqi/status";
            try {
                ResponseEntity<StatusDto> response = restTemplate.exchange(uri,
                        HttpMethod.GET, null, StatusDto.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    return response.getBody();
                } else {
                    waitOrThrow(retries, new MqiStatusApiError(
                            "HTTP status code " + response.getStatusCode()),
                            "publish");
                }
            } catch (RestClientException rce) {
                waitOrThrow(retries, new MqiStatusApiError(
                        "RestClientException occurred: " + rce.getMessage(),
                        rce), "publish");
            }
        }
    }

    /**
     * Publish a message
     * 
     * @param message
     * @throws AbstractCodedException
     */
    public void stop() throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri = hostUri + "/mqi/stop";
            try {
                ResponseEntity<String> response = restTemplate.exchange(uri,
                        HttpMethod.POST, null, String.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    return;
                } else {
                    waitOrThrow(retries, new MqiStopApiError(
                            "HTTP status code " + response.getStatusCode()),
                            "publish");
                }
            } catch (RestClientException rce) {
                waitOrThrow(retries, new MqiStopApiError(
                        "RestClientException occurred: " + rce.getMessage(),
                        rce), "publish");
            }
        }
    }
}
