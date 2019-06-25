package esa.s1pdgs.cpoc.mdcatalog.extraction.mqi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.mqi.client.AuxiliaryFilesMqiService;
import esa.s1pdgs.cpoc.mqi.client.EdrsSessionMqiService;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.client.LevelProductsMqiService;
import esa.s1pdgs.cpoc.mqi.client.LevelSegmentsMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;

/**
 * Configuration of MQI client.<br/>
 * Creation of 3 services: LEVEL_PRODUCTS, LEVEL_REPORTS, LEVEL_JOBS
 * 
 * @author Viveris Technologies
 */
@Configuration
public class MqiConfiguration {

    /**
     * Host URI for the MQI server
     */
    private final String hostUri;

    /**
     * Maximal number of retries when query fails
     */
    private final int maxRetries;

    /**
     * Temporisation in ms between 2 retries
     */
    private final int tempoRetryMs;

    /**
     * Constructor
     * 
     * @param hostUri
     * @param maxRetries
     * @param tempoRetryMs
     */
    @Autowired
    public MqiConfiguration(@Value("${file.mqi.host-uri}") final String hostUri,
            @Value("${file.mqi.max-retries}") final int maxRetries,
            @Value("${file.mqi.tempo-retry-ms}") final int tempoRetryMs) {
        this.hostUri = hostUri;
        this.maxRetries = maxRetries;
        this.tempoRetryMs = tempoRetryMs;
    }

    /**
     * Service for querying MQI for LEVEL_SEGMENT category
     * 
     * @param builder
     * @return
     */
    @Bean(name = "mqiServiceForLevelSegments")
    public GenericMqiService<ProductDto> mqiServiceForLevelSegments(
            final RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        return new LevelSegmentsMqiService(template, hostUri, maxRetries,
                tempoRetryMs);
    }

    /**
     * Service for querying MQI for LEVEL_PRODUCT category
     * 
     * @param builder
     * @return
     */
    @Bean(name = "mqiServiceForLevelProducts")
    public GenericMqiService<ProductDto> mqiServiceForLevelProducts(
            final RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        return new LevelProductsMqiService(template, hostUri, maxRetries,
                tempoRetryMs);
    }

    /**
     * Service for querying MQI for LEVEL_REPORTS category
     * 
     * @param builder
     * @return
     */
    @Bean(name = "mqiServiceForAuxiliaryFiles")
    public GenericMqiService<ProductDto> mqiServiceForAuxiliaryFiles(
            final RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        return new AuxiliaryFilesMqiService(template, hostUri, maxRetries,
                tempoRetryMs);
    }

    /**
     * Service for querying MQI for LEVEL_JOBS category
     * 
     * @param builder
     * @return
     */
    @Bean(name = "mqiServiceForEdrsSessions")
    public GenericMqiService<EdrsSessionDto> mqiServiceForEdrsSessions(
            final RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        return new EdrsSessionMqiService(template, hostUri, maxRetries,
                tempoRetryMs);
    }
}
