package esa.s1pdgs.cpoc.disseminator.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiClientFactory;

/**
 * Configuration of MQI client.<br/>
 * Creation of 3 services: LEVEL_PRODUCTS, LEVEL_REPORTS, LEVEL_JOBS
 * 
 * @author Faisal Rafi
 */
@Configuration
public class MqiConfiguration {
	
	private final MqiClientFactory mqiClientFactory;

    /**
     * Constructor
     * 
     * @param hostUri
     * @param maxRetries
     * @param tempoRetryMs
     * @param builder
     */
    @Autowired
    public MqiConfiguration(
    		@Value("${file.mqi.host-uri}") final String hostUri,
            @Value("${file.mqi.max-retries}") final int maxRetries,
            @Value("${file.mqi.tempo-retry-ms}") final int tempoRetryMs,
            final RestTemplateBuilder builder) {
    	mqiClientFactory = new MqiClientFactory(hostUri, maxRetries, tempoRetryMs)
    			.restTemplateSupplier(builder::build);
    }

    /**
     * Service for querying MQI 
     * 
     * @param builder
     * @return
     */
    @Bean
    public GenericMqiClient genericService() { 
    	return mqiClientFactory.newGenericMqiService();
    }
}
