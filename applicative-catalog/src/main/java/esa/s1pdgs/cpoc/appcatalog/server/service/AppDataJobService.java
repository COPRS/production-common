package esa.s1pdgs.cpoc.appcatalog.server.service;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
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
        LOGGER.debug("Updating appDataJob {}", patchJob.getId());
        return appDataJobDao.save(patchJob);
    }
}
