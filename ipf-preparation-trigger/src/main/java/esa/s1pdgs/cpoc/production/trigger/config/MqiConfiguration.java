package esa.s1pdgs.cpoc.production.trigger.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiClientFactory;
import esa.s1pdgs.cpoc.mqi.client.StatusService;

/**
 * Configuration of MQI client.
 * 
 * @author Faisal Rafi
 */
@Configuration
public class MqiConfiguration {
	
	private final MqiClientFactory mqiClientFactory;

    @Autowired
    public MqiConfiguration(
    		@Value("${mqi.host-uri}") final String hostUri,
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
    
    /**
     * Service for stopping application
     * 
     * @param builder
     * @return
     */
    @Bean(name = "mqiServiceForStatus")
    public StatusService mqiServiceForStatus() {
    	return mqiClientFactory.newStatusService();
    }
}
