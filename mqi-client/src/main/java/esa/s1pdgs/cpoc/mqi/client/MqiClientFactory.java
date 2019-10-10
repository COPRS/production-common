package esa.s1pdgs.cpoc.mqi.client;

import java.util.function.Supplier;

import org.springframework.web.client.RestTemplate;


public final class MqiClientFactory {
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
    
    private Supplier<RestTemplate> restTemplateBuilder = RestTemplate::new;

	public MqiClientFactory(String hostUri, int maxRetries, int tempoRetryMs) {
		this.hostUri = hostUri;
		this.maxRetries = maxRetries;
		this.tempoRetryMs = tempoRetryMs;
	}

	public final MqiClientFactory restTemplateSupplier(final Supplier<RestTemplate> supplier)
	{
		restTemplateBuilder = supplier;
		return this;
	}
	
	public final GenericMqiClient newGenericMqiService()
	{
    	return new GenericMqiClient(
    			restTemplateBuilder.get(),
        		hostUri, 
        		maxRetries, 
        		tempoRetryMs
        );
	}
	
	public final StatusService newStatusService()
	{
		return new StatusService(restTemplateBuilder.get(), hostUri, maxRetries, tempoRetryMs);
	}
}
