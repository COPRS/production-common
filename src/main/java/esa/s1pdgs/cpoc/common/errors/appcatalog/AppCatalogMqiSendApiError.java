package esa.s1pdgs.cpoc.common.errors.appcatalog;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class AppCatalogMqiSendApiError extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 8096417675612082535L;

    /**
     * Category
     */
    private final ProductCategory category;

    /**
     * Force
     */
    private final String uri;

    /**
     * object
     */
    private final Object dto;

    /**
     * @param category
     * @param message
     */
    public AppCatalogMqiSendApiError(final ProductCategory category,
            final String uri, final Object dto, final String message) {
        super(ErrorCode.APPCATALOG_MQI_SEND_API_ERROR, message);
        this.category = category;
        this.uri = uri;
        this.dto = dto;
    }

    /**
     * @param category
     * @param message
     * @param cause
     */
    public AppCatalogMqiSendApiError(final ProductCategory category,
            final String uri, final Object dto, final String message,
            final Throwable cause) {
        super(ErrorCode.APPCATALOG_MQI_SEND_API_ERROR, message, cause);
        this.category = category;
        this.uri = uri;
        this.dto = dto;
    }

    /**
     * @return the category
     */
    public ProductCategory getCategory() {
        return category;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @return the dto
     */
    public Object getDto() {
        return dto;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[category %s] [uri %s] [dto %s] [msg %s]",
                category, uri, dto, getMessage());
    }

}
