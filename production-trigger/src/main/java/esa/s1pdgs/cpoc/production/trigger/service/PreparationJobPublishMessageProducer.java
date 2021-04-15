package esa.s1pdgs.cpoc.production.trigger.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.production.trigger.config.ProcessSettings;
import esa.s1pdgs.cpoc.production.trigger.report.DispatchReportInput;
import esa.s1pdgs.cpoc.production.trigger.report.L0EWSliceMaskCheckReportingOutput;
import esa.s1pdgs.cpoc.production.trigger.report.SeaCoverageCheckReportingOutput;
import esa.s1pdgs.cpoc.production.trigger.taskTableMapping.TasktableMapper;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingFactory;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public final class PreparationJobPublishMessageProducer {
	static final Logger LOGGER = LogManager.getLogger(PreparationJobPublishMessageProducer.class);
	
    private final ProcessSettings processSettings;
    private final Pattern seaCoverageCheckPattern;
    private final Pattern l0EwSlcCheckPattern;
    private final MetadataClient metadataClient;
    		
    public PreparationJobPublishMessageProducer(
    		final ProcessSettings processSettings, 
    		final Pattern seaCoverageCheckPattern,
			final Pattern l0EwSlcCheckPattern, 
			final MetadataClient metadataClient
	) {
		this.processSettings = processSettings;
		this.seaCoverageCheckPattern = seaCoverageCheckPattern;
		this.l0EwSlcCheckPattern = l0EwSlcCheckPattern;
		this.metadataClient = metadataClient;
	}

	public final MqiPublishingJob<IpfPreparationJob> createPublishingJob(
    		final Reporting reporting, 
    		final GenericMessageDto<CatalogEvent> mqiMessage,
    		final TasktableMapper ttMapper
    ) throws Exception {
        final CatalogEvent event = mqiMessage.getBody();
        final String productName = event.getProductName();
        final ProductFamily family = event.getProductFamily();
        
        LOGGER.debug("Handling consumption of product {}", productName);

        reporting.begin(
        		ReportingUtils.newFilenameReportingInputFor(event.getProductFamily(), productName),
        		new ReportingMessage("Received CatalogEvent for %s", productName)
        );  
        
        if (isAllowed(productName, family, reporting)) {                   
            final List<String> taskTableNames = ttMapper.tasktableFor(event);
            final List<GenericPublicationMessageDto<? extends AbstractMessage>> messageDtos = new ArrayList<>(taskTableNames.size());
            
            for (final String taskTableName: taskTableNames)
            {
            	if (l0EwSliceMaskCheck(reporting, productName, family, taskTableName)) {
            		LOGGER.debug("Tasktable for {} is {}", productName, taskTableName);
            		messageDtos.add(dispatch(mqiMessage,reporting, taskTableName));
            	}
            }               
            LOGGER.info("Dispatching product {}", productName);
            return new MqiPublishingJob<IpfPreparationJob>(messageDtos);          
        }
        else {
        	LOGGER.debug("CatalogEvent for {} is ignored", productName); 
        }
        LOGGER.debug("Done handling consumption of product {}", productName);
        return new MqiPublishingJob<IpfPreparationJob>(Collections.emptyList());
    }
    
    
	private final boolean isAllowed(final String productName, final ProductFamily family, final ReportingFactory reporting) throws Exception {
        return seaCoverageCheck(reporting, productName, family);
	}

	private boolean seaCoverageCheck(final ReportingFactory reporting, final String productName,
			final ProductFamily family) throws Exception {
		// S1PRO-483: check for matching products if they are over sea. If not, simply skip the
        // production
		final Reporting seaReport = reporting.newReporting("SeaCoverageCheck");
        try {
			if (seaCoverageCheckPattern.matcher(productName).matches()) {   
				seaReport.begin(
						ReportingUtils.newFilenameReportingInputFor(family, productName),
						new ReportingMessage("Checking sea coverage")
				);	
				if (metadataClient.getSeaCoverage(family, productName) <= processSettings.getMinSeaCoveragePercentage()) {
					seaReport.end(
							new SeaCoverageCheckReportingOutput(false), 
							new ReportingMessage("Product %s is not over sea", productName)
					);
					LOGGER.warn("Skipping job generation for product {} because it is not over sea", productName);
			        return false;
			    }
				else {
					seaReport.end(
							new SeaCoverageCheckReportingOutput(true), 
							new ReportingMessage("Product %s is over sea", productName)
					);
				}
			}
		} catch (final Exception e) {
			seaReport.error(new ReportingMessage("SeaCoverage check failed: %s", LogUtils.toString(e)));
			throw e;			
		}        
        return true;
	}
	
	private boolean l0EwSliceMaskCheck(final ReportingFactory reporting, final String productName,
			final ProductFamily family, final String taskTableName) throws Exception {
		// S1PRO-2320: check if EW_SLC products matches a specific mask. If not, simply skip the production
		final Reporting ewSlcReport = reporting.newReporting("L0EWSliceMaskCheck");
        try {
			if (l0EwSlcCheckPattern.matcher(productName).matches() && taskTableName.contains(processSettings.getL0EwSlcTaskTableName())) { 
				ewSlcReport.begin(
						ReportingUtils.newFilenameReportingInputFor(family, productName),
						new ReportingMessage("Checking if L0 EW slice %s is intersecting mask", productName)
				);	
				if (!metadataClient.isIntersectingEwSlcMask(family, productName)) {
					ewSlcReport.end(
							new L0EWSliceMaskCheckReportingOutput(false), 
							new ReportingMessage("L0 EW slice %s is not intersecting mask", productName)
					);
					LOGGER.warn("Skipping job generation for product {} because it is not intersecting mask", productName);
			        return false;
			    }
				else {
					ewSlcReport.end(
							new L0EWSliceMaskCheckReportingOutput(true), 
							new ReportingMessage("L0 EW slice %s is intersecting mask", productName)
					);
				}
			}
		} catch (final Exception e) {
			ewSlcReport.error(new ReportingMessage("L0 EW slice check failed: %s", LogUtils.toString(e)));
			throw e;			
		}        
        return true;
	}
	
	private final GenericPublicationMessageDto<IpfPreparationJob> dispatch(
			final GenericMessageDto<CatalogEvent> mqiMessage,
			final ReportingFactory reportingFactory,
    		final String taskTableName
	) {
        final CatalogEvent event = mqiMessage.getBody();
        final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(mqiMessage);	
        
        // FIXME reporting of AppDataJob doesn't make sense here any more, needs to be replaced by something
        // meaningful
        final int appDataJobId = 0;
		
        final Reporting reporting = reportingFactory.newReporting("Dispatch");            
        reporting.begin(
        		DispatchReportInput.newInstance(appDataJobId, event.getProductName(), processSettings.getProductType()),
        		new ReportingMessage(
        				"Dispatching AppDataJob %s for %s %s", 
        				appDataJobId, 
        				processSettings.getProductType(), 
        				event.getProductName()
        		)
        );     
    	final IpfPreparationJob job = new IpfPreparationJob();    	    	
        job.setLevel(processSettings.getLevel());
        job.setHostname(processSettings.getHostname());
        job.setEventMessage(mqiMessage);     
    	job.setTaskTableName(taskTableName);   
    	// S1PRO-1772: user productSensing accessors here to make start/stop optional here (RAWs don't have them)
    	job.setStartTime(eventAdapter.productSensingStartDate());
    	job.setStopTime(eventAdapter.productSensingStopDate());
    	job.setProductFamily(event.getProductFamily());
    	job.setKeyObjectStorage(event.getProductName());
    	job.setUid(reporting.getUid());
    	job.setDebug(event.isDebug());
    	
    	final GenericPublicationMessageDto<IpfPreparationJob> messageDto = new GenericPublicationMessageDto<IpfPreparationJob>(
    			mqiMessage.getId(), 
    			event.getProductFamily(), 
    			job
    	);
    	messageDto.setInputKey(mqiMessage.getInputKey());
    	messageDto.setOutputKey(event.getProductFamily().name());
		reporting.end(
				new ReportingMessage(
						"AppDataJob %s for %s %s dispatched", 
						appDataJobId, 
						processSettings.getProductType(), 
						event.getProductName()
        		)
		);
		return messageDto;
	}
}