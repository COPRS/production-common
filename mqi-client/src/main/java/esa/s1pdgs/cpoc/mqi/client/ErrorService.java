package esa.s1pdgs.cpoc.mqi.client;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiNextApiError;
import esa.s1pdgs.cpoc.mqi.model.queue.ErrorDto;
import esa.s1pdgs.cpoc.mqi.model.rest.ErrorsMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 * @author Viveris Technologies
 */
public class ErrorService extends GenericMqiService<ErrorDto> {

    /**
     * Constructor
     * 
     * @param restTemplate
     * @param category
     * @param hostUri
     * @param maxRetries
     * @param tempoRetryMs
     */
    public ErrorService(final RestTemplate restTemplate,
            final String hostUri, final int maxRetries,
            final int tempoRetryMs) {
        super(restTemplate, ProductCategory.ERRORS, hostUri,
        		"/" + ProductCategory.ERRORS.name().toLowerCase() + "/ack",
        		"/" + ProductCategory.ERRORS.name().toLowerCase() + "/publish",
        		maxRetries, tempoRetryMs);
    }
    
    /**
     * Get the next message to proceed
     * 
     * @return
     * @throws AbstractCodedException
     */
    public GenericMessageDto<ErrorDto> next() throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri = hostUri + "/" + category.name().toLowerCase() + "/next";
            try {
                ResponseEntity<ErrorsMessageDto> response =
                        restTemplate.exchange(uri, HttpMethod.GET, null,
                                ErrorsMessageDto.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    return response.getBody();
                } else {
                    waitOrThrow(retries,
                            new MqiNextApiError(null,
                                    "HTTP status code "
                                            + response.getStatusCode()),
                            "next");
                }
            } catch (RestClientException rce) {
                waitOrThrow(retries, new MqiNextApiError(null,
                        "RestClientException occurred: " + rce.getMessage(),
                        rce), "next");
            }
        }
    }

}
