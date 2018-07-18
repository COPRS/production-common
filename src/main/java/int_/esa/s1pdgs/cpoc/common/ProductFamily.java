package int_.esa.s1pdgs.cpoc.common;

/**
 * Product family. Determinate the concerned topics, buckets in OBS, ...
 * 
 * @author Cyrielle Gailliard
 */
public enum ProductFamily {
    EDRS_SESSION, JOB_ORDER, AUXILIARY_FILE, L0_ACN, L0_PRODUCT, L0_REPORT,
    L0_JOB, L1_ACN, L1_PRODUCT, L1_REPORT, L1_JOB, BLANK;

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
