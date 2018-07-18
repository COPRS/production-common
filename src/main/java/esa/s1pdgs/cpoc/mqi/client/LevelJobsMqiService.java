package esa.s1pdgs.cpoc.mqi.client;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiNextApiError;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class LevelJobsMqiService extends GenericMqiService<LevelJobDto> {

    class LevelJobsMessageDto extends GenericMessageDto<LevelJobDto> {
        public LevelJobsMessageDto() {
            super();
        }
    };

    public LevelJobsMqiService(RestTemplate restTemplate, String hostUri,
            int maxRetries, int tempoRetryMs) {
        super(restTemplate, ProductCategory.LEVEL_JOBS, hostUri, maxRetries,
                tempoRetryMs);
    }

    public GenericMessageDto<LevelJobDto> next() throws AbstractCodedException {
        int retries = -1;
        while (retries < maxRetries) {
            retries++;
            String uri = hostUri + "/messages/" + category.name().toLowerCase()
                    + "/next";
            try {
                ResponseEntity<LevelJobsMessageDto> response =
                        restTemplate.exchange(uri, HttpMethod.GET, null,
                                LevelJobsMessageDto.class);
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

}
