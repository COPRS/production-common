package esa.s1pdgs.cpoc.preparation.worker.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.preparation.worker.db.AppDataJobRepository;
import esa.s1pdgs.cpoc.preparation.worker.db.SequenceDao;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.AppCatJobUpdateFailedException;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.AppCatalogJobNotFoundException;

@Service
public class AppCatJobService {

	private static final Logger LOGGER = LogManager.getLogger(AppCatJobService.class);

	public static final String JOB_SEQ_KEY = "appDataJob";

	private final AppDataJobRepository appDataJobRepository;
	private final SequenceDao sequenceDao;

	public AppCatJobService(final AppDataJobRepository appDataJobDao, final SequenceDao sequenceDao) {
		this.appDataJobRepository = appDataJobDao;
		this.sequenceDao = sequenceDao;
	}

	public AppDataJob newJob(final AppDataJob newJob) {
		final long sequence = sequenceDao.getNextSequenceId(JOB_SEQ_KEY);
		LOGGER.debug("Creating new appDataJob {}", sequence);
		newJob.setId(sequence);
		newJob.setCreationDate(new Date());
		newJob.setLastUpdateDate(new Date());
		return appDataJobRepository.save(newJob);
	}

	public AppDataJob getJob(final Long identifier) throws AppCatalogJobNotFoundException {
		return appDataJobRepository.findById(identifier)
				.orElseThrow(() -> new AppCatalogJobNotFoundException(identifier));
	}

	public List<AppDataJob> findByStateAndLastUpdateDateLessThan(final AppDataJobState state,
			final ApplicationLevel level, final Date lastUpdateDate) {
		return appDataJobRepository.findByStateAndLevelAndLastUpdateDateLessThan(state, level, lastUpdateDate);
	}

	public List<AppDataJob> findByCatalogEventsUid(final UUID uid) {
		return appDataJobRepository.findByCatalogEventsUid(uid.toString());
	}

	public List<AppDataJob> findByProductType(final String productType) {
		return appDataJobRepository.findByProductType(productType);
	}

	public List<AppDataJob> findByProductSessionId(final String sessionId) {
		return appDataJobRepository.findByProductSessionId(sessionId);
	}

	public List<AppDataJob> findByProductDataTakeId(final String productType, final String dataTakeId) {
		// sorry, dirty workaround to identify RFC jobs
		if (productType.toUpperCase().equals("RF_RAW__0S")) {
			return appDataJobRepository.findByProductDataTakeId_Rfc(dataTakeId);
		}
		return appDataJobRepository.findByProductDataTakeId_NonRfc(dataTakeId);
	}

	public List<AppDataJob> findJobInStateGenerating(final String taskTable) {
		return appDataJobRepository.findJobInStateGenerating(taskTable);
	}

	public void appendCatalogEvent(final long id, final CatalogEvent event) throws AppCatJobUpdateFailedException {
		try {
			AppDataJob job = getJob(id);
			job.getCatalogEvents().add(event);
			job.setLastUpdateDate(new Date());
			updateJob(job);
		} catch (AppCatalogJobNotFoundException e) {
			final String message = String.format(
					"Error on appDataJob %s %s update: %s. Trying next time...", 
					id, 
					"appendCatalogEvent", 
					Exceptions.messageOf(e)
			);
			throw new AppCatJobUpdateFailedException(message, e);
		}
	}

	public void deleteJob(final Long jobId) {
		LOGGER.debug("Deleting appDataJob {}", jobId);
		appDataJobRepository.deleteById(jobId);
	}

	public AppDataJob updateJob(final AppDataJob patchJob) throws AppCatalogJobNotFoundException {
		// assert job exists
		getJob(patchJob.getId());
		LOGGER.debug("Updating appDataJob {}", patchJob.getId());
		return appDataJobRepository.save(patchJob);
	}
}
