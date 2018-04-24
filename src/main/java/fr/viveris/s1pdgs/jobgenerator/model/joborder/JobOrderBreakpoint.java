package fr.viveris.s1pdgs.jobgenerator.model.joborder;

import java.util.ArrayList;
import java.util.List;

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
	public JobOrderBreakpoint(String enable, List<String> files) {
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
	public JobOrderBreakpoint(JobOrderBreakpoint obj) {
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
	public void setEnable(String enable) {
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
	public void addFiles(List<String> files) {
		this.files.addAll(files);
		this.nbFiles = files.size();
	}

	/**
	 * @return the nbFiles
	 */
	public int getNbFiles() {
		return nbFiles;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JobOrderBreakpoint [enable=" + enable + ", files=" + files + ", nbFiles=" + nbFiles + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + nbFiles;
		result = prime * result + ((enable == null) ? 0 : enable.hashCode());
		result = prime * result + ((files == null) ? 0 : files.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JobOrderBreakpoint other = (JobOrderBreakpoint) obj;
		if (nbFiles != other.nbFiles)
			return false;
		if (enable == null) {
			if (other.enable != null)
				return false;
		} else if (!enable.equals(other.enable))
			return false;
		if (files == null) {
			if (other.files != null)
				return false;
		} else if (!files.equals(other.files))
			return false;
		return true;
	}

}
