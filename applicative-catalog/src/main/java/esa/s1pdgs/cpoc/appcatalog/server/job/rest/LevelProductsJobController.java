package esa.s1pdgs.cpoc.appcatalog.server.job.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDtoState;
import esa.s1pdgs.cpoc.appcatalog.server.job.converter.JobConverter;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobService;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;

/**
 * Controller for managing jobs of product levels
 * 
 * @author Viveris Technologies
 */
@RestController
@RequestMapping(path = "/level_products/jobs")
public class LevelProductsJobController extends JobController<ProductDto> {

    /**
     * Constructor
     * 
     * @param appDataJobService
     */
    @Autowired
    public LevelProductsJobController(final AppDataJobService appDataJobService,
            @Value("${jobs.level-products.generations.max-errors-initial}") final int maxErrorInitial,
            @Value("${jobs.level-products.generations.max-errors-primary-check}") final int maxErrorPrimaryCheck) {
        super(appDataJobService, new LevelProductJobConverter(),
                ProductCategory.LEVEL_PRODUCTS);
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
class LevelProductJobConverter extends JobConverter<ProductDto> {

    /**
     * Constructor
     */
    public LevelProductJobConverter() {
        super(ProductCategory.LEVEL_PRODUCTS);
    }
}
