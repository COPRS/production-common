package esa.s1pdgs.cpoc.mdc.worker;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mdc.worker.config.MdcWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.mdc.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdc.worker.config.TriggerConfigurationProperties;
import esa.s1pdgs.cpoc.mdc.worker.config.TriggerConfigurationProperties.CategoryConfig;
import esa.s1pdgs.cpoc.mdc.worker.es.EsServices;
import esa.s1pdgs.cpoc.mdc.worker.extraction.MetadataExtractor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.MetadataExtractorFactory;
import esa.s1pdgs.cpoc.mdc.worker.status.AppStatusImpl;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;
import esa.s1pdgs.cpoc.report.message.input.FilenameReportingInput;

@Service
public class MetadataExtractionService implements MqiListener<CatalogJob> {
	private static final Logger LOG = LogManager.getLogger(MetadataExtractionService.class);

    private final AppStatusImpl appStatus;
    private final ErrorRepoAppender errorAppender;
    private final ProcessConfiguration processConfiguration;
    private final EsServices esServices;
    private final MqiClient mqiClient;
    private final MdcWorkerConfigurationProperties properties;
    private final MetadataExtractorFactory extractorFactory;
    private final TriggerConfigurationProperties triggerConfiguration;
        
    @Autowired
    public MetadataExtractionService(
    		final AppStatusImpl appStatus,
			final ErrorRepoAppender errorAppender, 
			final ProcessConfiguration processConfiguration, 
			final EsServices esServices,
			final MqiClient mqiClient,
			final MdcWorkerConfigurationProperties properties,
			final MetadataExtractorFactory extractorFactory,
			final TriggerConfigurationProperties triggerConfiguration
	) {
		this.appStatus = appStatus;
		this.errorAppender = errorAppender;
		this.processConfiguration = processConfiguration;
		this.esServices = esServices;
		this.mqiClient = mqiClient;
		this.properties = properties;
		this.extractorFactory = extractorFactory;
		this.triggerConfiguration = triggerConfiguration;
	}

	@PostConstruct
    public void init() {	
		final Map<ProductCategory, CategoryConfig> entries = triggerConfiguration.getProductCategories();		
		final ExecutorService service = Executors.newFixedThreadPool(entries.size());
		
		for (final Map.Entry<ProductCategory, CategoryConfig> entry : entries.entrySet()) {			
			service.execute(newConsumerFor(entry.getKey(), entry.getValue()));
		}
    }
	
	@Override
	public final void onMessage(final GenericMessageDto<CatalogJob> message) throws Exception {	
		final CatalogJob catJob = message.getBody();	
		final String productName = catJob.getProductName();
		final ProductFamily family = catJob.getProductFamily();		
		final ProductCategory category = ProductCategory.of(family);
		final Reporting reporting = ReportingUtils.newReportingBuilder().newReporting("MetadataExtraction");
    
		reporting.begin(new FilenameReportingInput(productName), new ReportingMessage("Starting metadata extraction"));   
				
		try {
			final MetadataExtractor extractor = extractorFactory.newMetadataExtractorFor(
					category,
					properties.getProductCategories().get(category)
			);			
			final JSONObject metadata = extractor.extract(reporting, message);
	    	LOG.debug("Metadata extracted: {} for product: {}", metadata, productName);
	    	
	    	// TODO move to extractor
	        if (!metadata.has("insertionTime")) {
	        	metadata.put("insertionTime", DateUtils.formatToMetadataDateTimeFormat(LocalDateTime.now()));
	        }
	        
			if (!esServices.isMetadataExist(metadata)) {
				LOG.debug("Creating metatadata in ES for product {}", productName);
			    esServices.createMetadata(metadata);
			}
			else{
				LOG.debug("ES already contains metadata for product {}", productName);
			}
			publish(message, reporting.getUid(), metadata);
	        reporting.end(new ReportingMessage("End metadata extraction"));
            
		}
		catch (final Exception e) {			
			final String errMess = String.format(
					"Failed to extract metadata from product %s of family %s: %s",
					productName,
					family,
					LogUtils.toString(e)
			);
	        reporting.error(new ReportingMessage("Metadata extraction failed: %s", 	LogUtils.toString(e)));	        
	        throw new MetadataExtractException(errMess, e);
		}    
	}
	
	@Override
	public final void onTerminalError(final GenericMessageDto<CatalogJob> message, final Exception error) {
		LOG.error(error);
        errorAppender.send(new FailedProcessingDto(
        		processConfiguration.getHostname(),
        		new Date(),
        		error.getMessage(),
        		message
        )); 
	}
	
	private final void publish(
			final GenericMessageDto<CatalogJob> message, 
			final UUID reportingId,
			final JSONObject metadata
	) throws Exception {
		final CatalogEvent event = toCatalogEvent(message.getBody(), metadata);
		event.setUid(reportingId);
		final GenericPublicationMessageDto<CatalogEvent> messageDto = new GenericPublicationMessageDto<CatalogEvent>(
				message.getId(), 
				event.getProductFamily(), 
				event
		);
		messageDto.setInputKey(message.getInputKey());
		messageDto.setOutputKey(determineOutputKeyDependentOnProductFamilyAndTimeliness(event));		    	
		mqiClient.publish(messageDto, ProductCategory.CATALOG_EVENT);
	}
	
	private String determineOutputKeyDependentOnProductFamilyAndTimeliness(final CatalogEvent event) {

		String outputKey = "";

		Object timeliness = event.getMetadata().get("timeliness");
		if (timeliness != null) {
			outputKey = event.getProductFamily().name() + "@" + timeliness;
		} else {
			outputKey = event.getProductFamily().name();
		}

		return outputKey;
	}

	private final MqiConsumer<CatalogJob> newConsumerFor(final ProductCategory category, final CategoryConfig config) {
		LOG.debug("Creating MQI consumer for category {} using {}", category, config);
		return new MqiConsumer<CatalogJob>(
				mqiClient, 
				category, 
				this,
				config.getFixedDelayMs(),
				config.getInitDelayPollMs(),
				appStatus
		);
	}
	
	private final CatalogEvent toCatalogEvent(final CatalogJob catJob, final JSONObject metadata) {
		final CatalogEvent catEvent = new CatalogEvent();
		catEvent.setProductName(catJob.getProductName());
		catEvent.setKeyObjectStorage(catJob.getKeyObjectStorage());
		catEvent.setProductFamily(catJob.getProductFamily());
		catEvent.setProductType(metadata.getString("productType"));
		catEvent.setMetadata(metadata.toMap());		
		return catEvent;
	}
}
