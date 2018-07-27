package esa.s1pdgs.cpoc.mqi.server.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.appcatalog.client.AppCatalogMqiAuxiliaryFilesService;
import esa.s1pdgs.cpoc.appcatalog.client.AppCatalogMqiEdrsSessionsService;
import esa.s1pdgs.cpoc.appcatalog.client.AppCatalogMqiLevelJobsService;
import esa.s1pdgs.cpoc.appcatalog.client.AppCatalogMqiLevelProductsService;
import esa.s1pdgs.cpoc.appcatalog.client.AppCatalogMqiLevelReportsService;
import esa.s1pdgs.cpoc.appcatalog.client.GenericAppCatalogMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.AuxiliaryFileDto;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;

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
            @Value("${persistence.suffix-uri-other-app}") final String suffixUriOtherApp) {
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
    public GenericAppCatalogMqiService<LevelProductDto> persistenceServiceForLevelProducts(
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
     * Service for querying MQI for AUXILIARY_FILES category
     * 
     * @param builder
     * @return
     */
    @Bean(name = "persistenceServiceForAuxiliaryFiles")
    public GenericAppCatalogMqiService<AuxiliaryFileDto> persistenceServiceForAuxiliaryFiles(
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
