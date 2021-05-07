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
    L1_REPORT, 
    L1_SLICE, 
    L1_JOB, 
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
    
    // S2QT types
    L1C,
    L2A,
    L2A_ZIP,
    
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
    
}
