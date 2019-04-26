package esa.s1pdgs.cpoc.common.errors.mqi;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class MqiRouteNotAvailable extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 8248616873024871315L;

    /**
     * Category
     */
    private final ProductCategory category;

    /**
     * Category
     */
    private final ProductFamily family;

    /**
     * @param topic
     * @param productName
     * @param message
     * @param e
     */
    public MqiRouteNotAvailable(final ProductCategory category,
            final ProductFamily family) {
        super(ErrorCode.MQI_ROUTE_NOT_AVAILABLE,
                String.format("No route available"));
        this.category = category;
        this.family = family;
    }

    /**
     * @return the category
     */
    public ProductCategory getCategory() {
        return category;
    }

    /**
     * @return the family
     */
    public ProductFamily getFamily() {
        return family;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[category %s] [family %s] [msg %s]", category,
                family, getMessage());
    }

}
