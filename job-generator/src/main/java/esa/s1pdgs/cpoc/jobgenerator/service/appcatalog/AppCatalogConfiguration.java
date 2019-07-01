package esa.s1pdgs.cpoc.jobgenerator.service.appcatalog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appcatalog.client.job.EdrsSessionsAppCatalogJobService;
import esa.s1pdgs.cpoc.appcatalog.client.job.LevelProductsAppCatalogJobService;
import esa.s1pdgs.cpoc.appcatalog.client.job.LevelSegmentsAppCatalogJobService;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;

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
	public AppCatalogJobClient<EdrsSessionDto> appCatalogServiceForEdrsSessions(
			final RestTemplateBuilder builder) {
		RestTemplate template = builder.setConnectTimeout(tmConnectMs).build();
		return new EdrsSessionsAppCatalogJobService(template, hostUri, maxRetries, tempoRetryMs);
	}

	/**
	 * Service for querying MQI for LEVEL_PRODUCT category
	 * 
	 * @param builder
	 * @return
	 */
	@Bean(name = "appCatalogServiceForLevelProducts")
	public AppCatalogJobClient<ProductDto> appCatalogServiceForLevelProducts(
			final RestTemplateBuilder builder) {
		RestTemplate template = builder.setConnectTimeout(tmConnectMs).build();
		return new LevelProductsAppCatalogJobService(template, hostUri, maxRetries, tempoRetryMs);
	}

    /**
     * Service for querying MQI for LEVEL_PRODUCT category
     * 
     * @param builder
     * @return
     */
    @Bean(name = "appCatalogServiceForLevelSegments")
    public AppCatalogJobClient<ProductDto> appCatalogServiceForLevelSegments(
            final RestTemplateBuilder builder) {
        RestTemplate template = builder.setConnectTimeout(tmConnectMs).build();
        return new LevelSegmentsAppCatalogJobService(template, hostUri, maxRetries, tempoRetryMs);
    }

}
