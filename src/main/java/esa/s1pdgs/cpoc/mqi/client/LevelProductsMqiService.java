package esa.s1pdgs.cpoc.mqi.client;

import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;

public class LevelProductsMqiService extends GenericMqiService<LevelProductDto> {

    public LevelProductsMqiService(RestTemplate restTemplate, String hostUri,
            int maxRetries, int tempoRetryMs) {
        super(restTemplate, ProductCategory.LEVEL_PRODUCTS, hostUri, maxRetries,
                tempoRetryMs);
    }

}
