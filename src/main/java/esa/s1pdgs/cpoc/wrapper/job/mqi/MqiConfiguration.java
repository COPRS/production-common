package esa.s1pdgs.cpoc.wrapper.job.mqi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.mqi.client.ErrorService;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.client.LevelJobsMqiService;
import esa.s1pdgs.cpoc.mqi.client.LevelProductsMqiService;
import esa.s1pdgs.cpoc.mqi.client.LevelReportsMqiService;
import esa.s1pdgs.cpoc.mqi.client.LevelSegmentsMqiService;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelSegmentDto;

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
    public MqiConfiguration(
            @Value("${process.mqi.host-uri}") final String hostUri,
            @Value("${process.mqi.max-retries}") final int maxRetries,
            @Value("${process.mqi.tempo-retry-ms}") final int tempoRetryMs) {
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
    public GenericMqiService<LevelSegmentDto> mqiServiceForLevelSegments(
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
    public GenericMqiService<LevelProductDto> mqiServiceForLevelProducts(
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
    @Bean(name = "mqiServiceForLevelReports")
    public GenericMqiService<LevelReportDto> mqiServiceForLevelReports(
            final RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        return new LevelReportsMqiService(template, hostUri, maxRetries,
                tempoRetryMs);
    }

    /**
     * Service for querying MQI for LEVEL_JOBS category
     * 
     * @param builder
     * @return
     */
    @Bean(name = "mqiServiceForLevelJobs")
    public GenericMqiService<LevelJobDto> mqiServiceForLevelJobs(
            final RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        return new LevelJobsMqiService(template, hostUri, maxRetries,
                tempoRetryMs);
    }

    /**
     * Service for publishing errors
     * 
     * @param builder
     * @return
     */
    @Bean(name = "mqiServiceForErrors")
    public ErrorService mqiServiceForErrors(final RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        return new ErrorService(template, hostUri, maxRetries, tempoRetryMs);
    }

    /**
     * Service for stopping application
     * 
     * @param builder
     * @return
     */
    @Bean(name = "mqiServiceForStatus")
    public StatusService mqiServiceForStatus(
            final RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        return new StatusService(template, hostUri, maxRetries, tempoRetryMs);
    }
}
