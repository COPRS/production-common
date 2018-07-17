package fr.viveris.s1pdgs.level0.wrapper.job.mqi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import fr.viveris.s1pdgs.common.ProductCategory;
import fr.viveris.s1pdgs.mqi.client.GenericMqiService;
import fr.viveris.s1pdgs.mqi.model.queue.LevelJobDto;
import fr.viveris.s1pdgs.mqi.model.queue.LevelProductDto;
import fr.viveris.s1pdgs.mqi.model.queue.LevelReportDto;

/**
 * Configuration of MQI client.<br/>
 * Creation of 3 services: LEVEL_PRODUCTS, LEVEL_REPORTS, LEVEL_JOBS
 * @author Viveris Technologies
 *
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
     * @param hostUri
     * @param maxRetries
     * @param tempoRetryMs
     */
    @Autowired
    public MqiConfiguration(@Value("${process.mqi.host-uri}") final String hostUri,
            @Value("${process.mqi.max-retries}") final int maxRetries,
            @Value("${process.mqi.tempto-retry-ms}") final int tempoRetryMs) {
        this.hostUri = hostUri;
        this.maxRetries = maxRetries;
        this.tempoRetryMs = tempoRetryMs;
    }

    /**
     * Service for querying MQI for LEVEL_PRODUCT category
     * @param builder
     * @return
     */
    @Bean(name = "mqiServiceForLevelProducts")
    public GenericMqiService<LevelProductDto> mqiServiceForLevelProducts(
            final RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        return new GenericMqiService<>(template, ProductCategory.LEVEL_PRODUCTS,
                hostUri, maxRetries, tempoRetryMs);
    }

    /**
     * Service for querying MQI for LEVEL_REPORTS category
     * @param builder
     * @return
     */
    @Bean(name = "mqiServiceForLevelReports")
    public GenericMqiService<LevelReportDto> mqiServiceForLevelReports(
            final RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        return new GenericMqiService<>(template, ProductCategory.LEVEL_REPORTS,
                hostUri, maxRetries, tempoRetryMs);
    }

    /**
     * Service for querying MQI for LEVEL_JOBS category
     * @param builder
     * @return
     */
    @Bean(name = "mqiServiceForLevelJobs")
    public GenericMqiService<LevelJobDto> mqiServiceForLevelJobs(
            final RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        return new GenericMqiService<>(template, ProductCategory.LEVEL_JOBS,
                hostUri, maxRetries, tempoRetryMs);
    }
}
