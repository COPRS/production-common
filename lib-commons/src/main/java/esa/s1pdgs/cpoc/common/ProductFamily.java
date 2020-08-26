package esa.s1pdgs.cpoc.common;

/**
 * Product family. Determinate the concerned topics, buckets in OBS, ...
 * 
 * @author Cyrielle Gailliard
 */
public enum ProductFamily {
    EDRS_SESSION, //
    AUXILIARY_FILE, //
    PLAN_AND_REPORT, //
    BLANK, //
    INVALID, //
    GHOST, //
    SESSION_RETRANSFER, //
    JOB_ORDER, //
    L0_ACN, //
    L0_BLANK, //
    L0_JOB, //
    L0_REPORT, //
    L0_SEGMENT, //
    L0_SEGMENT_JOB, //
    L0_SEGMENT_REPORT, //
    L0_SLICE, //
    L1_ACN, //
    L1_REPORT, //
    L1_SLICE, //
    L1_JOB, //
    L2_ACN, //
    L2_SLICE, //
    L2_JOB, //
    L2_REPORT, //
    // ZIP Product families
    AUXILIARY_FILE_ZIP, //
    L0_ACN_ZIP, //
    L0_BLANK_ZIP, //
    L0_SEGMENT_ZIP, //
    L0_SLICE_ZIP, //
    L1_ACN_ZIP, //
    L1_SLICE_ZIP, //
    L2_ACN_ZIP, //
    L2_SLICE_ZIP,
    PLAN_AND_REPORT_ZIP,
    
    // S2QT types
    L1C,
    L2A,
    L2A_ZIP,
    
    // S3 Types    
    S3_AUX,
    S3_GRANULES,
    S3_L0,
    S3_L1,
    S3_L2,
    S3_CAL,
    S3_PUG,
    // S3 Jobs
    S3_JOB,
    // S3 compressed types
    S3_AUX_ZIP,
    S3_L0_ZIP,
    S3_L1_ZIP,
    S3_L2_ZIP,
    S3_CAL_ZIP,
    S3_PUG_ZIP;

    /**
     * Get product family from value in string format
     * 
     * @param value
     * @return
     */
    public static ProductFamily fromValue(final String value) {
        ProductFamily ret;
        if (value == null || value.length() == 0) {
            ret = ProductFamily.BLANK;
        } else {
            try {
                ret = ProductFamily.valueOf(value);
            } catch (IllegalArgumentException ex) {
            	ex.printStackTrace();
                ret = ProductFamily.BLANK;
            }
        }
        return ret;
    }
}
