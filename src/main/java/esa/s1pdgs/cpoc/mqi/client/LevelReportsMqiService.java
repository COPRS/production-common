package esa.s1pdgs.cpoc.mqi.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class LevelReportsMqiService extends GenericMqiService<LevelProductDto> {

    public LevelReportsMqiService(RestTemplate restTemplate, String hostUri,
            int maxRetries, int tempoRetryMs) {
        super(restTemplate, ProductCategory.LEVEL_PRODUCTS, hostUri, maxRetries,
                tempoRetryMs);
    }

    @Override
    protected ResponseEntity<GenericMessageDto<LevelProductDto>> queryNext(
            String uri) {
        return restTemplate.exchange(uri, HttpMethod.GET, null,
                new ParameterizedTypeReference<GenericMessageDto<LevelProductDto>>() {
                });
    }

}
