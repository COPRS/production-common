package esa.s1pdgs.cpoc.execution.worker.job.model.mqi;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Object to publish in a topic
 * 
 * @author Viveris Technologies
 */
public class QueueMessage {

    /**
     * Family
     */
    protected final ProductFamily family;

    /**
     * Product name
     */
    protected final String productName;

    /**
     * @param family
     * @param productName
     */
    public QueueMessage(final ProductFamily family,
            final String productName) {
        super();
        this.family = family;
        this.productName = productName;
    }

    /**
     * @return the family
     */
    public ProductFamily getFamily() {
        return family;
    }

    /**
     * @return the productName
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{family: %s, productName: %s}", family,
                productName);
    }

    /**
     * to string to include in extended classes
     */
    public String toStringForExtendedClasses() {
        return String.format("family: %s, productName: %s", family,
                productName);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(family, productName);
    }

    /**
     * @see java.lang.Object#equals()
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            QueueMessage other = (QueueMessage) obj;
            // field comparison
            ret = Objects.equals(productName, other.productName)
                    && Objects.equals(family, other.family);
        }
        return ret;
    }

}
