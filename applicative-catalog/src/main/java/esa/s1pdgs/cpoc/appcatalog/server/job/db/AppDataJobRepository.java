package esa.s1pdgs.cpoc.appcatalog.server.job.db;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.common.ProductCategory;

/**
 * Access class to AppDataJob in mongo DB
 * 
 * @author Viveris Technologies
 */
@Service
public interface AppDataJobRepository
        extends MongoRepository<AppDataJob, Long>, AppDataJobRepositoryCustom {

    /**
     * @param state
     * @param category
     * @param lastUpdateDate
     * @return
     */
    public List<AppDataJob> findByStateAndCategoryAndLastUpdateDateLessThan(
            final AppDataJobState state, final ProductCategory category,
            final Date lastUpdateDate);

    /**
     * @param category
     * @param generationsState
     * @param nbErrors
     * @return
     */
    public List<AppDataJob> findByCategoryAndGenerationsStateAndGenerationsNbErrorsGreaterThanEqual(
            final ProductCategory category,
            final AppDataJobGenerationState generationsState,
            final int nbErrors);
}
