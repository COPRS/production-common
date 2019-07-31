package esa.s1pdgs.cpoc.common;

import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;

/**
 * Group products per category
 * 
 * @author Viveris Technologie
 */
public enum ProductCategory {
    AUXILIARY_FILES(ProductDto.class), 
    EDRS_SESSIONS(EdrsSessionDto.class), 
    LEVEL_JOBS(LevelJobDto.class), 
    LEVEL_PRODUCTS(ProductDto.class), 
    LEVEL_REPORTS(LevelReportDto.class), 
    LEVEL_SEGMENTS(ProductDto.class), 
    COMPRESSED_PRODUCTS(ProductDto.class),
    INGESTION(IngestionDto.class);

    /**
     * Get the category for a given product family
     * 
     * @param family
     * @return
     * @throws InternalErrorException
     */
    public static ProductCategory fromProductFamily(final ProductFamily family)
            throws InternalErrorException {
        if (family == null) {
            throw new InternalErrorException(
                    "Cannot determinate product category for a null family");
        }
        ProductCategory ret = null;
        switch (family) {
            case AUXILIARY_FILE:
                ret = ProductCategory.AUXILIARY_FILES;
                break;
            case EDRS_SESSION:
                ret = ProductCategory.EDRS_SESSIONS;
                break;
            case INVALID: // --> failed ingestion    
            case BLANK: // --> nominal polling ingestion
            	ret = ProductCategory.INGESTION;
            	break;
            case L0_JOB:
            case L1_JOB:
            case L2_JOB:
            case L0_SEGMENT_JOB:
                ret = ProductCategory.LEVEL_JOBS;
                break;
            case L0_REPORT:
            case L1_REPORT:
            case L2_REPORT:            	
            case L0_SEGMENT_REPORT:
                ret = ProductCategory.LEVEL_REPORTS;
                break;
            case L0_ACN:
            case L0_SLICE:
            case L1_ACN:
            case L1_SLICE:
            case L0_BLANK:
            case L2_SLICE:
            case L2_ACN:
                ret = ProductCategory.LEVEL_PRODUCTS;
                break;
            case L0_SEGMENT:
                ret = ProductCategory.LEVEL_SEGMENTS;
                break;
            default:
                throw new InternalErrorException(
                        "Cannot determinate product category for family "
                                + family);
        }
        return ret;
    }
    
    private final Class<? extends AbstractDto> dtoClass;

	private ProductCategory(Class<? extends AbstractDto> dtoClass) {
		this.dtoClass = dtoClass;
	}
	
	@SuppressWarnings("unchecked")
	public <T> Class<T> getDtoClass() {
		return (Class<T>) dtoClass;
	}  
}
