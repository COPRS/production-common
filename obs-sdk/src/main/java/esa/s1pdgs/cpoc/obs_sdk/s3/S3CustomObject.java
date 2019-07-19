package esa.s1pdgs.cpoc.obs_sdk.s3;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * @author Viveris technlogies
 */
public class S3CustomObject {

    /**
     * Product family
     */
    private final ProductFamily family;

    /**
     * Key object storage
     */
    private final String key;

    /**
     * @param family
     * @param key
     */
    public S3CustomObject(final ProductFamily family, final String key) {
        this.family = family;
        this.key = key;
    }

    /**
     * @return the family
     */
    public ProductFamily getFamily() {
        return family;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{family: %s, key: %s}", family, key);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toStringForExtendedClasses() {
        return String.format("family: %s, key: %s", family, key);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(family, key);
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
            S3CustomObject other = (S3CustomObject) obj;
            // field comparison
            ret = Objects.equals(key, other.key)
                    && Objects.equals(family, other.family);
        }
        return ret;
    }

}
