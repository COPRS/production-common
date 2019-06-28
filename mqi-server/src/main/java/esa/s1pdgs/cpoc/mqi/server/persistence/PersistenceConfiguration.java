package esa.s1pdgs.cpoc.mqi.server.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.appcatalog.client.mqi.GenericAppCatalogMqiService;

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
    @Bean
    public GenericAppCatalogMqiService appCatService(final RestTemplateBuilder builder) {
        return new GenericAppCatalogMqiService(builder.build(), hostUriCatalog, maxRetries, tempoRetryMs);
    }

    /**
     * Service for checkong if a message is processing or not by another app
     */
    @Bean(name = "checkProcessingOtherApp")
    public OtherApplicationService checkProcessingOtherApp(final RestTemplateBuilder builder) {
        return new OtherApplicationService(builder.build(), portUriOtherApp,
                maxRetries, tempoRetryMs, suffixUriOtherApp);
    }
    

}
