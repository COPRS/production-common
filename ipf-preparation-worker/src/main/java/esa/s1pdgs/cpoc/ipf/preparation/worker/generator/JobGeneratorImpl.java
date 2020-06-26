package esa.s1pdgs.cpoc.ipf.preparation.worker.generator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.generator.state.JobGenerationStateTransitions;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.JobGen;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TasktableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.publish.Publisher;
import esa.s1pdgs.cpoc.ipf.preparation.worker.query.AuxQueryHandler;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.metadata.client.SearchMetadataQuery;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public final class JobGeneratorImpl implements JobGenerator {
	private final TasktableAdapter tasktableAdapter;
	private final ProductTypeAdapter typeAdapter;	
    private final AppCatalogJobClient appCatClient;
    private final GracePeriodHandler gracePeriodHandler;
    private final ProcessSettings settings;
    private final ErrorRepoAppender errorAppender;
    private final Publisher publisher;
    private final JobOrder jobOrderTemplate;
    private final Map<Integer, SearchMetadataQuery> metadataQueries;
    private final List<List<String>> tasks;
    private final AuxQueryHandler auxQueryHandler;
        
	public JobGeneratorImpl(
			final TasktableAdapter tasktableAdapter,
			final ProductTypeAdapter typeAdapter, 
			final AppCatalogJobClient appCatClient,
			final GracePeriodHandler gracePeriodHandler, 
			final ProcessSettings settings,
			final ErrorRepoAppender errorAppender,
			final Publisher publisher,
			final JobOrder jobOrderTemplate,
			final Map<Integer, SearchMetadataQuery> metadataQueries,
			final List<List<String>> tasks,
			final AuxQueryHandler auxQueryHandler
	) {
		this.tasktableAdapter = tasktableAdapter;
		this.typeAdapter = typeAdapter;
		this.appCatClient = appCatClient;
		this.gracePeriodHandler = gracePeriodHandler;
		this.settings = settings;
		this.errorAppender = errorAppender;
		this.publisher = publisher;
		this.jobOrderTemplate = jobOrderTemplate;
		this.metadataQueries = metadataQueries;
		this.tasks = tasks;
		this.auxQueryHandler = auxQueryHandler;				
	}

	@Override
	public final void run() {
		try {
			final AppDataJob job = next(tasktableAdapter.file().getName());
			if (job == null) {
				LOGGER.trace("Found no applicable job to handle");
				return;
			}
			try {			
				final JobGen jobGenOld = typeAdapter.newJobGenFor(
						job, 
						tasktableAdapter, 
						publisher,
						jobOrderTemplate,
						metadataQueries,
						tasks,
						auxQueryHandler
				);			
				final JobGen jobGenNew = JobGenerationStateTransitions.ofInputState(jobGenOld.state())
						.performTransitionOn(jobGenOld);
						
				update(jobGenOld.job().getGeneration(), jobGenNew.job());
			}
			catch (final Exception e) {
				final Throwable error = Exceptions.unwrap(e);
				
				final List<GenericMessageDto<?>> messages = new ArrayList<>(job.getMessages());
				
				final String ids = messages.stream()
						.map(m -> String.valueOf(m.getId()))
						.collect(Collectors.joining(", "));	
	
				LOGGER.error("Error on handling job {} and creating failed request for it's messages {}: {}",
						job.getId(), ids, LogUtils.toString(error));

				errorAppender.send(new FailedProcessingDto(
						settings.getHostname(), 
						new Date(), 
						LogUtils.toString(error), 
						messages
				));
				// only delete job, if error request has been created successfully
				appCatClient.deleteJob(job);	
			}		
			// TODO check if it makes sense to evaluate the error counter here to limit the amount of
			// failed transition attempts	
		} 
		// on app-cat connection issues
		catch (final AbstractCodedException e) {
			// no use for further actions here as app-cat is mandatory for operation. Hence, we simply dump the message
			// and wait for the next attemt
			final String errorMessage = Exceptions.messageOf(e);
			LOGGER.error("Omitting job generation attempt due to error on app-cat access: {}", errorMessage);
		}
		// on any other error
		catch (final Exception e) {
			// also skip to t iteration here as it is likely induced by temporal problems (e.g. kafka down)
			final String errorMessage = Exceptions.messageOf(e);
			LOGGER.error("Omitting job generation attempt due to unexpected error: {}", errorMessage);
		}
	}

	
	private final AppDataJob next(final String tasktableName) throws AbstractCodedException {
		final List<AppDataJob> jobs = appCatClient.findJobInStateGeneratingForPod(
				tasktableName, 
				settings.getHostname()
		);
		
		if (jobs == null || jobs.isEmpty()) {
			LOGGER.trace("==  no job found in AppCatalog for taskTableXmlName {}", tasktableName);
			return null;
		}		
		
		for (final AppDataJob appDataJob : jobs) {			
			final AppDataJobGeneration jobGen = appDataJob.getGeneration();
			
			// check if grace period for state INITIAL and PRIMARY_CHECK is exceeded	
			if (gracePeriodHandler.isWithinGracePeriod(jobGen)) {
				LOGGER.trace("Job {} is still in grace period...", appDataJob.getId());
				continue;
			}
			LOGGER.trace("Found job {} to handle", appDataJob);
			return appDataJob;
		}
		// no job found
		return null;
	}
	
	private final AppDataJob update(
			final AppDataJobGeneration oldGeneration,
			final AppDataJob job
	) throws AbstractCodedException {
		final AppDataJobGeneration patchGen = job.getGeneration();
        patchGen.setLastUpdateDate(new Date());
        
        // is finished?
        if (patchGen.getState() == AppDataJobGenerationState.SENT) {
        	LOGGER.info("Finished job generation for appDataJob {}", job.getId());
        	job.setState(AppDataJobState.TERMINATED);  
        	job.setLastUpdateDate(new Date());
        } 
        // only update the state if it has changed
        else if (oldGeneration.getState() != patchGen.getState()) {
        	LOGGER.info("AppDataJob {} changed from {} to {}", job.getId(), oldGeneration.getState(), patchGen.getState());
        	patchGen.setNbErrors(0);
        	job.setLastUpdateDate(new Date());
        }
        // state did not change? only update modification date and increment error counter
        else {
        	LOGGER.info("AppDataJob {} no transition, staying in {}", job.getId(), oldGeneration.getState());
        	patchGen.setNbErrors(oldGeneration.getNbErrors()+1);   
        	// don't update jobs last modified date here to enable timeout
        }
      	job.setGeneration(patchGen);	
      	return appCatClient.updateJob(job);
	}
}
