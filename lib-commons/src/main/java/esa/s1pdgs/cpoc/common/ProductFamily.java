package esa.s1pdgs.cpoc.common;

/**
 * Product family. Determinate the concerned topics, buckets in OBS, ...
 * 
 * @author Cyrielle Gailliard
 */
public enum ProductFamily {
    EDRS_SESSION, AUXILIARY_FILE, 
    L0_ACN, L0_SLICE, L1_ACN, L1_SLICE, L0_BLANK,
    L0_REPORT, L1_REPORT, L0_SEGMENT_REPORT,
    L0_SEGMENT,
    L0_JOB, L1_JOB, L0_SEGMENT_JOB,
    JOB_ORDER, BLANK;

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
                ret = ProductFamily.BLANK;
            }
        }
        return ret;
    }
}
