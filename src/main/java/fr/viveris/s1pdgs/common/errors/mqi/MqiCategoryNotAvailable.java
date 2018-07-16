package fr.viveris.s1pdgs.common.errors.mqi;

import fr.viveris.s1pdgs.common.ProductCategory;
import fr.viveris.s1pdgs.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class MqiCategoryNotAvailable extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 8248616873024871315L;

    /**
     * Category
     */
    private final ProductCategory category;
    
    /**
     * Type
     */
    private final String type;

    /**
     * @param topic
     * @param productName
     * @param message
     * @param e
     */
    public MqiCategoryNotAvailable(final ProductCategory category, final String type) {
        super(ErrorCode.MQI_CATEGORY_NOT_AVAILABLE, String
                .format("No %s available for category %s", type, category));
        this.category = category;
        this.type = type;
    }

    /**
     * @return the category
     */
    public ProductCategory getCategory() {
        return category;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[category %s] [msg %s]", category, getMessage());
    }

}
