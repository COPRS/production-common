package esa.s1pdgs.cpoc.common;

import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.queue.PripPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;

/**
 * Group products per category
 * 
 * @author Viveris Technologie
 */
public enum ProductCategory {
    AUXILIARY_FILES(CatalogEvent.class), 
    EDRS_SESSIONS(CatalogEvent.class), 
    LEVEL_JOBS(IpfExecutionJob.class), 
    LEVEL_PRODUCTS(CatalogEvent.class), 
    LEVEL_REPORTS(LevelReportDto.class), 
    LEVEL_SEGMENTS(CatalogEvent.class),
    COMPRESSION_JOBS(CompressionJob.class),
    COMPRESSED_PRODUCTS(CompressionEvent.class),
    INGESTION(IngestionJob.class),
    INGESTION_EVENT(IngestionEvent.class),
    PREPARATION_JOBS(IpfPreparationJob.class),    
    CATALOG_EVENT(CatalogEvent.class),
    CATALOG_JOBS(CatalogJob.class),
    PRODUCTION_EVENT(ProductionEvent.class),
    PRIP_JOBS(PripPublishingJob.class);
	
    /**
     * Get the category for a given product family.
     * 
     * @param family the family
     * @return ProductCategory for given family
     * @throws IllegalArgumentException if ProductFamily cannot be mapped to ProductCategory or if family is null
     * @see {@link #of(ProductFamily)}
     */
	public static ProductCategory of(final ProductFamily family) {
        if (family == null) {
            throw new IllegalArgumentException("Cannot determine product category for a null family");
        }
        switch (family) {
	        case AUXILIARY_FILE:
	            return ProductCategory.AUXILIARY_FILES;
	        case EDRS_SESSION:
	            return ProductCategory.EDRS_SESSIONS;
	        case INVALID: // --> failed ingestion    
	        case BLANK: // --> nominal polling ingestion
	        	return ProductCategory.INGESTION;
	        case L0_JOB:
	        case L1_JOB:
	        case L2_JOB:
	        case L0_SEGMENT_JOB:
	            return ProductCategory.LEVEL_JOBS;
	        case L0_REPORT:
	        case L1_REPORT:
	        case L2_REPORT:            	
	        case L0_SEGMENT_REPORT:
	            return ProductCategory.LEVEL_REPORTS;
	        case L0_ACN:
	        case L0_SLICE:
	        case L1_ACN:
	        case L1_SLICE:
	        case L0_BLANK:
	        case L2_SLICE:
	        case L2_ACN:
	            return ProductCategory.LEVEL_PRODUCTS;
	        case L0_SEGMENT:
	            return ProductCategory.LEVEL_SEGMENTS;
	            
			case AUXILIARY_FILE_ZIP:
			case L0_ACN_ZIP:
			case L0_BLANK_ZIP:
			case L0_SEGMENT_ZIP:
			case L0_SLICE_ZIP:
			case L1_ACN_ZIP:
			case L1_SLICE_ZIP:
			case L2_ACN_ZIP:
			case L2_SLICE_ZIP:
				return ProductCategory.COMPRESSION_JOBS; 
	        default:
	        	throw new IllegalArgumentException(
	        			String.format("Cannot determine product category for family %s", family)
	        	);
        }
	}

    /**
     * Get the category for a given product family
     * 
     * @param family
     * @return
     * @throws InternalErrorException
     * @see {@link #of(ProductFamily)}
     */
	@Deprecated
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
    
    private final Class<? extends AbstractMessage> dtoClass;

	private ProductCategory(final Class<? extends AbstractMessage> dtoClass) {
		this.dtoClass = dtoClass;
	}
	
	@SuppressWarnings("unchecked")
	public <T> Class<T> getDtoClass() {
		return (Class<T>) dtoClass;
	}  
}
