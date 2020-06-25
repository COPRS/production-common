package esa.s1pdgs.cpoc.appcatalog.server.service;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobRepository;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobNotFoundException;
import esa.s1pdgs.cpoc.appcatalog.server.sequence.db.SequenceDao;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.filter.FilterCriterion;

/**
 * @author Viveris Technologies
 */
@Service
public class AppDataJobService {
    private static final Logger LOGGER = LogManager.getLogger(AppDataJobService.class);
	
    public static final String JOB_SEQ_KEY = "appDataJob";
    
    private final AppDataJobRepository appDataJobDao;
    private final SequenceDao sequenceDao;

    @Autowired
    public AppDataJobService(
    		final AppDataJobRepository appDataJobDao,
            final SequenceDao sequenceDao
    ) {
        this.appDataJobDao = appDataJobDao;
        this.sequenceDao = sequenceDao;
    }
    
    public AppDataJob newJob(final AppDataJob newJob) {    	
        final long sequence = sequenceDao.getNextSequenceId(JOB_SEQ_KEY);
        LOGGER.debug("Creating new appDataJob {}", sequence);
        newJob.setId(sequence);
        newJob.setState(AppDataJobState.WAITING);
        newJob.setCreationDate(new Date());
        newJob.setLastUpdateDate(null);
        return appDataJobDao.save(newJob);
    }

    public AppDataJob getJob(final Long identifier) throws AppCatalogJobNotFoundException {
        return appDataJobDao.findById(identifier).orElseThrow(
                () -> new AppCatalogJobNotFoundException(identifier));
    }

    public List<AppDataJob> search(final List<FilterCriterion> filters, final Sort sort) {
        return appDataJobDao.search(filters, sort);
    }

    public List<AppDataJob> findByStateAndLastUpdateDateLessThan(
    		final AppDataJobState state, 
    		final ApplicationLevel level,
            final Date lastUpdateDate
    ) {
        return appDataJobDao.findByStateAndLevelAndLastUpdateDateLessThan(
        		state, 
        		level, 
        		lastUpdateDate
        );
    }

    public void deleteJob(final Long jobId) {
        LOGGER.debug("Deleting appDataJob {}", jobId);
        appDataJobDao.deleteById(jobId);
    }

    public AppDataJob updateJob(final AppDataJob patchJob) throws AppCatalogJobNotFoundException {
    	// assert job exists
        getJob(patchJob.getId());
        LOGGER.debug("Updating appDataJob {}", patchJob);
        patchJob.setLastUpdateDate(new Date());
        return update(patchJob);
    }


    public AppDataJob updateJobGeneration(
    		final Long jobId,
            final AppDataJobGeneration patchGen
    ) throws AppCatalogJobNotFoundException {

        final AppDataJob job = getJob(jobId);
        final AppDataJobGeneration oldGeneration = job.getGeneration();        
        patchGen.setLastUpdateDate(new Date());
        
        // is finished?
        if (patchGen.getState() == AppDataJobGenerationState.SENT) {
        	LOGGER.info("Finished job generation for appDataJob {}", jobId);
        	job.setState(AppDataJobState.TERMINATED);  
        	job.setLastUpdateDate(new Date());
        } 
        // only update the state if it has changed
        else if (oldGeneration.getState() != patchGen.getState()) {
        	LOGGER.info("AppDataJob {} changed from {} to {}", jobId, oldGeneration.getState(), patchGen.getState());
        	patchGen.setNbErrors(0);
        	job.setLastUpdateDate(new Date());
        }
        // state did not change? only update modification date and increment error counter
        else {
        	LOGGER.info("AppDataJob {} no transition, staying in {}", jobId, oldGeneration.getState());
        	patchGen.setNbErrors(oldGeneration.getNbErrors()+1);        	
        }
      	job.setGeneration(patchGen);  
      	return update(job);
    }

    private final AppDataJob update(final AppDataJob job) {
    	   return appDataJobDao.save(job);
    }
}
