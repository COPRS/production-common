package esa.s1pdgs.cpoc.production.trigger.appcat;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class AppCatAdapter {
	private static final Logger LOG = LogManager.getLogger(AppCatAdapter.class);
	
    private final AppCatalogJobClient appCatClient;

	public AppCatAdapter(final AppCatalogJobClient appCatClient) {
		this.appCatClient = appCatClient;
	}
	
	public final Optional<AppDataJob> findJobFor(final GenericMessageDto<CatalogEvent> mqiMessage) throws AbstractCodedException {
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
	
	public final AppDataJob create(final AppDataJob job) throws AbstractCodedException {
		return appCatClient.newJob(job);
	}   
		
	public final AppDataJob update(final AppDataJob job) throws AbstractCodedException {
		return appCatClient.updateJob(job);
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
}
