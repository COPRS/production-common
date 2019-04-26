package esa.s1pdgs.cpoc.jobgenerator.model.joborder;

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

/**
 * Class listing the breakpoint configuration in a job order
 * 
 * @author Cyrielle Gailliard
 *
 */
@XmlRootElement(name = "Breakpoint")
@XmlAccessorType(XmlAccessType.NONE)
public class JobOrderBreakpoint {

	/**
	 * Breakpoint enable: OFF | ON
	 */
	@XmlElement(name = "Enable")
	private String enable;

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
	public JobOrderBreakpoint() {
		this.files = new ArrayList<>();
		this.nbFiles = 0;
		this.enable = "OFF";
	}

	/**
	 * Construction with all fields
	 * 
	 * @param enable
	 * @param files
	 */
	public JobOrderBreakpoint(final String enable, final List<String> files) {
		this();
		this.enable = enable;
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
	public JobOrderBreakpoint(final JobOrderBreakpoint obj) {
		this(obj.getEnable(), obj.getFiles());
	}

	/**
	 * @return the enable
	 */
	public String getEnable() {
		return enable;
	}

	/**
	 * @param enable
	 *            the enable to set
	 */
	public void setEnable(final String enable) {
		this.enable = enable;
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
		return String.format("{enable: %s, files: %s, nbFiles: %s}", enable, files, nbFiles);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(enable, files, nbFiles);
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
			JobOrderBreakpoint other = (JobOrderBreakpoint) obj;
			ret = Objects.equals(enable, other.enable) && Objects.equals(files, other.files)
					&& nbFiles == other.nbFiles;
		}
		return ret;
	}

}
