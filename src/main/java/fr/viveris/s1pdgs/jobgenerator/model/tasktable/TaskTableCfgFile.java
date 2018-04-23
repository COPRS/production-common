package fr.viveris.s1pdgs.jobgenerator.model.tasktable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Object for Cfg_Files in task table
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
	public TaskTableCfgFile(String fileName, String version) {
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
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TaskTableCfgFile [fileName=" + fileName + ", version=" + version + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	/* (non-Javadoc)
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
		TaskTableCfgFile other = (TaskTableCfgFile) obj;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
}
