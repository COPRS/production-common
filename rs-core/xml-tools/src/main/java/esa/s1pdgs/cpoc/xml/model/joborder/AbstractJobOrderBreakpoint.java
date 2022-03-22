package esa.s1pdgs.cpoc.xml.model.joborder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.springframework.util.CollectionUtils;

@XmlRootElement(name = "Breakpoint")
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractJobOrderBreakpoint {

    /**
     * List of breakpoints
     */
    @XmlElementWrapper(name = "List_of_Brk_Files")
    @XmlElement(name = "Brk_Files")
    private List<String> files;

    /**
     * Number of breakpoints. Automatically filled with files.
     */
    @XmlPath("List_of_Brk_Files/@count")
    private int nbFiles;

    /**
     * Default constructor
     */
    public AbstractJobOrderBreakpoint() {
        this.files = new ArrayList<>();
        this.nbFiles = 0;
    }

    /**
     * Construction with all fields
     *
     * @param files
     */
    public AbstractJobOrderBreakpoint(final List<String> files) {
        this();
        if (!CollectionUtils.isEmpty(files)) {
            this.files.addAll(files);
            this.nbFiles = this.files.size();
        }
    }

    /**
     * Clone
     *
     * @param obj
     */
    public AbstractJobOrderBreakpoint(final AbstractJobOrderBreakpoint obj) {
        this(obj.getFiles());
    }

    /**
     * @return the files
     */
    public List<String> getFiles() {
        return files;
    }

    /**
     * @param files
     *            the files to set
     */
    public void addFiles(final List<String> files) {
        this.files.addAll(files);
        this.nbFiles = files.size();
    }

    /**
     * @return the nbFiles
     */
    public int getNbFiles() {
        return nbFiles;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{files: %s, nbFiles: %s}", files, nbFiles);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(files, nbFiles);
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
            AbstractJobOrderBreakpoint other = (AbstractJobOrderBreakpoint) obj;
            ret = Objects.equals(files, other.files)
                    && nbFiles == other.nbFiles;
        }
        return ret;
    }

}
