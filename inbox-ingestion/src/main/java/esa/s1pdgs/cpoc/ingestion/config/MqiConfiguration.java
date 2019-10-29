package esa.s1pdgs.cpoc.ingestion.config;

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

    @Autowired
    public MqiConfiguration(
    		/* @Value("${mqi.host-uri}") */ @Value("#{environment.mqi_host_uri}") final String hostUri,
            @Value("${mqi.max-retries}") final int maxRetries,
            @Value("${mqi.tempo-retry-ms}") final int tempoRetryMs,
            final RestTemplateBuilder builder) {
    	mqiClientFactory = new MqiClientFactory(hostUri, maxRetries, tempoRetryMs)
    			.restTemplateSupplier(builder::build);
    }

    @Bean
    public GenericMqiClient genericService() { 
    	return mqiClientFactory.newGenericMqiService();
    }
}
