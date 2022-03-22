package esa.s1pdgs.cpoc.ipf.execution.worker.job.model.mqi;

import java.io.File;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * @author Viveris Technologies
 */
public class FileQueueMessage extends QueueMessage {

    /**
     * File to upload
     */
    private final File file;

    /**
     * 
     * @param family
     * @param productName
     * @param file
     */
    public FileQueueMessage(final ProductFamily family,
            final String productName, final File file) {
        super(family, productName);
        this.file = file;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String superStr = super.toStringForExtendedClasses();
        return String.format("{%s, file: %s}", superStr,
                file);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int superHash = super.hashCode();
        return Objects.hash(superHash, file);
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
            FileQueueMessage other = (FileQueueMessage) obj;
            // field comparison
            ret = super.equals(other)
                    && Objects.equals(file, other.file);
        }
        return ret;
    }

}
