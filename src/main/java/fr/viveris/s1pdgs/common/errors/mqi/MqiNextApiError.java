package fr.viveris.s1pdgs.common.errors.mqi;

import fr.viveris.s1pdgs.common.ProductCategory;
import fr.viveris.s1pdgs.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class MqiNextApiError extends AbstractCodedException {

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
    public MqiNextApiError(final ProductCategory category,
            final String message) {
        super(ErrorCode.MQI_NEXT_API_ERROR, message);
        this.category = category;
    }

    /**
     * @param category
     * @param message
     * @param cause
     */
    public MqiNextApiError(final ProductCategory category, final String message,
            final Throwable cause) {
        super(ErrorCode.MQI_NEXT_API_ERROR, message, cause);
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
