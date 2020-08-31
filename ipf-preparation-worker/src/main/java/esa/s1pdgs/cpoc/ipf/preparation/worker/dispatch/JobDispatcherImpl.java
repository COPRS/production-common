package esa.s1pdgs.cpoc.ipf.preparation.worker.dispatch;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.appcat.AppCatJobService;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.report.TaskTableLookupReportingOutput;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.NullMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

/**
 * Job dispatcher<br/>
 * 
 * When a message is read and can be processing, it will be dispatch to one or
 * several task tables according the product category.
 * 
 * @param <T>
 */
public class JobDispatcherImpl implements JobDispatcher {
    private final AppCatJobService appCat;    
    private final ProcessSettings settings;
    private final ProductTypeAdapter typeAdapter;
    private final Collection<String> generatorAvailableForTasktableNames;

    public JobDispatcherImpl(
    		final ProductTypeAdapter typeAdapter,
    		final ProcessSettings settings,
            final AppCatJobService appCat,         
            final Collection<String> generatorAvailableForTasktableNames
    ) {
        this.appCat = appCat;
        this.generatorAvailableForTasktableNames = generatorAvailableForTasktableNames;
        this.settings = settings;
        this.typeAdapter = typeAdapter;
    }

	@Override
	public final MqiMessageEventHandler dispatch(final GenericMessageDto<IpfPreparationJob> message) throws Exception {
    	final IpfPreparationJob prepJob = message.getBody();
    	
    	final AppDataJob jobFromMessage = toAppDataJob(prepJob);
        
        final Reporting reporting = ReportingUtils.newReportingBuilder()
        		.predecessor(prepJob.getUid())
        		.newReporting("TaskTableLookup");
        
        typeAdapter.customAppDataJob(jobFromMessage);
    	LOGGER.trace("== dispatch job {}", jobFromMessage.toString());
    	
        final String tasktableFilename = jobFromMessage.getTaskTableName();
        
    	reporting.begin(
    			ReportingUtils.newFilenameReportingInputFor(prepJob.getProductFamily(), jobFromMessage.getProductName()),
    			new ReportingMessage("Start associating TaskTables to AppDataJob", jobFromMessage.getId())
    	); 
    	
		return new MqiMessageEventHandler.Builder<NullMessage>(ProductCategory.UNDEFINED)
				.onSuccess(res -> reporting.end(
	            		new TaskTableLookupReportingOutput(Collections.singletonList(tasktableFilename)),
	            		new ReportingMessage("End associating TaskTables to AppDataJob")
	            ))
				.onError(e -> reporting.error(new ReportingMessage(
	        			"Error associating TaskTables to AppDataJob: %s", 
	        			LogUtils.toString(e)
	        	)))
				.publishMessageProducer(() -> {
					LOGGER.trace("Got TaskTable {}", tasktableFilename);
		            
		            // assert that there is a job generator for the assigned tasktable
		            if (!generatorAvailableForTasktableNames.contains(tasktableFilename))  {
		            	throw new IllegalStateException(
		            			String.format(
		            					"No job generator found for tasktable %s. Available are: %s", 
		            					tasktableFilename,
		            					generatorAvailableForTasktableNames
		            			)
		            	);
		            } 
		    		handleJob(message, jobFromMessage, reporting.getUid(), tasktableFilename);
		    		return new MqiPublishingJob<NullMessage>(Collections.emptyList());
				})
				.newResult();
    }

	private final AppDataJob toAppDataJob(final IpfPreparationJob prepJob) {
        final AppDataJob job = new AppDataJob();
        job.setLevel(prepJob.getLevel());
        job.setPod(prepJob.getHostname());
        job.getMessages().add(prepJob.getEventMessage());
        job.setProduct(newProductFor(prepJob.getEventMessage())); 
    	job.setTaskTableName(prepJob.getTaskTableName());     
    	job.setStartTime(prepJob.getStartTime());
    	job.setStopTime(prepJob.getStopTime());
    	job.setProductName(prepJob.getKeyObjectStorage());     
    	return job;    	
	}
	
	private final AppDataJobProduct newProductFor(final GenericMessageDto<CatalogEvent> mqiMessage) {
		final CatalogEvent event = mqiMessage.getBody();
	    final AppDataJobProduct productDto = new AppDataJobProduct();
	    
		final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(mqiMessage);		
		productDto.getMetadata().put("productName", event.getProductName());
		productDto.getMetadata().put("productType", event.getProductType());
		productDto.getMetadata().put("satelliteId", eventAdapter.satelliteId());
		productDto.getMetadata().put("missionId", eventAdapter.missionId());
		productDto.getMetadata().put("processMode", eventAdapter.processMode());
		// S1PRO-1772: user productSensing accessors here to make start/stop optional here (RAWs don't have them)
		productDto.getMetadata().put("startTime", eventAdapter.productSensingStartDate());
		productDto.getMetadata().put("stopTime", eventAdapter.productSensingStopDate());     
		productDto.getMetadata().put("timeliness", eventAdapter.timeliness());
		productDto.getMetadata().put("acquistion", eventAdapter.swathType());
	    return productDto;
	}

	// This needs to be synchronized to avoid duplicate jobs
	private final synchronized void handleJob(
			final GenericMessageDto<IpfPreparationJob> message, 
			final AppDataJob jobFromMessage,
			final UUID reportingUid,
			final String tasktableFilename
	) throws AbstractCodedException {
    	final GenericMessageDto<CatalogEvent> firstMessage = jobFromMessage.getMessages().get(0);
    	
		final Optional<AppDataJob> jobForMess = appCat.findJobFor(firstMessage); 
		final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(firstMessage);
		final Optional<AppDataJob> specificJob = typeAdapter.findAssociatedJobFor(appCat, eventAdapter);
		
		// there is already a job for this message --> possible restart scenario --> just update the pod name 
		if (jobForMess.isPresent()) {		
			final AppDataJob job = jobForMess.get();
			LOGGER.warn("Found job {} already associated to mqiMessage {}. Ignoring new message ...",
					job.getId(), firstMessage.getId());		
		}
		else if (specificJob.isPresent()) {
			final AppDataJob existingJob = specificJob.get(); 
			LOGGER.info("Found job {} already being handled. Appending new message ...",
					existingJob.getId(), firstMessage.getId());
			appCat.appendMessage(existingJob.getId(), firstMessage);
		}
		else {
			LOGGER.debug("Persisting new job for message {} (catalog event message {}) ...",
					message.getId(), firstMessage.getId());
			
			// no job yet associated to this message --> create job and persist
		    final Date now = new Date();         
		    final AppDataJobGeneration gen = new AppDataJobGeneration();
		    gen.setState(AppDataJobGenerationState.INITIAL);
		    gen.setTaskTable(tasktableFilename);
		    gen.setNbErrors(0);
		    gen.setCreationDate(now);
		    gen.setLastUpdateDate(now);
		    
		    jobFromMessage.setGeneration(gen);
		    jobFromMessage.setPrepJobMessage(message);
		    jobFromMessage.setReportingId(reportingUid);
		    jobFromMessage.setState(AppDataJobState.GENERATING); // will activate that this request can be polled
		    jobFromMessage.setPod(settings.getHostname()); 
		    
		    final AppDataJob newlyCreatedJob = appCat.create(jobFromMessage);
		    LOGGER.info("dispatched job {}", newlyCreatedJob.getId());                
		}
	}

}
