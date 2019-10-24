package esa.s1pdgs.cpoc.appcatalog.server.job.db;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationInvalidTransitionStateException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationNotFoundException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationTerminatedException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobNotFoundException;
import esa.s1pdgs.cpoc.appcatalog.server.sequence.db.SequenceDao;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.filter.FilterCriterion;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 * @author Viveris Technologies
 */
@Service
public class AppDataJobService {

    /**
     * Key used for generating identifiers
     */
    public static final String JOB_SEQ_KEY = "appDataJob";

    /**
     * Dao for job
     */
    private final AppDataJobRepository appDataJobDao;

    /**
     * Dao for sequence
     */
    private final SequenceDao sequenceDao;

    /**
     * Constructor for the Services
     * 
     * @param mongoDBDAO
     */
    @Autowired
    public AppDataJobService(final AppDataJobRepository appDataJobDao,
            final SequenceDao sequenceDao) {
        this.appDataJobDao = appDataJobDao;
        this.sequenceDao = sequenceDao;
    }

    /**
     * Search for jobs
     * 
     * @param filters
     * @return
     */
    public List<AppDataJob> search(final List<FilterCriterion> filters,
            final ProductCategory category, final Sort sort) {
        return appDataJobDao.search(filters, category, sort);
    }

    /**
     * @param state
     * @param category
     * @param lastUpdateDate
     * @return
     */
    public List<AppDataJob> findByStateAndCategoryAndLastUpdateDateLessThan(
            final AppDataJobState state, final ProductCategory category,
            final Date lastUpdateDate) {
        return appDataJobDao.findByStateAndCategoryAndLastUpdateDateLessThan(
                state, category, lastUpdateDate);
    }

    /**
     * Get a job from its identifier
     * 
     * @param identifier
     * @return
     * @throws AppCatalogJobNotFoundException
     */
    public AppDataJob getJob(final Long identifier)
            throws AppCatalogJobNotFoundException {
        return appDataJobDao.findById(identifier).orElseThrow(
                () -> new AppCatalogJobNotFoundException(identifier));
    }

    /**
     * Create a job
     * 
     * @param newJob
     * @return
     */
    public AppDataJob newJob(final AppDataJob newJob) {
        // Check new job format TODO
        long sequence = sequenceDao.getNextSequenceId(JOB_SEQ_KEY);
        // Save it
        newJob.setId(sequence);
        newJob.setState(AppDataJobState.WAITING);
        newJob.setCreationDate(new Date());
        newJob.setLastUpdateDate(null);
        return appDataJobDao.save(newJob);
    }

    /**
     * Delete a job
     * 
     * @param jobId
     */
    public void deleteJob(final Long jobId) {
        appDataJobDao.deleteById(jobId);
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
    public AppDataJob patchJob(final Long jobId, final AppDataJob patchJob)
            throws AppCatalogJobNotFoundException {
        // Find if job exists
        AppDataJob jobDb = appDataJobDao.findById(jobId)
                .orElseThrow(() -> new AppCatalogJobNotFoundException(jobId));

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

    public AppDataJob patchGenerationToJob(final Long jobId,
            final String taskTable, final AppDataJobGeneration patchGen,
            final int maxNbErrors) throws AppCatalogJobNotFoundException,
            AppCatalogJobGenerationTerminatedException,
            AppCatalogJobGenerationInvalidTransitionStateException,
            AppCatalogJobGenerationNotFoundException {
        // Find if job exists
        AppDataJob<?> jobDb = appDataJobDao.findById(jobId)
                .orElseThrow(() -> new AppCatalogJobNotFoundException(jobId));

        // Find the right generation and update it
        AppDataJobGeneration foundGenDb = null;
        for (AppDataJobGeneration genDb : jobDb.getGenerations()) {
            if (genDb.getTaskTable().equals(taskTable)) {
                foundGenDb = genDb;
            }
        }

        if (foundGenDb == null) {
            throw new AppCatalogJobGenerationNotFoundException(jobId,
                    taskTable);
        }

        // Check format
        if (foundGenDb.getState() != patchGen.getState()) {
            switch (foundGenDb.getState()) {
                case INITIAL:
                    if (patchGen
                            .getState() != AppDataJobGenerationState.PRIMARY_CHECK) {
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
        } else {
            if (foundGenDb.getState() == patchGen.getState()) {
                int currentNbErrors = foundGenDb.getNbErrors();
                if (currentNbErrors + 1 >= maxNbErrors) {
                    terminateGeneration(jobDb, foundGenDb);
                    throw new AppCatalogJobGenerationTerminatedException(
                            jobDb.getProduct().getProductName(),
                            jobDb.getMessages().stream().map(s -> (GenericMessageDto<? extends AbstractDto>)s).collect(Collectors.toList()));
                } else {
                    foundGenDb.setLastUpdateDate(new Date());
                    foundGenDb.setNbErrors(currentNbErrors + 1);
                    foundGenDb.setState(patchGen.getState());
                    appDataJobDao.udpateJobGeneration(jobDb.getId(), foundGenDb);
                    return jobDb;
                }
            } else {
                foundGenDb.setState(patchGen.getState());
                foundGenDb.setLastUpdateDate(new Date());
                foundGenDb.setNbErrors(0);
                appDataJobDao.udpateJobGeneration(jobDb.getId(), foundGenDb);
                return jobDb;
            }
        }
    }

    private AppDataJob terminateGeneration(AppDataJob jobDb,
            AppDataJobGeneration genDb) throws AppCatalogJobNotFoundException {
        // Update gen
        genDb.setState(AppDataJobGenerationState.SENT);
        genDb.setLastUpdateDate(new Date());
        genDb.setNbErrors(0);
        appDataJobDao.udpateJobGeneration(jobDb.getId(), genDb);

        // Search if all generation are terminated
        AppDataJob<?> refreshJobDb = appDataJobDao.findById(jobDb.getId())
                .orElseThrow(() -> new AppCatalogJobNotFoundException(jobDb.getId()));
        boolean terminated = true;
        for (AppDataJobGeneration gen : refreshJobDb.getGenerations()) {
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
