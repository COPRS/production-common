package esa.s1pdgs.cpoc.xml.model.tasktable;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Object for Cfg_Files in task table
 * 
 * @author Cyrielle
 *
 */
@XmlRootElement(name = "Cfg_Files")
@XmlAccessorType(XmlAccessType.NONE)
public class TaskTableCfgFile {

	/**
	 * File name
	 */
	@XmlElement(name = "File_Name")
	private String fileName;

	/**
	 * Version
	 */
	@XmlElement(name = "Version")
	private String version;

	/**
	 * 
	 */
	public TaskTableCfgFile() {
		super();
	}

	/**
	 * @param fileName
	 * @param version
	 */
	public TaskTableCfgFile(final String fileName, final String version) {
		this();
		this.fileName = fileName;
		this.version = version;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(final String version) {
		this.version = version;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(fileName, version);
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
			TaskTableCfgFile other = (TaskTableCfgFile) obj;
			ret = Objects.equals(fileName, other.fileName) && Objects.equals(version, other.version);
		}
		return ret;
	}
}
