package esa.s1pdgs.cpoc.common;

import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.DownloadJob;
import esa.s1pdgs.cpoc.mqi.model.queue.EvictionManagementJob;
import esa.s1pdgs.cpoc.mqi.model.queue.EvictionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.DataRequestJob;
import esa.s1pdgs.cpoc.mqi.model.queue.DataRequestEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LtaDownloadEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.OnDemandEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.PripPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.DisseminationJob;

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
    ETAD_PRODUCTS(ProductionEvent.class),
    SPP_PRODUCTS(ProductionEvent.class),
    SPP_MBU_PRODUCTS(ProductionEvent.class),
    DEBUG(ProductionEvent.class),
    FAILED_WORKDIRS(ProductionEvent.class),
    COMPRESSION_JOBS(CompressionJob.class),
    COMPRESSED_PRODUCTS(CompressionEvent.class),
    INGESTION(IngestionJob.class),
    INGESTION_EVENT(IngestionEvent.class),
    PREPARATION_JOBS(IpfPreparationJob.class),    
    CATALOG_EVENT(CatalogEvent.class),
    CATALOG_JOBS(CatalogJob.class),
    PRODUCTION_EVENT(ProductionEvent.class),
    PRIP_JOBS(PripPublishingJob.class),
    DISSEMINATION_JOBS(DisseminationJob.class),
    EVICTION_MANAGEMENT_JOBS(EvictionManagementJob.class),
    EVICTION_EVENT(EvictionEvent.class),
    DATA_REQUEST_JOBS(DataRequestJob.class),
    DATA_REQUEST_EVENT(DataRequestEvent.class),
    LTA_DOWNLOAD_EVENT(LtaDownloadEvent.class), 
    ON_DEMAND_EVENT(OnDemandEvent.class),
    DOWNLOAD_JOB(DownloadJob.class),
    // S2 based categories
    S2_AUX(ProductionEvent.class),
    S2_PRODUCTS(ProductionEvent.class),
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
	        case L1_ETAD_JOB:
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
	        case L1_ETAD:
	        	return ProductCategory.ETAD_PRODUCTS;
	        case SPP_MBU:
	        	return ProductCategory.SPP_MBU_PRODUCTS;
	        case SPP_OBS:
	        	return ProductCategory.SPP_PRODUCTS;
	        case DEBUG:
	        	return ProductCategory.DEBUG;
	        case FAILED_WORKDIR:
	        	return ProductCategory.FAILED_WORKDIRS;
			case AUXILIARY_FILE_ZIP:
			case PLAN_AND_REPORT_ZIP:
			case SPP_OBS_ZIP:
			case L0_ACN_ZIP:
			case L0_BLANK_ZIP:
			case L0_SEGMENT_ZIP:
			case L0_SLICE_ZIP:
			case L1_ACN_ZIP:
			case L1_ETAD_ZIP:
			case L1_SLICE_ZIP:
			case L2_ACN_ZIP:
			case L2_SLICE_ZIP:
		    // S2 zip families
			case S2_AUX_ZIP:
			case S2_L0_GR_ZIP:
			case S2_L0_DS_ZIP:
			case S2_L1A_GR_ZIP:
			case S2_L1A_DS_ZIP:
			case S2_L1B_GR_ZIP:
			case S2_L1B_DS_ZIP:
			case S2_L1C_TL_ZIP:
			case S2_L1C_DS_ZIP:
			case S2_L1C_TC_ZIP:
			case S2_L2A_TL_ZIP:
			case S2_L2A_DS_ZIP:
			case S2_SAD_ZIP:
			case S2_HKTM_ZIP:
			// S3 zip families
			case S3_AUX_ZIP:
			case S3_GRANULES_ZIP:
			case S3_L0_ZIP:
			case S3_L1_NRT_ZIP:
			case S3_L1_STC_ZIP:
			case S3_L1_NTC_ZIP:
			case S3_L2_NRT_ZIP:
			case S3_L2_STC_ZIP:
			case S3_L2_NTC_ZIP:
			case S3_CAL_ZIP:
			case S3_PUG_ZIP:
				return ProductCategory.COMPRESSION_JOBS;
			// S2 families
			case S2_AUX:
			case S2_SAD:
				return ProductCategory.S2_AUX;
			case S2_L0_GR:
			case S2_L0_DS:
			case S2_L1A_GR:
			case S2_L1A_DS:
			case S2_L1B_GR:
			case S2_L1B_DS:
			case S2_L1C_TL:
			case S2_L1C_DS:
			case S2_L1C_TC:
			case S2_L2A_TL:
			case S2_L2A_DS:
			case S2_HKTM:
				return ProductCategory.S2_PRODUCTS;
			// S3 families
        	case S3_AUX:
        		return ProductCategory.S3_AUX;
        	case S3_GRANULES:
        	case S3_L0:
        	case S3_L1_NRT:
        	case S3_L1_STC:
        	case S3_L1_NTC:
        	case S3_L2_NRT:
        	case S3_L2_STC:
        	case S3_L2_NTC:
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
