package esa.s1pdgs.cpoc.ipf.preparation.worker.dispatch;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
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
        
        final Reporting reporting = ReportingUtils.newReportingBuilder()
        		.predecessor(prepJob.getUid())
        		.newReporting("TaskTableLookup");
        
        final List<AppDataJob> jobs = typeAdapter.createAppDataJobs(prepJob);
        
        reporting.begin(
    			ReportingUtils.newFilenameReportingInputFor(prepJob.getProductFamily(), prepJob.getKeyObjectStorage()),
    			new ReportingMessage("Start associating TaskTables to created AppDataJobs")
    	); 
    	
		return new MqiMessageEventHandler.Builder<NullMessage>(ProductCategory.UNDEFINED)
				.onSuccess(res -> reporting.end(
	            		new TaskTableLookupReportingOutput(Collections.singletonList(prepJob.getTaskTableName())),
	            		new ReportingMessage("End associating TaskTables to AppDataJobs")
	            ))
				.onError(e -> reporting.error(new ReportingMessage(
	        			"Error associating TaskTables to AppDataJobs: %s", 
	        			LogUtils.toString(e)
	        	)))
				.publishMessageProducer(() -> {
					if (jobs != null && !jobs.isEmpty()) {
						AppDataJob firstJob = jobs.get(0);
						
			            final String tasktableFilename = firstJob.getTaskTableName();
			            
			            LOGGER.trace("Got TaskTable {}", tasktableFilename);
			            
						// assert that there is a job generator for the assigned tasktable
						if (!generatorAvailableForTasktableNames.contains(tasktableFilename)) {
							throw new IllegalStateException(
									String.format("No job generator found for tasktable %s. Available are: %s", tasktableFilename,
											generatorAvailableForTasktableNames));
						}

			            handleJobs(message, jobs, reporting.getUid(), tasktableFilename);
					}
					
					return new MqiPublishingJob<NullMessage>(Collections.emptyList());
				})
				.newResult();
    }

	// This needs to be synchronized to avoid duplicate jobs
	private final synchronized void handleJobs(
			final GenericMessageDto<IpfPreparationJob> message, 
			final List<AppDataJob> jobsFromMessage,
			final UUID reportingUid,
			final String tasktableFilename
	) throws AbstractCodedException {
		AppDataJob firstJob = jobsFromMessage.get(0);
		
    	final GenericMessageDto<CatalogEvent> firstMessage = firstJob.getMessages().get(0);
    	
		final Optional<List<AppDataJob>> jobForMess = appCat.findJobsFor(firstMessage); 
		final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(firstMessage);
		final Optional<AppDataJob> specificJob = typeAdapter.findAssociatedJobFor(appCat, eventAdapter, firstJob);
						
		// there is already a job for this message --> possible restart scenario --> just update the pod name 
		if (jobForMess.isPresent() && getJobMatchingTasktable(jobForMess.get(), tasktableFilename) != null) {
			final AppDataJob job = getJobMatchingTasktable(jobForMess.get(), tasktableFilename);
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
			LOGGER.debug("Persisting new job(s) for message {} (catalog event message {}) ...",
					message.getId(), firstMessage.getId());
			
			// no job yet associated to this message --> create jobs and persist
			for (final AppDataJob job : jobsFromMessage) {
			    final Date now = new Date();         
			    final AppDataJobGeneration gen = new AppDataJobGeneration();
			    gen.setState(AppDataJobGenerationState.INITIAL);
			    gen.setTaskTable(tasktableFilename);
			    gen.setNbErrors(0);
			    gen.setCreationDate(now);
			    gen.setLastUpdateDate(now);
			    
			    job.setGeneration(gen);
			    job.setPrepJobMessage(message);
			    job.setReportingId(reportingUid);
			    job.setState(AppDataJobState.GENERATING); // will activate that this request can be polled
			    job.setPod(settings.getHostname()); 
			    
			    final AppDataJob newlyCreatedJob = appCat.create(job);
			    LOGGER.info("dispatched job {}", newlyCreatedJob.getId());
			}
		}
	}
	
	/**
	 * Returns the job of the list with the matching tasktable name. Returns null if
	 * no matching job was found
	 */
	private AppDataJob getJobMatchingTasktable(List<AppDataJob> jobs, String taskTableName) {
		for (AppDataJob job : jobs) {
			if (job.getTaskTableName().equals(taskTableName)) {
				return job;
			}
		}
		return null;
	}

}
