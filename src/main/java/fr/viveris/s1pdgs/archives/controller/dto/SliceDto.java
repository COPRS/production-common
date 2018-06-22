package fr.viveris.s1pdgs.archives.controller.dto;

import java.util.Objects;

import fr.viveris.s1pdgs.archives.model.ProductFamily;

/**
 * DTO class for L0 slices
 * 
 * @author Viveris technologies
 */
public class SliceDto {

    /**
     * Product name of the slice
     */
    private String productName;

    /**
     * ObjectkeyStorage of the slice
     */
    private String keyObjectStorage;

    /**
     * Family name of the slice (l0 or l1)
     */
    private ProductFamily family;

    /**
     * @param productName
     * @param keyObjectStorage
     */
    public SliceDto(final String productName, final String keyObjectStorage,
            final ProductFamily family) {
        this.productName = productName;
        this.keyObjectStorage = keyObjectStorage;
        this.family = family;
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
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(
                "{productName: %s, keyObjectStorage: %s, family: %s}",
                productName, keyObjectStorage, family);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(productName, keyObjectStorage, family);
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
            final SliceDto other = (SliceDto) obj;
            // field comparison
            ret = Objects.equals(productName, other.productName)
                    && Objects.equals(keyObjectStorage, other.keyObjectStorage)
                    && Objects.equals(family, other.family);
        }
        return ret;
    }

}
