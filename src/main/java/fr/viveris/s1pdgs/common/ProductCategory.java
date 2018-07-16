package fr.viveris.s1pdgs.common;

import fr.viveris.s1pdgs.common.errors.InternalErrorException;

/**
 * Group products per category
 * 
 * @author Viveris Technologie
 */
public enum ProductCategory {
    AUXILIARY_FILES, EDRS_SESSIONS, LEVEL_JOBS, LEVEL_PRODUCTS, LEVEL_REPORTS;

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
            case L0_JOB:
            case L1_JOB:
                ret = ProductCategory.LEVEL_JOBS;
                break;
            case L0_REPORT:
            case L1_REPORT:
                ret = ProductCategory.LEVEL_REPORTS;
                break;
            case L0_ACN:
            case L0_PRODUCT:
            case L1_ACN:
            case L1_PRODUCT:
                ret = ProductCategory.LEVEL_PRODUCTS;
                break;
            default:
                throw new InternalErrorException(
                        "Cannot determinate product category for family "
                                + family);
        }
        return ret;
    }
}
