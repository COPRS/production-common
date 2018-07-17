package fr.viveris.s1pdgs.common.errors.mqi;

import fr.viveris.s1pdgs.common.ProductCategory;
import fr.viveris.s1pdgs.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class MqiPublishApiError extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 126226117695671374L;

    /**
     * Category
     */
    private final ProductCategory category;

    /**
     * Output
     */
    private final Object output;

    /**
     * @param category
     * @param message
     */
    public MqiPublishApiError(final ProductCategory category,
            final Object output, final String message) {
        super(ErrorCode.MQI_PUBLISH_API_ERROR, message);
        this.category = category;
        this.output = output;
    }

    /**
     * @param category
     * @param message
     * @param cause
     */
    public MqiPublishApiError(final ProductCategory category,
            final Object output, final String message, final Throwable cause) {
        super(ErrorCode.MQI_PUBLISH_API_ERROR, message, cause);
        this.category = category;
        this.output = output;
    }

    /**
     * @return the category
     */
    public ProductCategory getCategory() {
        return category;
    }

    /**
     * @return the output
     */
    public Object getOutput() {
        return output;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[category %s] [output %s] [msg %s]", category,
                output, getMessage());
    }

}
