package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Locale;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Exchanged object for the product category LevelProducts.
 * 
 * @author Viveris Technologies
 */
public class LevelProductDto {

    /**
     * Product name of the metadata to index
     */
    private String productName;

    /**
     * ObjectkeyStorage of the metatdata to index
     */
    private String keyObjectStorage;

    /**
     * Family name for L0 Slices
     */
    private ProductFamily family;

    /**
     * Mode
     */
    private String mode;

    /**
     * Default constructor
     */
    public LevelProductDto() {
        super();
        this.family = ProductFamily.BLANK;
    }

    /**
     * @param productName
     * @param keyObjectStorage
     */
    public LevelProductDto(final String productName,
            final String keyObjectStorage, final ProductFamily family, final String mode) {
        this();
        this.productName = productName;
        this.keyObjectStorage = keyObjectStorage;
        this.family = family;
        if (mode != null) {
            this.mode = mode.toUpperCase(Locale.FRANCE);
        }
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
     * @return the family
     */
    public ProductFamily getFamily() {
        return family;
    }

    /**
     * @param familyName
     *            the familyName to set
     */
    public void setFamily(final ProductFamily family) {
        this.family = family;
    }

    /**
     * @return
     */
    public String getMode() {
        return mode;
    }

    /**
     * @param mode
     */
    public void setMode(final String mode) {
        if (mode == null) {
            this.mode = mode;
        } else {
            this.mode = mode.toUpperCase(Locale.FRANCE);
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(
                "{productName: %s, keyObjectStorage: %s, family: %s, mode: %s}",
                productName, keyObjectStorage, family, mode);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(productName, keyObjectStorage, family, mode);
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
            LevelProductDto other = (LevelProductDto) obj;
            // field comparison
            ret = Objects.equals(productName, other.productName)
                    && Objects.equals(keyObjectStorage, other.keyObjectStorage)
                    && Objects.equals(family, other.family)
                    && Objects.equals(mode, other.mode);
        }
        return ret;
    }
}
