package esa.s1pdgs.cpoc.compression.trigger.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiClientFactory;
import esa.s1pdgs.cpoc.mqi.client.StatusService;

@Configuration
public class MqiConfiguration {
	private final MqiClientFactory mqiClientFactory;
    /**
     * Constructor
     * 
     * @param hostUri
     * @param maxRetries
     * @param tempoRetryMs
     */
    @Autowired
    public MqiConfiguration(
            @Value("${compression-trigger.mqi.host-uri}") final String hostUri,
            @Value("${compression-trigger.mqi.max-retries}") final int maxRetries,
            @Value("${compression-trigger.mqi.tempo-retry-ms}") final int tempoRetryMs,
            final RestTemplateBuilder builder) {
    	mqiClientFactory = new MqiClientFactory(hostUri, maxRetries, tempoRetryMs)
    			.restTemplateSupplier(builder::build);
    }

    @Bean
    public GenericMqiClient genericMqiClient() {
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
