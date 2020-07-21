package esa.s1pdgs.cpoc.ipf.preparation.worker.appcat;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.generator.GracePeriodHandler;
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

	public AppDataJob next(final String tasktableName) throws AbstractCodedException {
		final List<AppDataJob> jobs = appCatClient.findJobInStateGenerating(tasktableName);
		
		if (jobs == null || jobs.isEmpty()) {
			LOG.trace("==  no job found in AppCatalog for taskTableXmlName {}", tasktableName);
			return null;
		}		
		
		for (final AppDataJob appDataJob : jobs) {			
			final AppDataJobGeneration jobGen = appDataJob.getGeneration();
			
			// check if grace period for state INITIAL and PRIMARY_CHECK is exceeded	
			if (gracePeriodHandler.isWithinGracePeriod(jobGen)) {
				LOG.debug("Job {} is still in grace period...", appDataJob.getId());
				continue;
			}
			LOG.trace("Found job {} to handle", appDataJob);
			return appDataJob;
		}
		// no job found
		return null;
	}
	
	public void performUpdateOnJob() {
		
	}	
	
	public Optional<AppDataJob> findJobFor(final GenericMessageDto<CatalogEvent> mqiMessage) 
			throws AbstractCodedException {
		return first(
				appCatClient.findByMessagesId(mqiMessage.getId()), 
				String.format("mqiMessage %s", mqiMessage.getId())
		);	
	}
	
	public final Optional<AppDataJob> findJobForSession(final String sessionId) throws AbstractCodedException {
		return first(
				appCatClient.findByProductSessionId(sessionId), 
				String.format("session %s", sessionId)
		);	
	}
	
	public final Optional<AppDataJob> findJobForDatatakeId(final String dataTakeId) throws AbstractCodedException {
		return first(
				appCatClient.findByProductDataTakeId(dataTakeId), 
				String.format("dataTakeId %s", dataTakeId)
		);	
	}
	
	private final Optional<AppDataJob> first(final List<AppDataJob> result, final String desc) {
		if (result == null || result.isEmpty()) {
			LOG.debug("No AppDataJob found for {}", desc);
			return Optional.empty();
		}
		// just an assertion to identify possible problems -> should not happen
		if (result.size() != 1) {
			final String jobIds = result.stream()
					.map(j -> String.valueOf(j.getId()))
					.collect(Collectors.joining(", "));
			LOG.warn("More than one AppDataJob found for {}: {}", desc, jobIds);
			// TODO: check how to deal with this problem
			// for the time being: fall through
		}	
		return Optional.of(result.get(0));	
	}

	public final AppDataJob terminate(final AppDataJob job) throws AbstractCodedException {
    	LOG.info("Terminating appDataJob {} for product {}", job.getId(), job.getProductName());
    	job.setState(AppDataJobState.TERMINATED);  
    	return appCatClient.updateJob(job);
	}
	

	public final synchronized void appendMessage(final long id, final GenericMessageDto<CatalogEvent> mess) 
			throws AppCatJobUpdateFailed {

	}


	public final synchronized void updateProduct(final long id, final Product queried, final AppDataJobGenerationState outputState) 
			throws AppCatJobUpdateFailed {
		
	}

	public final synchronized void updateAux(final long id, final List<AppDataJobTaskInputs> queried, final AppDataJobGenerationState outputState) 
			throws AppCatJobUpdateFailed {
		
	}
	
	public final synchronized void updateSend(final long id, final AppDataJobGenerationState outputState) 
			throws AppCatJobUpdateFailed {
		
	}
	
}



