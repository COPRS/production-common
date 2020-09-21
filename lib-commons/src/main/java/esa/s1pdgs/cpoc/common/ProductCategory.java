package esa.s1pdgs.cpoc.common;

import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.EvictionManagementJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LtaDownloadEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.PripPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;

/**
 * Group products per category
 * 
 * @author Viveris Technologie
 */
public enum ProductCategory {
    AUXILIARY_FILES(ProductionEvent.class), 
    EDRS_SESSIONS(IngestionEvent.class), 
    PLANS_AND_REPORTS(IngestionEvent.class),
    LEVEL_JOBS(IpfExecutionJob.class), 
    LEVEL_PRODUCTS(ProductionEvent.class), 
    LEVEL_REPORTS(LevelReportDto.class), 
    LEVEL_SEGMENTS(ProductionEvent.class),
    SPP_PRODUCTS(ProductionEvent.class),
    DEBUG(ProductionEvent.class),
    COMPRESSION_JOBS(CompressionJob.class),
    COMPRESSED_PRODUCTS(CompressionEvent.class),
    INGESTION(IngestionJob.class),
    INGESTION_EVENT(IngestionEvent.class),
    PREPARATION_JOBS(IpfPreparationJob.class),    
    CATALOG_EVENT(CatalogEvent.class),
    CATALOG_JOBS(CatalogJob.class),
    PRODUCTION_EVENT(ProductionEvent.class),
    PRIP_JOBS(PripPublishingJob.class),
    EVICTION_MANAGEMENT_JOBS(EvictionManagementJob.class),
    LTA_DOWNLOAD_EVENT(LtaDownloadEvent.class),    
    // S2 based categories
    LEVEL_INPUT(IngestionEvent.class), // represent level product that has been ingested
	// S3 based categories
	S3_AUX(ProductionEvent.class),
	S3_PRODUCTS(ProductionEvent.class),
	UNDEFINED(null);
	
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
	        case PLAN_AND_REPORT:
	        	return ProductCategory.PLANS_AND_REPORTS;
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
	        case L1C:
	        case L2A:
	        	return ProductCategory.LEVEL_INPUT;
	        case SPP_OBS:
	        	return ProductCategory.SPP_PRODUCTS;
	        case DEBUG:
	        	return ProductCategory.DEBUG;
			case AUXILIARY_FILE_ZIP:
			case L0_ACN_ZIP:
			case L0_BLANK_ZIP:
			case L0_SEGMENT_ZIP:
			case L0_SLICE_ZIP:
			case L1_ACN_ZIP:
			case L1_SLICE_ZIP:
			case L2_ACN_ZIP:
			case L2_SLICE_ZIP:
			case L2A_ZIP:
			// S3 zip families
			case S3_AUX_ZIP:
			case S3_L0_ZIP:
			case S3_L1_ZIP:
			case S3_L2_ZIP:
			case S3_CAL_ZIP:
			case S3_PUG_ZIP:
				return ProductCategory.COMPRESSION_JOBS;
        	case S3_AUX:
        		return ProductCategory.S3_AUX;
        	case S3_GRANULES:
        	case S3_L0:
        	case S3_L1:
        	case S3_L2:
        	case S3_CAL:
        	case S3_PUG:
        		return ProductCategory.S3_PRODUCTS;
	        default:
	        	throw new IllegalArgumentException(
	        			String.format("Cannot determine product category for family %s", family)
	        	);
        }
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
