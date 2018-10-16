package esa.s1pdgs.cpoc.appcatalog.server.job.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDtoState;
import esa.s1pdgs.cpoc.appcatalog.server.job.converter.JobConverter;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobService;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelSegmentDto;

/**
 * Controller for managing jobs of product levels
 * 
 * @author Viveris Technologies
 */
@RestController
@RequestMapping(path = "/level_segments/jobs")
public class LevelSegmentsJobController extends JobController<LevelSegmentDto> {

    /**
     * Constructor
     * 
     * @param appDataJobService
     */
    @Autowired
    public LevelSegmentsJobController(final AppDataJobService appDataJobService,
            @Value("${jobs.level-segments.generations.max-errors-initial}") final int maxErrorInitial,
            @Value("${jobs.level-segments.generations.max-errors-primary-check}") final int maxErrorPrimaryCheck) {
        super(appDataJobService, new LevelSegmentJobConverter(),
                ProductCategory.LEVEL_SEGMENTS);
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
 * Internal class for converting generic jobs for level products
 * 
 * @author Viveris Technologies
 */
class LevelSegmentJobConverter extends JobConverter<LevelSegmentDto> {

    /**
     * Constructor
     */
    public LevelSegmentJobConverter() {
        super(ProductCategory.LEVEL_SEGMENTS);
    }
}
