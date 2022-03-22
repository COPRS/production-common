package esa.s1pdgs.cpoc.mqi.server.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.StatusProcessingApiError;

/**
 * Service to check if the other application deal with messages or not
 * 
 * @author Viveris Technologies
 */
public class OtherApplicationService {

    /**
     * Logger
     */
    private static final Log LOGGER =
            LogFactory.getLog(OtherApplicationService.class);

    /**
     * Rest template
     */
    protected final RestTemplate restTemplate;

    /**
     * Host URI for the applicative catalog
     */
    private final String portUri;

    /**
     * Maximal number of retries when query fails
     */
    private final int maxRetries;

    /**
     * Temporisation in ms between 2 retries
     */
    private final int tempoRetryMs;

    /**
     * 
     */
    private final String suffixUriOtherApp;

    /**
     * Constructor
     * 
     */
    public OtherApplicationService(final RestTemplate restTemplate,
            final String portUri, final int maxRetries,
            final int tempoRetryMs,final String suffixUriOtherApp) {
        this.restTemplate = restTemplate;
        this.portUri = portUri;
        if (maxRetries < 0 || maxRetries > 20) {
            this.maxRetries = 0;
        } else {
            this.maxRetries = maxRetries;
        }
        this.tempoRetryMs = tempoRetryMs;
        this.suffixUriOtherApp = suffixUriOtherApp;
    }

    /**
     * @return the portUri
     */
    public String getPortUri() {
        return portUri;
    }

    /**
     * @return the maxRetries
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * @return the tempoRetryMs
     */
    public int getTempoRetryMs() {
        return tempoRetryMs;
    }

    /**
     * Wait or throw an error according the number of retries
     * 
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
     * Check if the application is processing the message
     * 
     */
    public boolean isProcessing(final String podName,
            final ProductCategory category, final long messageId)
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri = "http://" + podName + suffixUriOtherApp + ":" + this.portUri
                    + "/app/" + category.name().toLowerCase() + "/process/"
                    + messageId;
            try {
                ResponseEntity<Boolean> response = restTemplate.exchange(uri,
                        HttpMethod.GET, null, Boolean.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    Boolean ret = response.getBody();
                    if (ret == null) {
                        return false;
                    } else {
                        return ret;
                    }
                } else {
                    waitOrThrow(retries,
                            new StatusProcessingApiError(uri,
                                    "HTTP status code "
                                            + response.getStatusCode()),
                            "isProcessing");
                }
            } catch (RestClientException rce) {
                waitOrThrow(retries,
                        new StatusProcessingApiError(uri,
                                "RestClientException occurred: "
                                        + rce.getMessage(),
                                rce),
                        "isProcessing");
            }
        }
    }

}
