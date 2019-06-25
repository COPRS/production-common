package esa.s1pdgs.cpoc.mqi.client;

import java.util.function.Supplier;

import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.ProductMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.EdrsSessionsMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.LevelJobsMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.LevelReportsMessageDto;


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
    
	public final GenericMqiService<ProductDto> newProductServiceFor(final ProductCategory category)
	{
    	return new GenericMqiService<ProductDto>(
    			restTemplateBuilder.get(), 
    			category, 
        		hostUri, 
        		maxRetries, 
        		tempoRetryMs, 
        		ProductMessageDto.class
        );
	}
	
	public final GenericMqiService<LevelJobDto> newLevelJobsServiceFor()
	{
    	return new GenericMqiService<LevelJobDto>(
    			restTemplateBuilder.get(), 
    			ProductCategory.LEVEL_JOBS, 
        		hostUri, 
        		maxRetries, 
        		tempoRetryMs, 
        		LevelJobsMessageDto.class
        );
	}
	
	public final GenericMqiService<EdrsSessionDto> newErdsSessionService()
	{
    	return new GenericMqiService<EdrsSessionDto>(
    			restTemplateBuilder.get(), 
    			ProductCategory.EDRS_SESSIONS, 
        		hostUri, 
        		maxRetries, 
        		tempoRetryMs, 
        		EdrsSessionsMessageDto.class
        );
	}
	
	public final GenericMqiService<LevelReportDto> newReportsService()
	{
    	return new GenericMqiService<LevelReportDto>(
    			restTemplateBuilder.get(), 
    			ProductCategory.LEVEL_REPORTS, 
        		hostUri, 
        		maxRetries, 
        		tempoRetryMs, 
        		LevelReportsMessageDto.class
        );
	}
	
	public final StatusService newStatusService()
	{
		return new StatusService(restTemplateBuilder.get(), hostUri, maxRetries, tempoRetryMs);
	}
}
