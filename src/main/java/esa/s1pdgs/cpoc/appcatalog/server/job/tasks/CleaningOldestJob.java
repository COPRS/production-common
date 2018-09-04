package esa.s1pdgs.cpoc.appcatalog.server.job.tasks;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appcatalog.server.job.JobsProperties;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobService;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobState;
import esa.s1pdgs.cpoc.common.ProductCategory;

@Component
public class CleaningOldestJob {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(CleaningOldestJob.class);

    /**
     * Job service
     */
    private final AppDataJobService appDataJobService;

    /**
     * Maximal job age per status
     */
    private final Map<String, Long> maxAgeJobsLevelProducts;

    /**
     * Maximal job age per status
     */
    private final Map<String, Long> maxAgeJobsEdrsSessions;

    /**
     * @param appDataJobService
     * @param category
     * @param maxAgeJobs
     * @param maxNbErrorsGenerations
     */
    @Autowired
    public CleaningOldestJob(final AppDataJobService appDataJobService,
            final JobsProperties jobsProperties) {
        this.appDataJobService = appDataJobService;
        this.maxAgeJobsLevelProducts =
                jobsProperties.getEdrsSessions().getMaxAgeJobMs();
        this.maxAgeJobsEdrsSessions =
                jobsProperties.getLevelProducts().getMaxAgeJobMs();
    }

    /**
     * Clean job terminated after x times (to avoid being done twice)
     */
    @Scheduled(fixedDelayString = "${jobs.cleaning-jobs-terminated-fixed-rate-ms}")
    public void cleanJobInGeneratedState() {
        // EDRS sessions
        Date dateCompareE =
                new Date(System.currentTimeMillis() - maxAgeJobsEdrsSessions
                        .get(AppDataJobState.TERMINATED.name().toLowerCase()));
        List<AppDataJob> jobsE = appDataJobService
                .findByStateAndCategoryAndLastUpdateDateLessThan(
                        AppDataJobState.TERMINATED,
                        ProductCategory.EDRS_SESSIONS, dateCompareE);
        for (AppDataJob jobE : jobsE) {
            LOGGER.info(
                    "[productName {}] [level {}] Remove terminated job for enough time",
                    jobE.getProduct().getProductName(),
                    ProductCategory.EDRS_SESSIONS, jobE.getLevel());
            appDataJobService.deleteJob(jobE.getIdentifier());
        }
        // Level products
        Date dateCompareP =
                new Date(System.currentTimeMillis() - maxAgeJobsLevelProducts
                        .get(AppDataJobState.TERMINATED.name().toLowerCase()));
        List<AppDataJob> jobsP = appDataJobService
                .findByStateAndCategoryAndLastUpdateDateLessThan(
                        AppDataJobState.TERMINATED,
                        ProductCategory.LEVEL_PRODUCTS, dateCompareP);
        for (AppDataJob jobP : jobsP) {
            LOGGER.info(
                    "[productName {}] [level {}] Remove terminated job for enough time",
                    jobP.getProduct().getProductName(), jobP.getLevel());
            appDataJobService.deleteJob(jobP.getIdentifier());
        }
    }

    /**
     * Remove jobs in transitory state for too long
     */
    @Scheduled(fixedDelayString = "${jobs.cleaning-jobs-invalid-fixed-rate-ms}")
    public void cleanJobInWaitingForTooLong() {
        this.deleteJobsInTemporarlyStateForTooLong(AppDataJobState.WAITING);
        this.deleteJobsInTemporarlyStateForTooLong(AppDataJobState.DISPATCHING);
        this.deleteJobsInTemporarlyStateForTooLong(AppDataJobState.GENERATING);
    }

    /**
     * @param state
     */
    private void deleteJobsInTemporarlyStateForTooLong(AppDataJobState state) {
        // EDRS sessions
        Date dateCompareE = new Date(System.currentTimeMillis()
                - maxAgeJobsEdrsSessions.get(state.name().toLowerCase()));
        List<AppDataJob> jobsE = appDataJobService
                .findByStateAndCategoryAndLastUpdateDateLessThan(state,
                        ProductCategory.EDRS_SESSIONS, dateCompareE);
        for (AppDataJob jobE : jobsE) {
            LOGGER.info(
                    "[category {}] [productName {}] [level {}] [state {}] Remove transitory job for long time",
                    ProductCategory.EDRS_SESSIONS,
                    jobE.getProduct().getProductName(), jobE.getLevel(), state);
            appDataJobService.deleteJob(jobE.getIdentifier());
        }
        // Level products
        Date dateCompareP = new Date(System.currentTimeMillis()
                - maxAgeJobsEdrsSessions.get(state.name().toLowerCase()));
        List<AppDataJob> jobsP = appDataJobService
                .findByStateAndCategoryAndLastUpdateDateLessThan(state,
                        ProductCategory.LEVEL_PRODUCTS, dateCompareP);
        for (AppDataJob jobP : jobsP) {
            LOGGER.error(
                    "[category {}] [productName {}] [level {}] [state {}] Remove transitory job for long time",
                    ProductCategory.LEVEL_PRODUCTS,
                    jobP.getProduct().getProductName(), jobP.getLevel(), state);
            appDataJobService.deleteJob(jobP.getIdentifier());
        }
    }

    // TODO add clean for job generations
}
