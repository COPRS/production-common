package esa.s1pdgs.cpoc.mqi.client;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiNextApiError;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.LevelReportsMessageDto;

/**
 * Implementation of the GenericMqiService for the category LEVEL_REPORTS
 * 
 * @author Viveris Technologies
 */
public class LevelReportsMqiService extends GenericMqiService<LevelReportDto> {

    /**
     * Constructor
     * 
     * @param restTemplate
     * @param hostUri
     * @param maxRetries
     * @param tempoRetryMs
     */
    public LevelReportsMqiService(final RestTemplate restTemplate,
            final String hostUri, final int maxRetries,
            final int tempoRetryMs) {
        super(restTemplate, ProductCategory.LEVEL_REPORTS, hostUri, maxRetries,
                tempoRetryMs);
    }

    /**
     * @see GenericMqiService#next()
     */
    public GenericMessageDto<LevelReportDto> next()
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri = hostUri + "/messages/" + category.name().toLowerCase()
                    + "/next";
            try {
                ResponseEntity<LevelReportsMessageDto> response =
                        restTemplate.exchange(uri, HttpMethod.GET, null,
                                LevelReportsMessageDto.class);
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
