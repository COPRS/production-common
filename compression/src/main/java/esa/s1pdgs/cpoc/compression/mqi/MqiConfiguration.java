package esa.s1pdgs.cpoc.compression.mqi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.mqi.client.CompressedProductsMqiService;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.client.LevelProductsMqiService;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;

@Configuration
public class MqiConfiguration {

    /**
     * Host URI for the MQI server
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
     * Constructor
     * 
     * @param hostUri
     * @param maxRetries
     * @param tempoRetryMs
     */
    @Autowired
    public MqiConfiguration(
            @Value("${compression.mqi.host-uri}") final String hostUri,
            @Value("${compression.mqi.max-retries}") final int maxRetries,
            @Value("${compression.mqi.tempo-retry-ms}") final int tempoRetryMs) {
        this.hostUri = hostUri;
        this.maxRetries = maxRetries;
        this.tempoRetryMs = tempoRetryMs;
    }

    @Bean(name = "mqiServiceForCompression")
    public GenericMqiService<CompressionJobDto> mqiServiceForCompression(
            final RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        return new CompressedProductsMqiService(template, hostUri, maxRetries,
                tempoRetryMs);
    }

    /**
     * Service for stopping application
     * 
     * @param builder
     * @return
     */
    @Bean(name = "mqiServiceForStatus")
    public StatusService mqiServiceForStatus(
            final RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        return new StatusService(template, hostUri, maxRetries, tempoRetryMs);
    }
}
