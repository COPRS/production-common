package esa.s1pdgs.cpoc.mqi.client;

import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;

public class LevelReportsMqiService extends GenericMqiService<LevelReportDto> {

    public LevelReportsMqiService(RestTemplate restTemplate, String hostUri,
            int maxRetries, int tempoRetryMs) {
        super(restTemplate, ProductCategory.LEVEL_REPORTS, hostUri, maxRetries,
                tempoRetryMs);
    }

}
