package esa.s1pdgs.cpoc.mdc.worker;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mdc.worker.config.MdcWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.mdc.worker.config.MdcWorkerConfigurationProperties.CategoryConfig;
import esa.s1pdgs.cpoc.mdc.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdc.worker.es.EsServices;
import esa.s1pdgs.cpoc.mdc.worker.extraction.MetadataExtractor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.MetadataExtractorFactory;
import esa.s1pdgs.cpoc.mdc.worker.status.AppStatusImpl;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;

@Service
public class MetadataExtractionService {
	private static final Logger LOG = LogManager.getLogger(MetadataExtractionService.class);

    private final AppStatusImpl appStatus;
    private final ErrorRepoAppender errorAppender;
    private final ProcessConfiguration processConfiguration;
    private final EsServices esServices;
    private final MqiClient mqiClient;
    private final MdcWorkerConfigurationProperties properties;
    private final MetadataExtractorFactory extractorFactory;
        
    @Autowired
    public MetadataExtractionService(
    		final AppStatusImpl appStatus,
			final ErrorRepoAppender errorAppender, 
			final ProcessConfiguration processConfiguration, 
			final EsServices esServices,
			final MqiClient mqiClient,
			final MdcWorkerConfigurationProperties properties,
			final MetadataExtractorFactory extractorFactory
	) {
		this.appStatus = appStatus;
		this.errorAppender = errorAppender;
		this.processConfiguration = processConfiguration;
		this.esServices = esServices;
		this.mqiClient = mqiClient;
		this.properties = properties;
		this.extractorFactory = extractorFactory;
	}

	@PostConstruct
    public void init() {	
		final ExecutorService service = Executors.newFixedThreadPool(properties.getProductCategories().size());
		
		for (final Map.Entry<ProductCategory, CategoryConfig> entry : properties.getProductCategories().entrySet()) {				
			service.execute(newConsumerFor(entry.getKey(), entry.getValue()));
		}
    }
	
	private final MqiConsumer<CatalogJob> newConsumerFor(final ProductCategory category, final CategoryConfig config) {
		LOG.debug("Creating MQI consumer for category {} using {}", category, config);
		
		final MetadataExtractor extractor = extractorFactory.newMetadataExtractorFor(category, config);
		
		final CatalogJobListener listener = new CatalogJobListener(
				extractor, 
				esServices, 
				processConfiguration.getHostname(), 
				errorAppender
		);		
		return new MqiConsumer<CatalogJob>(
				mqiClient, 
				category, 
				listener,
				config.getFixedDelayMs(),
				config.getInitDelayPollMs(),
				appStatus
		);
	}
}
