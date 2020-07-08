package esa.s1pdgs.cpoc.production.trigger.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.appcatalog.client.config.AppCatalogConfigurationProperties;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MessageFilter;
import esa.s1pdgs.cpoc.production.trigger.consumption.EdrsSessionConsumer;
import esa.s1pdgs.cpoc.production.trigger.consumption.L0SegmentConsumer;
import esa.s1pdgs.cpoc.production.trigger.consumption.L0SliceConsumer;
import esa.s1pdgs.cpoc.production.trigger.consumption.ProductTypeConsumptionHandler;
import esa.s1pdgs.cpoc.production.trigger.service.GenericConsumer;

@Configuration
public class TriggerConfig {	
	private static final Map<ProductCategory, ProductTypeConsumptionHandler> CONSUMPTION_HANDLER_FOR_CATEGORY = new LinkedHashMap<>();
	static {
		CONSUMPTION_HANDLER_FOR_CATEGORY.put(ProductCategory.EDRS_SESSIONS,  new EdrsSessionConsumer());
		CONSUMPTION_HANDLER_FOR_CATEGORY.put(ProductCategory.LEVEL_SEGMENTS,  new L0SegmentConsumer());
		CONSUMPTION_HANDLER_FOR_CATEGORY.put(ProductCategory.LEVEL_PRODUCTS,  new L0SliceConsumer());
	}
	
	private final ProcessSettings processSettings; 
	private final GenericMqiClient mqiService;
	private final List<MessageFilter> messageFilter;
	private final ErrorRepoAppender errorRepoAppender;
	private final AppStatus appStatus;
	private final MetadataClient metadataClient;
	
	@Autowired
	public TriggerConfig(
			final RestTemplateBuilder restTemplateBuilder,
			final ProcessSettings processSettings, 
			final GenericMqiClient mqiService,
			final List<MessageFilter> messageFilter,
			final ErrorRepoAppender errorRepoAppender, 
			final AppStatus appStatus,
			final MetadataClient metadataClient
	) {
		this.processSettings = processSettings;
		this.mqiService = mqiService;
		this.messageFilter = messageFilter;
		this.errorRepoAppender = errorRepoAppender;
		this.appStatus = appStatus;
		this.metadataClient = metadataClient;
	}
		
	@Bean
	public ProductTypeConsumptionHandler newProductTypeConsumptionHandler() {		
		final ProductTypeConsumptionHandler res = CONSUMPTION_HANDLER_FOR_CATEGORY.get(processSettings.getCategory());
		
		if (res == null) {
			throw new IllegalStateException(
					String.format(
							"Invalid category %s, configured are: %s", 
							processSettings.getCategory(),
							CONSUMPTION_HANDLER_FOR_CATEGORY.keySet()
					)
			);
		}
		return res;
	}
	
	@Bean	
	@Autowired
	public GenericConsumer newConsumer( 
			final ProductTypeConsumptionHandler handler,
			final AppCatalogJobClient appCatClient
	) {		
		return new GenericConsumer(
				processSettings, 
				mqiService, 
				messageFilter,
				appStatus,
				errorRepoAppender, 
				metadataClient, 
				handler
		);
	}
}
