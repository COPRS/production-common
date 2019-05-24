package esa.s1pdgs.cpoc.jobgenerator.model.metadata;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Class describing the metdata of a file
 * 
 * @author Cyrielle Gailliard
 */
public abstract class AbstractMetadata {

    /**
     * 
     */
    public final static DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");

    /**
     * 
     */
    public final static DateTimeFormatter DATE_FORMATTER_26 =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

    /**
     * 
     */
    public final static DateTimeFormatter DATE_FORMATTER_LIGTH =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Product name
     */
    protected String productName;

    /**
     * Product type
     */
    protected String productType;

    /**
     * Key in object storage
     */
    protected String keyObjectStorage;

    /**
     * Validity start time
     */
    protected String validityStart;

    /**
     * Validity stop time
     */
    protected String validityStop;

    /**
     * @param productName
     * @param productType
     * @param keyObjectStorage
     * @param validityStart
     * @param validityStop
     */
    public AbstractMetadata(final String productName, final String productType,
            final String keyObjectStorage, final String validityStart,
            final String validityStop) {
        super();
        this.productName = productName;
        this.productType = productType;
        this.keyObjectStorage = keyObjectStorage;
        this.validityStart = validityStart;
        this.validityStop = validityStop;
    }
    
    public AbstractMetadata() {
    	
    }

    /**
     * @return the productName
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @param productName
     *            the productName to set
     */
    public void setProductName(final String productName) {
        this.productName = productName;
    }

    /**
     * @return the keyObjectStorage
     */
    public String getKeyObjectStorage() {
        return keyObjectStorage;
    }

    /**
     * @param keyObjectStorage
     *            the keyObjectStorage to set
     */
    public void setKeyObjectStorage(final String keyObjectStorage) {
        this.keyObjectStorage = keyObjectStorage;
    }

    /**
     * @return the validityStart
     */
    public String getValidityStart() {
        return validityStart;
    }

    /**
     * @param validityStart
     *            the validityStart to set
     */
    public void setValidityStart(final String validityStart) {
        this.validityStart = validityStart;
    }

    /**
     * @return the validityStop
     */
    public String getValidityStop() {
        return validityStop;
    }

    /**
     * @param validityStop
     *            the validityStop to set
     */
    public void setValidityStop(final String validityStop) {
        this.validityStop = validityStop;
    }

    /**
     * @return the productType
     */
    public String getProductType() {
        return productType;
    }

    /**
     * @param productType
     *            the productType to set
     */
    public void setProductType(final String productType) {
        this.productType = productType;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(
                "{productName: %s, productType: %s, keyObjectStorage: %s, validityStart: %s, validityStop: %s}",
                productName, productType, keyObjectStorage, validityStart,
                validityStop);
    }

    /**
     *
     */
    public String toAbstractString() {
        return String.format(
                "productName: %s, productType: %s, keyObjectStorage: %s, validityStart: %s, validityStop: %s",
                productName, productType, keyObjectStorage, validityStart,
                validityStop);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(productName, productType, keyObjectStorage,
                validityStart, validityStop);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            AbstractMetadata other = (AbstractMetadata) obj;
            ret = Objects.equals(productName, other.productName)
                    && Objects.equals(productType, other.productType)
                    && Objects.equals(keyObjectStorage, other.keyObjectStorage)
                    && Objects.equals(validityStart, other.validityStart)
                    && Objects.equals(validityStop, other.validityStop);
        }
        return ret;
    }
    
    public DateTimeFormatter getStartTimeFormatter() {
        if (validityStart.length() > 26) {
            return DATE_FORMATTER;
        } else if (validityStart.length() == 26) {
            return DATE_FORMATTER_26;
        } else {
            return DATE_FORMATTER_LIGTH;
        }
    }
    
    public DateTimeFormatter getStopTimeFormatter() {
        if (validityStop.length() > 26) {
            return DATE_FORMATTER;
        } else if (validityStop.length() == 26) {
            return DATE_FORMATTER_26;
        } else {
            return DATE_FORMATTER_LIGTH;
        }
    }
}
