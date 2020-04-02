package esa.s1pdgs.cpoc.obs_sdk;

import java.io.File;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Representes an object whose function is to be uploaded on a local directory
 * 
 * @author Viveris Technologies
 */
public class FileObsUploadObject extends ObsUploadObject {

    /**
     * File to upload in the OBS
     */
    private File file;

    /**
     * Constructor
     * 
     * @param key
     * @param family
     * @param file
     */
    public FileObsUploadObject(final ProductFamily family, final String key, final File file) {
        super(family, key);
        this.file = file;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file
     *            the file to set
     */
    public void setFile(final File file) {
        this.file = file;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final String superToStr = super.toStringForExtend();
        return String.format("{%s, file: %s}", superToStr, file);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int superHash = super.hashCode();
        return Objects.hash(superHash, file);
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
            final FileObsUploadObject other = (FileObsUploadObject) obj;
            ret = super.equals(obj) && Objects.equals(file, other.file);
        }
        return ret;
    }

}
