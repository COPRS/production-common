package esa.s1pdgs.cpoc.appcatalog.server.job.tasks;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.server.config.JobsProperties;
import esa.s1pdgs.cpoc.appcatalog.server.service.AppDataJobService;
import esa.s1pdgs.cpoc.common.ApplicationLevel;

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
     * Maximal job age per status
     */
    private final Map<String, Long> maxAgeJobsLevelSegments;

    /**
     */
    @Autowired
    public CleaningOldestJob(final AppDataJobService appDataJobService,
            final JobsProperties jobsProperties) {
        this.appDataJobService = appDataJobService;
        this.maxAgeJobsLevelProducts =
                jobsProperties.getEdrsSessions().getMaxAgeJobMs();
        this.maxAgeJobsEdrsSessions =
                jobsProperties.getLevelProducts().getMaxAgeJobMs();
        this.maxAgeJobsLevelSegments =
                jobsProperties.getLevelSegments().getMaxAgeJobMs();
    }

    /**
     * Clean job terminated after x times (to avoid being done twice)
     */
    @Scheduled(fixedDelayString = "${jobs.cleaning-jobs-terminated-fixed-rate-ms}")
    public void cleanJobInGeneratedState() {
        cleanJobsByState(AppDataJobState.TERMINATED, false);
    }

    /**
     * Remove jobs in transitory state for too long
     */
    @Scheduled(fixedDelayString = "${jobs.cleaning-jobs-invalid-fixed-rate-ms}")
    public void cleanJobInWaitingForTooLong() {
        cleanJobsByState(AppDataJobState.WAITING, true);
        cleanJobsByState(AppDataJobState.DISPATCHING, true);
        cleanJobsByState(AppDataJobState.GENERATING, true);
    }

    /**
     * 
     */
    private void cleanJobsByState(final AppDataJobState state, final boolean isError) {
        // EDRS sessions
        cleanJobsByStateAndCategory(
        		state, 
        		ApplicationLevel.L0,
                maxAgeJobsEdrsSessions.get(state.name().toLowerCase()),
                isError
        );
        // Level segments
        cleanJobsByStateAndCategory(
        		state, 
        		ApplicationLevel.L0_SEGMENT,
                maxAgeJobsLevelSegments.get(state.name().toLowerCase()),
                isError
        );        
        // Level products
        cleanJobsByStateAndCategory(
        		state, 
        		ApplicationLevel.L1,
                maxAgeJobsLevelProducts.get(state.name().toLowerCase()),
                isError
        );
        cleanJobsByStateAndCategory(
        		state, 
        		ApplicationLevel.L2,
                maxAgeJobsLevelProducts.get(state.name().toLowerCase()),
                isError
        );
        cleanJobsByStateAndCategory(
                state,
                ApplicationLevel.SPP_OBS,
                maxAgeJobsLevelProducts.get(state.name().toLowerCase()),
                isError
        );
    }

    /**
     * 
     */
    private void cleanJobsByStateAndCategory(
    		final AppDataJobState state,
    		final ApplicationLevel level,
            final long maxAge, 
            final boolean isError
     ) {
        final Date dateCompareS = new Date(System.currentTimeMillis() - maxAge);
        final List<AppDataJob> jobsS = appDataJobService.findByStateAndLastUpdateDateLessThan(
        		state, 
        		level, 
        		dateCompareS
        );
        for (final AppDataJob jobS : jobsS) {
        	final String logMessage = String.format(
        			"Remove %s job %s (%s) for enough time", 
        			jobS.getLevel(),
        			jobS.getId(),
        			state
        	);       	
            if (isError) {
                LOGGER.error(logMessage);
            } else {
                LOGGER.info(logMessage);
            }
            appDataJobService.deleteJob(jobS.getId());
        }
    }

    // TODO add clean for job generations
}
