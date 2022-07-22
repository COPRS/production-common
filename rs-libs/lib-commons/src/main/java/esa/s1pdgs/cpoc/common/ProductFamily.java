package esa.s1pdgs.cpoc.common;

import java.util.Objects;

/**
 * Product family. Determinate the concerned topics, buckets in OBS, ...
 * 
 * @author Cyrielle Gailliard
 */
public enum ProductFamily {
    EDRS_SESSION, 
    AUXILIARY_FILE, 
    PLAN_AND_REPORT, 
    BLANK, 
    INVALID, 
    GHOST, 
    SESSION_RETRANSFER, 
    JOB_ORDER, 
    L0_ACN, 
    L0_BLANK, 
    L0_JOB, 
    L0_REPORT, 
    L0_SEGMENT, 
    L0_SEGMENT_JOB, 
    L0_SEGMENT_REPORT, 
    L0_SLICE, 
    L1_ACN, 
    L1_ETAD, 
    L1_REPORT, 
    L1_SLICE, 
    L1_JOB, 
    L1_ETAD_JOB,
    L2_ACN, 
    L2_SLICE, 
    L2_JOB, 
    L2_REPORT, 
    DEBUG, // for debug bucket in obs
    FAILED_WORKDIR, // for failed working directories copied to obs
    // ZIP Product families
    AUXILIARY_FILE_ZIP, 
    L0_ACN_ZIP, 
    L0_BLANK_ZIP, 
    L0_SEGMENT_ZIP, 
    L0_SLICE_ZIP, 
    L1_ACN_ZIP, 
    L1_ETAD_ZIP, 
    L1_SLICE_ZIP, 
    L2_ACN_ZIP, 
    L2_SLICE_ZIP,
    PLAN_AND_REPORT_ZIP,

    //SPP types
    SPP_MBU_JOB,
    SPP_MBU,
    SPP_OBS_JOB,
    SPP_OBS,
    // SPP compressed types
    SPP_OBS_ZIP,
    
    // S2 types
    S2_AUX,
    S2_L0_GR,
    S2_L0_DS,
    S2_L1A_GR,
    S2_L1A_DS,
    S2_L1B_GR,
    S2_L1B_DS,
    S2_L1C_TL,
    S2_L1C_DS,
    S2_L1C_TC,
    S2_L2A_TL,
    S2_L2A_DS,
    S2_SAD,
    S2_HKTM,
    // S2 compressed types
    S2_AUX_ZIP,
    S2_L0_GR_ZIP,
    S2_L0_DS_ZIP,
    S2_L1A_GR_ZIP,
    S2_L1A_DS_ZIP,
    S2_L1B_GR_ZIP,
    S2_L1B_DS_ZIP,
    S2_L1C_TL_ZIP,
    S2_L1C_DS_ZIP,
    S2_L1C_TC_ZIP,
    S2_L2A_TL_ZIP,
    S2_L2A_DS_ZIP,
    S2_SAD_ZIP,
    S2_HKTM_ZIP,
    
    // S3 Types    
    S3_AUX,
    S3_GRANULES,
    S3_L0,
    S3_L1_NRT,
    S3_L1_STC,
    S3_L1_NTC,
    S3_L2_NRT,
    S3_L2_STC,
    S3_L2_NTC,
    S3_CAL,
    S3_PUG,
    // S3 Jobs
    S3_JOB,
    // S3 compressed types
    S3_AUX_ZIP,
    S3_GRANULES_ZIP,
    S3_L0_ZIP,
    S3_L1_NRT_ZIP,
    S3_L1_STC_ZIP,
    S3_L1_NTC_ZIP,
    S3_L2_NRT_ZIP,
    S3_L2_STC_ZIP,
    S3_L2_NTC_ZIP,
    S3_CAL_ZIP,
    S3_PUG_ZIP;
	
	// --------------------------------------------------------------------------
	
	public boolean isCompressed() {
		return ProductFamily.isCompressed(this);
	}
	
	// --------------------------------------------------------------------------

    /**
     * Get product family from value in string format
     * 
     */
    public static ProductFamily fromValue(final String value) {
        ProductFamily ret;
        if (value == null || value.length() == 0) {
            ret = ProductFamily.BLANK;
        } else {
            try {
                ret = ProductFamily.valueOf(value);
            } catch (final IllegalArgumentException ex) {
            	ex.printStackTrace();
                ret = ProductFamily.BLANK;
            }
        }
        return ret;
    }
    
	public static boolean isCompressed(final ProductFamily productFamily) {
		return Objects.requireNonNull(productFamily, "productFamily must not be null!").name().endsWith("_ZIP");
	}
	
	public boolean isSessionFamily() {
		return (this == EDRS_SESSION || this == SESSION_RETRANSFER);
	}
	
	public boolean isEndToEndFamily() {
		return (this != AUXILIARY_FILE
				&& this != AUXILIARY_FILE_ZIP
				&& this != EDRS_SESSION 
				&& this != SESSION_RETRANSFER 
				&& this != L0_SEGMENT
				&& this != L0_SEGMENT_ZIP
				&& this != PLAN_AND_REPORT
				&& this != PLAN_AND_REPORT_ZIP
				&& this != SPP_MBU
				&& this != BLANK 
				&& this != INVALID 
				&& this != GHOST 
				&& this != JOB_ORDER 
				&& this != DEBUG
				&& this != FAILED_WORKDIR
				&& this != L0_BLANK
				&& this != L0_BLANK_ZIP
				&& this != L0_JOB
				&& this != L0_SEGMENT_JOB
				&& this != L1_JOB
				&& this != L2_JOB
				&& this != L1_ETAD_JOB
				&& this != SPP_MBU_JOB
				&& this != SPP_OBS_JOB
				&& this != L0_REPORT
				&& this != L0_SEGMENT_REPORT
				&& this != L1_REPORT
				&& this != L2_REPORT
				&& this != S2_AUX
				&& this != S2_AUX_ZIP
				&& this != S3_AUX
				&& this != S3_AUX_ZIP);
		
	}
    
	
}