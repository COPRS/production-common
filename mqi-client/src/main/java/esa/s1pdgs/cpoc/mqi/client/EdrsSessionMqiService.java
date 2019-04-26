package esa.s1pdgs.cpoc.mqi.client;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiNextApiError;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.rest.EdrsSessionsMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 * Implementation of the GenericMqiService for the category EDRS_SESSIONS
 * 
 * @author Viveris Technologies
 */
public class EdrsSessionMqiService extends GenericMqiService<EdrsSessionDto> {

    /**
     * Constructor
     * 
     * @param restTemplate
     * @param hostUri
     * @param maxRetries
     * @param tempoRetryMs
     */
    public EdrsSessionMqiService(final RestTemplate restTemplate,
            final String hostUri, final int maxRetries,
            final int tempoRetryMs) {
        super(restTemplate, ProductCategory.EDRS_SESSIONS, hostUri, maxRetries,
                tempoRetryMs);
    }

    /**
     * @see GenericMqiService#next()
     */
    public GenericMessageDto<EdrsSessionDto> next()
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri = hostUri + "/messages/" + category.name().toLowerCase()
                    + "/next";
            try {
                ResponseEntity<EdrsSessionsMessageDto> response =
                        restTemplate.exchange(uri, HttpMethod.GET, null,
                                EdrsSessionsMessageDto.class);
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
    }

}
