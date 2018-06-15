package fr.viveris.s1pdgs.libs.obs_sdk;

import java.util.Objects;

/**
 * Describe an object of the S1-PDGS object storage
 * 
 * @author Viveris Technologies
 */
public class ObsObject {

    /**
     * Object prefix or key
     */
    protected String key;

    /**
     * Object family
     */
    protected ObsFamily family;

    /**
     * Constructor
     * 
     * @param key
     * @param family
     */
    public ObsObject(final String key, final ObsFamily family) {
        this.key = key;
        this.family = family;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(final String key) {
        this.key = key;
    }

    /**
     * @return the family
     */
    public ObsFamily getFamily() {
        return family;
    }

    /**
     * @param family
     *            the family to set
     */
    public void setFamily(final ObsFamily family) {
        this.family = family;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{key: %s, family: %s}", key, family);
    }

    /**
     * toString function used by the classes extending this one
     * 
     * @return
     */
    public String toStringForExtend() {
        return String.format("key: %s, family: %s", key, family);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(key, family);
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
            ObsObject other = (ObsObject) obj;
            ret = Objects.equals(key, other.key)
                    && Objects.equals(family, other.family);
        }
        return ret;
    }

}
