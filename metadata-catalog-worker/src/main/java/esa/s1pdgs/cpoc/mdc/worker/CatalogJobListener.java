package esa.s1pdgs.cpoc.mdc.worker;

import java.time.LocalDateTime;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mdc.worker.es.EsServices;
import esa.s1pdgs.cpoc.mdc.worker.extraction.MetadataExtractor;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.report.FilenameReportingInput;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;

public class CatalogJobListener implements MqiListener<CatalogJob>
{
	private static final Logger LOG = LogManager.getLogger(CatalogJobListener.class);
	
	private final MetadataExtractor extractor;
    private final EsServices esServices;
    private final String hostname;
    private final ErrorRepoAppender errorAppender;
    
	public CatalogJobListener(
			final MetadataExtractor extractor, 
			final EsServices esServices, 
			final String hostname,
			final ErrorRepoAppender errorAppender
	) {
		this.extractor = extractor;
		this.esServices = esServices;
		this.hostname = hostname;
		this.errorAppender = errorAppender;
	}

	@Override
	public final void onMessage(final GenericMessageDto<CatalogJob> message) {
		final CatalogJob catJob = message.getBody();	
		final String productName = catJob.getProductName();
		final ProductFamily family = catJob.getProductFamily();
		
        final Reporting.Factory reportingFactory = new LoggerReporting.Factory("MetadataExtraction");        
        final Reporting report = reportingFactory.newReporting(0);        
        report.begin(new FilenameReportingInput(productName), new ReportingMessage("Starting metadata extraction"));   
		try {
			//final MetadataExtractor extractor = extractors.getOrDefault(ProductCategory.of(family), MetadataExtractor.FAIL);
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
	        		hostname,
	        		new Date(),
	        		errorMessage,
	        		message
	        )); 
            report.error(new ReportingMessage(errorMessage));
            throw new RuntimeException(errorMessage);
		}    
	}
}