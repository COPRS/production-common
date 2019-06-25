package esa.s1pdgs.cpoc.mqi.server.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.appcatalog.client.mqi.AppCatalogMqiAuxiliaryFilesService;
import esa.s1pdgs.cpoc.appcatalog.client.mqi.AppCatalogMqiCompressionJobService;
import esa.s1pdgs.cpoc.appcatalog.client.mqi.AppCatalogMqiEdrsSessionsService;
import esa.s1pdgs.cpoc.appcatalog.client.mqi.AppCatalogMqiLevelJobsService;
import esa.s1pdgs.cpoc.appcatalog.client.mqi.AppCatalogMqiLevelProductsService;
import esa.s1pdgs.cpoc.appcatalog.client.mqi.AppCatalogMqiLevelReportsService;
import esa.s1pdgs.cpoc.appcatalog.client.mqi.AppCatalogMqiLevelSegmentsService;
import esa.s1pdgs.cpoc.appcatalog.client.mqi.GenericAppCatalogMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;

/**
 * Configuration of applicative catalog client for data persistence.<br/>
 * Creation of 5 services, one per product category
 * 
 * @author Viveris Technologies
 */
@Configuration
public class PersistenceConfiguration {

    /**
     * Host URI for the applicative catalog
     */
    private final String hostUriCatalog;
    
    /**
     * 
     */
    private final String portUriOtherApp;

    /**
     * Maximal number of retries when query fails
     */
    private final int maxRetries;

    /**
     * Temporisation in ms between 2 retries
     */
    private final int tempoRetryMs;

    /**
     * 
     */
    private final String suffixUriOtherApp;

    /**
     * Constructor
     * 
     * @param hostUri
     * @param maxRetries
     * @param tempoRetryMs
     */
    @Autowired
    public PersistenceConfiguration(
            @Value("${persistence.host-uri-catalog}") final String hostUriCatalog,
            @Value("${persistence.port-uri-other-app}") final String portUriOtherApp,
            @Value("${persistence.max-retries}") final int maxRetries,
            @Value("${persistence.tempo-retry-ms}") final int tempoRetryMs,
            @Value("${persistence.other-app.suffix-uri}") final String suffixUriOtherApp) {
        this.hostUriCatalog = hostUriCatalog;
        this.maxRetries = maxRetries;
        this.tempoRetryMs = tempoRetryMs;
        this.portUriOtherApp = portUriOtherApp;
        this.suffixUriOtherApp = suffixUriOtherApp;
    }

    /**
     * Service for querying MQI for LEVEL_PRODUCT category
     * 
     * @param builder
     * @return
     */
    @Bean(name = "persistenceServiceForLevelProducts")
    public GenericAppCatalogMqiService<ProductDto> persistenceServiceForLevelProducts(
            final RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        return new AppCatalogMqiLevelProductsService(template, hostUriCatalog,
                maxRetries, tempoRetryMs);
    }

    /**
     * Service for querying MQI for LEVEL_REPORTS category
     * 
     * @param builder
     * @return
     */
    @Bean(name = "persistenceServiceForLevelReports")
    public GenericAppCatalogMqiService<LevelReportDto> persistenceServiceForLevelReports(
            final RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        return new AppCatalogMqiLevelReportsService(template, hostUriCatalog,
                maxRetries, tempoRetryMs);
    }

    /**
     * Service for querying MQI for LEVEL_JOBS category
     * 
     * @param builder
     * @return
     */
    @Bean(name = "persistenceServiceForLevelJobs")
    public GenericAppCatalogMqiService<LevelJobDto> persistenceServiceForLevelJobs(
            final RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        return new AppCatalogMqiLevelJobsService(template, hostUriCatalog,
                maxRetries, tempoRetryMs);
    }

    /**
     * Service for querying MQI for LEVEL_SEGMENTS category
     * 
     * @param builder
     * @return
     */
    @Bean(name = "persistenceServiceForLevelSegments")
    public GenericAppCatalogMqiService<ProductDto> persistenceServiceForLevelSegments(
            final RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        return new AppCatalogMqiLevelSegmentsService(template, hostUriCatalog,
                maxRetries, tempoRetryMs);
    }

    /**
     * Service for querying MQI for AUXILIARY_FILES category
     * 
     * @param builder
     * @return
     */
    @Bean(name = "persistenceServiceForAuxiliaryFiles")
    public GenericAppCatalogMqiService<ProductDto> persistenceServiceForAuxiliaryFiles(
            final RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        return new AppCatalogMqiAuxiliaryFilesService(template, hostUriCatalog,
                maxRetries, tempoRetryMs);
    }

    /**
     * Service for querying MQI for EDRS_SESSIONS category
     * 
     * @param builder
     * @return
     */
    @Bean(name = "persistenceServiceForEdrsSessions")
    public GenericAppCatalogMqiService<EdrsSessionDto> persistenceServiceForEdrsSessions(
            final RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        return new AppCatalogMqiEdrsSessionsService(template, hostUriCatalog,
                maxRetries, tempoRetryMs);
    }
    
    @Bean(name = "persistenceServiceForCompressionJob")
    public GenericAppCatalogMqiService<ProductDto> persistenceServiceForCompressionJob(
            final RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        return new AppCatalogMqiCompressionJobService(template, hostUriCatalog,
                maxRetries, tempoRetryMs);
    }
    
    /**
     * Service for checkong if a message is processing or not by another app
     */
    @Bean(name = "checkProcessingOtherApp")
    public OtherApplicationService checkProcessingOtherApp(
            final RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        return new OtherApplicationService(template, portUriOtherApp,
                maxRetries, tempoRetryMs, suffixUriOtherApp);
    }
    

}
