package esa.s1pdgs.cpoc.ipf.preparation.worker.appcat;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

@Component
public class AppCatJobService {
	private static final Logger LOG = LogManager.getLogger(AppCatJobService.class);
	
	private final AppCatalogJobClient appCatClient;
    private final GracePeriodHandler gracePeriodHandler;

	@Autowired
	public AppCatJobService(final AppCatalogJobClient appCatClient, final GracePeriodHandler gracePeriodHandler) {
		this.appCatClient = appCatClient;
		this.gracePeriodHandler = gracePeriodHandler;
	}
	
	public final AppDataJob create(final AppDataJob job) throws AbstractCodedException {
		LOG.debug("creating new job for {}", job.getProductName());
		return appCatClient.newJob(job);
	}   

	public AppDataJob next(final String tasktableName, final String processingGroup) throws AbstractCodedException {		
		for (final AppDataJob appDataJob : nextForTasktable(tasktableName)) {			
			final AppDataJobGeneration jobGen = appDataJob.getGeneration();
			
			// Only process AppDataJobs of the configured processingGroup
			if (processingGroup != null) {
				if (!processingGroup.equals(appDataJob.getProcessingGroup())) {
					LOG.trace("Job {} not processed because it belongs to another processing group ({} != {})",
							appDataJob.getId(), processingGroup, appDataJob.getProcessingGroup());
					continue;
				}
			}
			// check if grace period for state INITIAL, PRIMARY_CHECK and SENT is exceeded
			if (gracePeriodHandler.isWithinGracePeriod(new Date(),jobGen)) {
				LOG.debug("Job {} is still in grace period...", appDataJob.getId());
				continue;
			}
			LOG.trace("Found job {} to handle", appDataJob);
			return appDataJob;
		}
		// no job found
		return null;
	}
	
	public Optional<List<AppDataJob>> findJobsFor(final GenericMessageDto<CatalogEvent> mqiMessage) 
			throws AbstractCodedException {
		return returnOrEmpty(
				appCatClient.findByMessagesId(mqiMessage.getId()), 
				String.format("mqiMessage %s", mqiMessage.getId())
		);	
	}
	
	public Optional<List<AppDataJob>> findJobsForProductType(final String productType) throws AbstractCodedException {
		return returnOrEmpty(
				appCatClient.findByProductType(productType), 
				String.format("productType %s", productType)
		);
	}
	
	public final Optional<AppDataJob> findJobForSession(final String sessionId) throws AbstractCodedException {
		return first(
				appCatClient.findByProductSessionId(sessionId), 
				String.format("session %s", sessionId)
		);	
	}
	
	public final Optional<List<AppDataJob>> findJobsForDatatakeId(
			final String dataTakeId,
			final String productType
	) throws AbstractCodedException {
		return returnOrEmpty(
				appCatClient.findByProductDataTakeId(productType, dataTakeId), 
				String.format("dataTakeId %s", dataTakeId)
		);	
	}
	
	public final AppDataJob terminate(final AppDataJob job) throws AbstractCodedException {
    	LOG.info("Terminating appDataJob {} for product {}", job.getId(), job.getProductName());
    	job.setState(AppDataJobState.TERMINATED);  
    	return appCatClient.updateJob(job);
	}
	
	private final synchronized void performUpdate(final UpdateFunction command, final long id, final String name) 
			throws AppCatJobUpdateFailed {
		try {
			final AppDataJob job = findOrFail(id);
			command.applyUpdateOn(job);
			appCatClient.updateJob(job);			
		} catch (final AbstractCodedException e) {
			final String message = String.format(
					"Error on appDataJob %s %s update: %s. Trying next time...", 
					id, 
					name, 
					Exceptions.messageOf(e)
			);
			throw new AppCatJobUpdateFailed(message, e);
		}
	}

	public final void appendMessage(final long id, final GenericMessageDto<CatalogEvent> mess) 
			throws AppCatJobUpdateFailed {		
		performUpdate(
				job -> {
					job.getMessages().add(mess);
					job.setLastUpdateDate(new Date());
				}, 
				id, 
				"message"
		);
	}


	public final void updateProduct(final long id, final Product queried, final AppDataJobGenerationState outputState, final boolean timedOut) 
			throws AppCatJobUpdateFailed {
		performUpdate(
				job -> {					
					job.setTimedOut(timedOut);
					if (queried != null) {
						final AppDataJobProduct prod = queried.toProduct();
						job.setProduct(prod);						
						job.setAdditionalInputs(queried.overridingInputs());
						job.setPreselectedInputs(queried.preselectedInputs());
						
						// dirty workaround for segment and session scenario
						final AppDataJobProductAdapter productAdapter = new AppDataJobProductAdapter(prod);
						job.setStartTime(productAdapter.getStartTime());
						job.setStopTime(productAdapter.getStopTime());
					}
					
					// Before updating the state -> save last state
					job.getGeneration().setPreviousState(job.getGeneration().getState());
					
					// no transition?
					if (job.getGeneration().getState() == outputState) {
						// don't update jobs last modified date here to enable timeout, just update the generations 
						// last update time
						job.getGeneration().setLastUpdateDate(new Date());		
						job.getGeneration().setNbErrors(job.getGeneration().getNbErrors()+1);
					}
					else {
						job.getGeneration().setState(outputState);
						job.setLastUpdateDate(new Date());
					}
				}, 
				id, 
				"inputProduct"
		);
	}

	public final void updateAux(final long id, final List<AppDataJobTaskInputs> queried, final AppDataJobGenerationState outputState) 
			throws AppCatJobUpdateFailed {
		performUpdate(
				job -> {
					if (!queried.isEmpty()) {
						job.setAdditionalInputs(queried);	
					}
					
					// Before updating the state -> save last state
					job.getGeneration().setPreviousState(job.getGeneration().getState());
					
					// no transition?
					if (job.getGeneration().getState() == outputState) {
						// don't update jobs last modified date here to enable timeout, just update the generation time
						job.getGeneration().setLastUpdateDate(new Date());
						job.getGeneration().setNbErrors(job.getGeneration().getNbErrors()+1);
					}
					else {
						job.getGeneration().setState(outputState);
						job.setLastUpdateDate(new Date());
					}
				}, 
				id, 
				"auxProduct"
		);
	}
	
	public final void updateSend(final long id, final AppDataJobGenerationState outputState) 
			throws AppCatJobUpdateFailed {
		performUpdate(
				job -> {
					// no transition?
					if (job.getGeneration().getState() == outputState) {
						// Before updating the state -> save last state
						job.getGeneration().setPreviousState(job.getGeneration().getState());
						
						// don't update jobs last modified date here to enable timeout, just update the generation time
						job.getGeneration().setLastUpdateDate(new Date());
						job.getGeneration().setNbErrors(job.getGeneration().getNbErrors()+1);
					}
					else {		
						// set the previous state to output state in order to wait before termination
						job.getGeneration().setPreviousState(outputState);
						job.getGeneration().setState(outputState);
						job.setLastUpdateDate(new Date());
					}
				}, 
				id, 
				"send"
		);
	}
	
	public void updateToTerminate(final long id, final AppDataJobGenerationState outputState) {
		performUpdate(
				job -> {
					// complete job 
			    	LOG.info("Finishing appDataJob {} for product {}", job.getId(), job.getProductName());
			    	job.getGeneration().setState(outputState);
			    	job.setState(AppDataJobState.TERMINATED);  
				}, 
				id, 
				"wait"
		);		
	}
		
	private final AppDataJob findOrFail(final long jobId) throws AbstractCodedException {
		final AppDataJob result = appCatClient.findById(jobId);
		if (result == null) {
			throw new InternalErrorException(String.format("Job %s is null", String.valueOf(jobId)));
		}
		return result;
	}
	
	private final Optional<AppDataJob> first(final List<AppDataJob> result, final String desc) {
		if (isEmpty(result)) {
			LOG.debug("No AppDataJob found for {}", desc);
			return Optional.empty();
		}
		return Optional.of(result.get(0));	
	}
	
	private final List<AppDataJob> nextForTasktable(final String tasktableName)  throws AbstractCodedException {
		final List<AppDataJob> jobs = appCatClient.findJobInStateGenerating(tasktableName);
		
		if (isEmpty(jobs)) {
			LOG.trace("==  no job found in AppCatalog for taskTableXmlName {}", tasktableName);
			return Collections.emptyList();
		}	
		return jobs;		
	}

	private final boolean isEmpty(final List<AppDataJob> jobs) {
		return jobs == null || jobs.isEmpty();
	}
	
	private final Optional<List<AppDataJob>> returnOrEmpty(final List<AppDataJob> result, final String desc) {
		if (isEmpty(result)) {
			LOG.debug("No AppDataJob found for {}", desc);
			return Optional.empty();
		}
		return Optional.of(result);	
	}


}



