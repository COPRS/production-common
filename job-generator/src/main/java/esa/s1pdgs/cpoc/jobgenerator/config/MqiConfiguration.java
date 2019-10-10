package esa.s1pdgs.cpoc.jobgenerator.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiClientFactory;
import esa.s1pdgs.cpoc.mqi.client.StatusService;

/**
 * Configuration of MQI client.<br/>
 * Creation of 3 services: LEVEL_PRODUCTS, LEVEL_REPORTS, LEVEL_JOBS
 * 
 * @author Viveris Technologies
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
     */
    @Autowired
    public MqiConfiguration(
            @Value("${process.mqi.host-uri}") final String hostUri,
            @Value("${process.mqi.max-retries}") final int maxRetries,
            @Value("${process.mqi.tempo-retry-ms}") final int tempoRetryMs,
            final RestTemplateBuilder builder
    ) {
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
    public GenericMqiClient genericMqiClient() {
    	return mqiClientFactory.newGenericMqiService();
    }


    /**
     * Service for stopping MQI server
     * 
     * @param builder
     * @return
     */
    @Bean
    public StatusService mqiServiceForStatus() {
    	return mqiClientFactory.newStatusService();
    }
}
