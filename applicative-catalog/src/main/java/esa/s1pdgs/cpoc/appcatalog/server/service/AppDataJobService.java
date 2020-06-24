package esa.s1pdgs.cpoc.appcatalog.server.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobRepository;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationInvalidTransitionStateException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationNotFoundException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationTerminatedException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobNotFoundException;
import esa.s1pdgs.cpoc.appcatalog.server.sequence.db.SequenceDao;
import esa.s1pdgs.cpoc.common.filter.FilterCriterion;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 * @author Viveris Technologies
 */
@Service
public class AppDataJobService {
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
            final Date lastUpdateDate
    ) {
        return appDataJobDao.findByStateAndLastUpdateDateLessThan(state, lastUpdateDate);
    }

    public void deleteJob(final Long jobId) {
        appDataJobDao.deleteById(jobId);
    }
    
    public AppDataJob addMessageToJob(final Long jobId, final GenericMessageDto<CatalogEvent> mess)
    		throws AppCatalogJobNotFoundException {
        final AppDataJob jobDb = getJob(jobId);
        jobDb.getMessages().add(mess);
        return appDataJobDao.save(jobDb);
    }

    /**
     * Patch a job
     * 
     * @param jobId
     * @param state
     * @param pod
     * @return
     * @throws AppCatalogJobNotFoundException
     */
    public AppDataJob patchJob(final Long jobId, final AppDataJob patchJob) throws AppCatalogJobNotFoundException {
        // Find if job exists
        final AppDataJob jobDb = getJob(jobId);

        boolean update = true;

        // Update job general details
        if (jobDb.getState() != patchJob.getState()) {
            update = true;
            jobDb.setState(patchJob.getState());
        }
        if (!jobDb.getPod().equals(patchJob.getPod())) {
            update = true;
            jobDb.setPod(patchJob.getPod());
        }
        
        jobDb.setPrepJobInputQueue(patchJob.getPrepJobInputQueue());
        jobDb.setPrepJobMessageId(patchJob.getPrepJobMessageId());
        jobDb.setReportingId(patchJob.getReportingId());

        // Update message if needed
        if (!CollectionUtils.isEmpty(patchJob.getMessages())) {
            update = true;
            jobDb.setMessages(patchJob.getMessages());
        }

        // Update product if needed
        if (patchJob.getProduct() != null) {
            // Note: we can replace all objects because the DTO and DB objects
            // have the same fields
            update = true;
            jobDb.setProduct(patchJob.getProduct());
        }

        // Update generations if needed
        if (!CollectionUtils.isEmpty(patchJob.getGenerations())) {
            // Note: we can replace all objects because the DTO and DB objects
            // have the same fields
            update = true;
            jobDb.setGenerations(patchJob.getGenerations());
        }

        if (update) {
            jobDb.setLastUpdateDate(new Date());
        }

        return appDataJobDao.save(jobDb);
    }

    public AppDataJob patchGenerationToJob(
    		final Long jobId,
            final String taskTable, 
            final AppDataJobGeneration patchGen
    ) throws AppCatalogJobNotFoundException,
            AppCatalogJobGenerationTerminatedException,
            AppCatalogJobGenerationInvalidTransitionStateException,
            AppCatalogJobGenerationNotFoundException {
    	
        // Find if job exists
        final AppDataJob jobDb = appDataJobDao.findById(jobId)
                .orElseThrow(() -> new AppCatalogJobNotFoundException(jobId));

        // Find the right generation and update it
        final List<AppDataJobGeneration> genForTasktable = jobDb.getGenerations().stream()
        		.filter(g -> g.getTaskTable().equals(taskTable))
        		.collect(Collectors.toList());
        
        if (genForTasktable.isEmpty()) {
            throw new AppCatalogJobGenerationNotFoundException(jobId, taskTable);
        }        
        final AppDataJobGeneration foundGenDb = genForTasktable.get(0);

        // Update state only if it has changed
        if (foundGenDb.getState() != patchGen.getState()) {
            switch (foundGenDb.getState()) {
                case INITIAL:
                    if (patchGen.getState() != AppDataJobGenerationState.PRIMARY_CHECK) {
                        throw new AppCatalogJobGenerationInvalidTransitionStateException(
                                foundGenDb.getState().name(),
                                patchGen.getState().name());
                    }
                    break;
                case PRIMARY_CHECK:
                    if (patchGen
                            .getState() != AppDataJobGenerationState.READY) {
                        throw new AppCatalogJobGenerationInvalidTransitionStateException(
                                foundGenDb.getState().name(),
                                patchGen.getState().name());
                    }
                    break;
                case READY:
                    if (patchGen.getState() != AppDataJobGenerationState.SENT) {
                        throw new AppCatalogJobGenerationInvalidTransitionStateException(
                                foundGenDb.getState().name(),
                                patchGen.getState().name());
                    }
                    break;
                default:
                    break;
            }
        }

        // Update
        if (patchGen.getState() == AppDataJobGenerationState.SENT) {
            return terminateGeneration(jobDb, foundGenDb);
        } 
        if (foundGenDb.getState() == patchGen.getState()) {
            final int currentNbErrors = foundGenDb.getNbErrors();
            foundGenDb.setLastUpdateDate(new Date());
            foundGenDb.setNbErrors(currentNbErrors + 1);
            foundGenDb.setState(patchGen.getState());
            return appDataJobDao.updateJobGeneration(jobDb.getId(), foundGenDb);
        }
        foundGenDb.setState(patchGen.getState());
        foundGenDb.setLastUpdateDate(new Date());
        foundGenDb.setNbErrors(0);
        return appDataJobDao.updateJobGeneration(jobDb.getId(), foundGenDb);
    }

    private AppDataJob terminateGeneration(
    		final AppDataJob jobDb,
            final AppDataJobGeneration genDb
    ) throws AppCatalogJobNotFoundException {
        // Update gen
        genDb.setState(AppDataJobGenerationState.SENT);
        genDb.setLastUpdateDate(new Date());
        genDb.setNbErrors(0);
        final AppDataJob refreshJobDb = appDataJobDao.updateJobGeneration(jobDb.getId(), genDb);

        boolean terminated = true;
        for (final AppDataJobGeneration gen : refreshJobDb.getGenerations()) {
            if (gen.getState() != AppDataJobGenerationState.SENT) {
                terminated = false;
            }
        }
        // Terminate job if needed
        if (terminated) {
            refreshJobDb.setState(AppDataJobState.TERMINATED);
            refreshJobDb.setLastUpdateDate(new Date());
            return appDataJobDao.save(refreshJobDb);
        }
        return refreshJobDb;
    }
}
