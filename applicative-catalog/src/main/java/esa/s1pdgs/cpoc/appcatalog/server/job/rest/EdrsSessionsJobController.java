package esa.s1pdgs.cpoc.appcatalog.server.job.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDtoState;
import esa.s1pdgs.cpoc.appcatalog.server.job.converter.JobConverter;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobService;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;

/**
 * Controller for managing jobs of product levels
 * 
 * @author Viveris Technologies
 */
@RestController
@RequestMapping(path = "/edrs_sessions/jobs")
public class EdrsSessionsJobController extends JobController<EdrsSessionDto> {

    /**
     * Constructor
     * 
     * @param appDataJobService
     */
    @Autowired
    public EdrsSessionsJobController(final AppDataJobService appDataJobService,
            @Value("${jobs.edrs-sessions.generations.max-errors-initial}") final int maxErrorInitial,
            @Value("${jobs.edrs-sessions.generations.max-errors-primary-check}") final int maxErrorPrimaryCheck) {
        super(appDataJobService, new EdrsSessionJobConverter(),
                ProductCategory.EDRS_SESSIONS);
        this.getMaxNbErrors().put(AppDataJobGenerationDtoState.INITIAL,
                Integer.valueOf(maxErrorInitial));
        this.getMaxNbErrors().put(AppDataJobGenerationDtoState.PRIMARY_CHECK,
                Integer.valueOf(maxErrorPrimaryCheck));
        this.getMaxNbErrors().put(AppDataJobGenerationDtoState.READY,
                Integer.valueOf(maxErrorPrimaryCheck));
        this.getMaxNbErrors().put(AppDataJobGenerationDtoState.SENT,
                Integer.valueOf(maxErrorPrimaryCheck));
    }

}

/**
 * Internal class for converting generic jobs for EDRS sessions
 * 
 * @author Viveris Technologies
 */
class EdrsSessionJobConverter extends JobConverter<EdrsSessionDto> {

    /**
     * Constructor
     */
    public EdrsSessionJobConverter() {
        super(ProductCategory.EDRS_SESSIONS);
    }
}
