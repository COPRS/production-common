package esa.s1pdgs.cpoc.common.errors.appcatalog;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class AppCatalogMqiNextApiError extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 8096417675612082535L;
    /**
     * Category
     */
    private final ProductCategory category;

    /**
     * @param category
     * @param message
     */
    public AppCatalogMqiNextApiError(final ProductCategory category,
            final String message) {
        super(ErrorCode.APPCATALOG_MQI_NEXT_API_ERROR, message);
        this.category = category;
    }

    /**
     * @param category
     * @param message
     * @param cause
     */
    public AppCatalogMqiNextApiError(final ProductCategory category, final String message,
            final Throwable cause) {
        super(ErrorCode.APPCATALOG_MQI_NEXT_API_ERROR, message, cause);
        this.category = category;
    }

    /**
     * @return the category
     */
    public ProductCategory getCategory() {
        return category;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[category %s] [msg %s]", category, getMessage());
    }

}
