package esa.s1pdgs.cpoc.mqi.client.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.mqi.client.StatusService;

@Configuration
@EnableConfigurationProperties
public class MqiClientConfiguration {
	private final MqiConfigurationProperties props;

	@Autowired
	public MqiClientConfiguration(final MqiConfigurationProperties props) {
		this.props = props;
	}

	@Bean
	@ConditionalOnProperty(name="mqi.host-uri")
	public GenericMqiClient newGenericMqiService(@Autowired final Supplier<RestTemplate> supp)
	{
    	return new GenericMqiClient(
    			supp.get(),
    			props.getHostUri(), 
        		props.getMaxRetries(), 
        		props.getTempoRetryMs()
        );
	}
	
    @Bean(name="mqiServiceForStatus")
	@ConditionalOnProperty(name="mqi.host-uri")
	public StatusService newStatusService(@Autowired final Supplier<RestTemplate> supp)
	{
		return new StatusService(    			
				supp.get(),
    			props.getHostUri(), 
        		props.getMaxRetries(), 
        		props.getTempoRetryMs()
        );
	}
    
    @Bean(name="mqiMessageFilter")
    public List<MessageFilter> messageFilter() {
    	return new ArrayList<MessageFilter>(props.getMessageFilter());
    }
    
    // convert RestTemplateBuilder into useable/mockable format
    @Bean 
    @ConditionalOnProperty(name="mqi.host-uri")
    public Supplier<RestTemplate> restTemplateSupplier(@Autowired final RestTemplateBuilder builder) {
    	return () -> builder.build();
    }
}
