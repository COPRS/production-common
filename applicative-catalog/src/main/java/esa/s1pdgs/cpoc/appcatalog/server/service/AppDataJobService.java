package esa.s1pdgs.cpoc.appcatalog.server.service;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobRepository;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobNotFoundException;
import esa.s1pdgs.cpoc.appcatalog.server.sequence.db.SequenceDao;
import esa.s1pdgs.cpoc.common.ApplicationLevel;

/**
 * @author Viveris Technologies
 */
@Service
public class AppDataJobService {
    private static final Logger LOGGER = LogManager.getLogger(AppDataJobService.class);
	
    public static final String JOB_SEQ_KEY = "appDataJob";
    
    private final AppDataJobRepository appDataJobRepository;
    private final SequenceDao sequenceDao;

    @Autowired
    public AppDataJobService(
    		final AppDataJobRepository appDataJobDao,
            final SequenceDao sequenceDao
    ) {
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
        return appDataJobRepository.findById(identifier).orElseThrow(
                () -> new AppCatalogJobNotFoundException(identifier));
    }

    public List<AppDataJob> findByStateAndLastUpdateDateLessThan(
    		final AppDataJobState state, 
    		final ApplicationLevel level,
            final Date lastUpdateDate
    ) {
        return appDataJobRepository.findByStateAndLevelAndLastUpdateDateLessThan(
        		state, 
        		level, 
        		lastUpdateDate
        );
    }
    
    public List<AppDataJob> findByMessagesId(final long messageId) {
    	return appDataJobRepository.findByMessagesId(messageId);
    }

    public List<AppDataJob> findByProductSessionId(final String sessionId) {
    	return appDataJobRepository.findByProductSessionId(sessionId);
    }
    
    public List<AppDataJob> findByProductDataTakeId(final String dataTakeId) {
    	return appDataJobRepository.findByProductDataTakeId(dataTakeId);
    }
    
    public List<AppDataJob> findJobInStateGenerating(final String taskTable) {
    	return appDataJobRepository.findJobInStateGenerating(taskTable);
    }

    public void deleteJob(final Long jobId) {
        LOGGER.debug("Deleting appDataJob {}", jobId);
        appDataJobRepository.deleteById(jobId);
    }

    public AppDataJob updateJob(final AppDataJob patchJob) throws AppCatalogJobNotFoundException {
    	// assert job exists
        final AppDataJob existingJob = getJob(patchJob.getId());
        // has been modified concurrently?
        // FIXME needs to be implemented in 940
//        if (existingJob.getLastUpdateDate().after(patchJob.getLastUpdateDate())) {
//        	throw new AppCatalogJobGenerationInvalidStateException();
//        }
        patchJob.setLastUpdateDate(new Date());
        
        LOGGER.debug("Updating appDataJob {}", patchJob.getId());
        return appDataJobRepository.save(patchJob);
    }
}
