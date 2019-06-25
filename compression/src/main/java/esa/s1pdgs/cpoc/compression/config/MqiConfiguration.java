package esa.s1pdgs.cpoc.compression.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.client.MqiClientFactory;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;

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
            @Value("${compression.mqi.host-uri}") final String hostUri,
            @Value("${compression.mqi.max-retries}") final int maxRetries,
            @Value("${compression.mqi.tempo-retry-ms}") final int tempoRetryMs,
            final RestTemplateBuilder builder) {
    	mqiClientFactory = new MqiClientFactory(hostUri, maxRetries, tempoRetryMs)
    			.restTemplateSupplier(builder::build);
    }

    @Bean(name = "mqiServiceForCompression")
    public GenericMqiService<ProductDto> mqiServiceForCompression() {
    	return mqiClientFactory.newProductServiceFor(ProductCategory.COMPRESSED_PRODUCTS);
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
