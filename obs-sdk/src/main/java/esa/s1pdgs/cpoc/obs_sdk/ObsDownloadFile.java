package esa.s1pdgs.cpoc.obs_sdk;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * @author Viveris Technologies
 */
public class ObsDownloadFile extends ObsCustomObject {

    /**
     * Target directory
     */
    private final String targetDir;

    /**
     * @param family
     * @param key
     * @param targetDir
     */
    public ObsDownloadFile(final ProductFamily family, final String key,
            final String targetDir) {
        super(family, key);
        this.targetDir = targetDir;
    }

    /**
     * @return the targetDir
     */
    public String getTargetDir() {
        return targetDir;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String superStr = super.toStringForExtendedClasses();
        return String.format("{%s, targetDir: %s}", superStr, targetDir);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int superHash = super.hashCode();
        return Objects.hash(superHash, targetDir);
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
            ObsDownloadFile other = (ObsDownloadFile) obj;
            // field comparison
            ret = super.equals(other)
                    && Objects.equals(targetDir, other.targetDir);
        }
        return ret;
    }

}
