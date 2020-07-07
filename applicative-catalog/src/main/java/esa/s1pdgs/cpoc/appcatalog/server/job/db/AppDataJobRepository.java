package esa.s1pdgs.cpoc.appcatalog.server.job.db;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.common.ApplicationLevel;

/**
 * Access class to AppDataJob in mongo DB
 * 
 * @author Viveris Technologies
 */
@Service
public interface AppDataJobRepository
        extends MongoRepository<AppDataJob, Long>, AppDataJobRepositoryCustom {

    public List<AppDataJob> findByStateAndLevelAndLastUpdateDateLessThan(
            final AppDataJobState state,
            final ApplicationLevel level,
            final Date lastUpdateDate
    );

    public List<AppDataJob> findByGenerationStateAndGenerationNbErrorsGreaterThanEqual(
            final AppDataJobGenerationState generationsState,
            final int nbErrors
    );
}
