package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * @author Viveris Technologies
 * @see IpfExecutionJob
 */
public class LevelJobInputDto {

    /**
     * Input family
     * 
     * @see ProductFamily
     */
    private String family;

    /**
     * Local path on the target host
     */
    private String localPath;

    /**
     * The reference to the content. Can be the object storage or directly in
     * the string according the family
     */
    private String contentRef;

    /**
     * Constructor
     */
    public LevelJobInputDto() {
        super();
    }

    /**
     * @param family
     * @param localPath
     * @param contentRef
     */
    public LevelJobInputDto(final String family, final String localPath,
            final String contentRef) {
        this();
        this.family = family;
        this.localPath = localPath;
        this.contentRef = contentRef;
    }

    /**
     * @return the family
     */
    public String getFamily() {
        return family;
    }

    /**
     * @param family
     *            the family to set
     */
    public void setFamily(final String family) {
        this.family = family;
    }

    /**
     * @return the localPath
     */
    public String getLocalPath() {
        return localPath;
    }

    /**
     * @param localPath
     *            the localPath to set
     */
    public void setLocalPath(final String localPath) {
        this.localPath = localPath;
    }

    /**
     * @return the contentRef
     */
    public String getContentRef() {
        return contentRef;
    }

    /**
     * @param contentRef
     *            the contentRef to set
     */
    public void setContentRef(final String contentRef) {
        this.contentRef = contentRef;
    }

    /**
     * to string
     */
    @Override
    public String toString() {
        return String.format("{family: %s, localPath: %s, contentRef: %s}",
                family, localPath, contentRef);
    }

    /**
     * Hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(contentRef, family, localPath);
    }

    /**
     * Equals
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            LevelJobInputDto other = (LevelJobInputDto) obj;
            ret = Objects.equals(contentRef, other.contentRef)
                    && Objects.equals(family, other.family)
                    && Objects.equals(localPath, other.localPath);
        }
        return ret;
    }

}
