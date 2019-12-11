package esa.s1pdgs.cpoc.mdc.worker;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mdc.worker.config.MdcWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.mdc.worker.config.MdcWorkerConfigurationProperties.CategoryConfig;
import esa.s1pdgs.cpoc.mdc.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdc.worker.es.EsServices;
import esa.s1pdgs.cpoc.mdc.worker.extraction.MetadataExtractor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.MetadataExtractorFactory;
import esa.s1pdgs.cpoc.mdc.worker.status.AppStatusImpl;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.report.FilenameReportingInput;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;

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
	
	public final void consume(final GenericMessageDto<CatalogJob> message, final CategoryConfig config)
			throws AbstractCodedException {		
		final CatalogJob catJob = message.getBody();	
		final String productName = catJob.getProductName();
		final ProductFamily family = catJob.getProductFamily();
		
        final Reporting.Factory reportingFactory = new LoggerReporting.Factory("MetadataExtraction");        
        final Reporting report = reportingFactory.newReporting(0);        
        report.begin(new FilenameReportingInput(productName), new ReportingMessage("Starting metadata extraction"));   
		try {
			final MetadataExtractor extractor = extractorFactory.newMetadataExtractorFor(
					ProductCategory.of(catJob.getProductFamily()), 
					config
			);			
			final JSONObject metadata = extractor.extract(reportingFactory, message);
        	LOG.debug("Metadata extracted :{} for product: {}", metadata, productName);
        	
        	// TODO move to extractor
            if (!metadata.has("insertionTime")) {
            	metadata.put("insertionTime", DateUtils.formatToMetadataDateTimeFormat(LocalDateTime.now()));
            }
            
            final Reporting reportPublish = reportingFactory.newReporting(4);            
            reportPublish.begin(new ReportingMessage("Start publishing metadata"));

            try {
				if (!esServices.isMetadataExist(metadata)) {
				    esServices.createMetadata(metadata);
				}
				final CatalogEvent event = toCatalogEvent(catJob, metadata);
		    	final GenericPublicationMessageDto<CatalogEvent> messageDto = new GenericPublicationMessageDto<CatalogEvent>(
		    			message.getId(), 
		    			event.getProductFamily(), 
		    			event
		    	);
		    	messageDto.setInputKey(message.getInputKey());
		    	messageDto.setOutputKey(event.getProductFamily().name());		    	
				mqiClient.publish(messageDto, ProductCategory.of(family));		
				
			    reportPublish.end(new ReportingMessage("End publishing metadata"));
				
			} catch (final Exception e) {
				reportPublish.error(new ReportingMessage("[code {}] {}", ErrorCode.INTERNAL_ERROR.getCode(), LogUtils.toString(e)));
				throw e;
			}
            report.end(new ReportingMessage("End metadata extraction"));
		}
		catch (final Exception e) {
			final String errorMessage = String.format(
					"Failed to extract metadata from product %s of family %s: %s", 
					productName,
					family,
					LogUtils.toString(e)
					
			);
			LOG.error(errorMessage);
            errorAppender.send(new FailedProcessingDto(
            		processConfiguration.getHostname(),
	        		new Date(),
	        		errorMessage,
	        		message
	        )); 
            report.error(new ReportingMessage(errorMessage));
            throw new RuntimeException(errorMessage);
		}    
	}
	
	private final MqiConsumer<CatalogJob> newConsumerFor(final ProductCategory category, final CategoryConfig config) {
		LOG.debug("Creating MQI consumer for category {} using {}", category, config);
		return new MqiConsumer<CatalogJob>(
				mqiClient, 
				category, 
				m -> consume(m, config),
				config.getFixedDelayMs(),
				config.getInitDelayPollMs(),
				appStatus
		);
	}
	
	private CatalogEvent toCatalogEvent(final CatalogJob catJob, final JSONObject metadata) {
		final CatalogEvent catEvent = new CatalogEvent();
		catEvent.setProductName(catJob.getProductName());
		catEvent.setKeyObjectStorage(catJob.getKeyObjectStorage());
		catEvent.setProductFamily(catJob.getProductFamily());
		catEvent.setProductType(metadata.getString("productType"));
		catEvent.setMetadata(toJsonNode(metadata));		
		return catEvent;
	}
	
	private final JsonNode toJsonNode(final JSONObject json) {
		try {
			final StringWriter stringWriter = new StringWriter();
			json.write(stringWriter); 
			final ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.readTree(stringWriter.toString());
		} catch (JSONException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}
