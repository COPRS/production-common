package esa.s1pdgs.cpoc.ipf.preparation.worker.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.ProductCategory;

/**
 * Configuration of applicative data catalog client Creation of 3 services:
 * LEVEL_PRODUCTS, EDRS_SESSIONS
 * 
 * @author Viveris Technologies
 */
@Configuration
public class AppCatalogConfiguration {

	/**
	 * Host URI for the applicative catalog server
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
     * Connection timeout
     */
    private final int tmConnectMs;

	/**
	 * Constructor
	 * 
	 * @param hostUri
	 * @param maxRetries
	 * @param tempoRetryMs
	 */
	@Autowired
	public AppCatalogConfiguration(@Value("${process.appcatalog.host-uri}") final String hostUri,
			@Value("${process.appcatalog.max-retries}") final int maxRetries,
			@Value("${process.appcatalog.tempo-retry-ms}") final int tempoRetryMs,
			@Value("${process.appcatalog.tm-connect-ms}") final int tmConnectMs) {
		this.hostUri = hostUri;
		this.maxRetries = maxRetries;
		this.tempoRetryMs = tempoRetryMs;
		this.tmConnectMs = tmConnectMs;
	}

	/**
	 * Service for querying MQI for LEVEL_PRODUCT category
	 * 
	 * @param builder
	 * @return
	 */
	@Bean(name = "appCatalogServiceForEdrsSessions")
	public AppCatalogJobClient appCatalogServiceForEdrsSessions(
			final RestTemplateBuilder builder) {
		@SuppressWarnings("deprecation")
		RestTemplate template = builder.setConnectTimeout(tmConnectMs).build();
		return new AppCatalogJobClient(template, hostUri, maxRetries, tempoRetryMs, ProductCategory.EDRS_SESSIONS);
	}

	/**
	 * Service for querying MQI for LEVEL_PRODUCT category
	 * 
	 * @param builder
	 * @return
	 */
	@Bean(name = "appCatalogServiceForLevelProducts")
	public AppCatalogJobClient appCatalogServiceForLevelProducts(
			final RestTemplateBuilder builder) {
		@SuppressWarnings("deprecation")
		RestTemplate template = builder.setConnectTimeout(tmConnectMs).build();
		return new AppCatalogJobClient(template, hostUri, maxRetries, tempoRetryMs, ProductCategory.LEVEL_PRODUCTS);
	}

    /**
     * Service for querying MQI for LEVEL_PRODUCT category
     * 
     * @param builder
     * @return
     */
    @Bean(name = "appCatalogServiceForLevelSegments")
    public AppCatalogJobClient appCatalogServiceForLevelSegments(
            final RestTemplateBuilder builder) {
    	@SuppressWarnings("deprecation")
        RestTemplate template = builder.setConnectTimeout(tmConnectMs).build();
        return new AppCatalogJobClient(template, hostUri, maxRetries, tempoRetryMs, ProductCategory.LEVEL_SEGMENTS);
    }

}
