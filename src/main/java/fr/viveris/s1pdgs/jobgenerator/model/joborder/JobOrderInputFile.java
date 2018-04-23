package fr.viveris.s1pdgs.jobgenerator.model.joborder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "File_Name")
@XmlAccessorType(XmlAccessType.NONE)
public class JobOrderInputFile {
	
	@XmlValue
	private String filename;
	
	private String keyObjectStorage;

	public JobOrderInputFile() {
	}

	/**
	 * @param filename
	 * @param keyObjectStorage
	 */
	public JobOrderInputFile(String filename, String keyObjectStorage) {
		this();
		this.filename = filename;
		this.keyObjectStorage = keyObjectStorage;
	}

	/**
	 * @param filename
	 * @param keyObjectStorage
	 */
	public JobOrderInputFile(JobOrderInputFile obj) {
		this(obj.getFilename(), obj.getKeyObjectStorage());
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * @return the keyObjectStorage
	 */
	public String getKeyObjectStorage() {
		return keyObjectStorage;
	}

	/**
	 * @param keyObjectStorage the keyObjectStorage to set
	 */
	public void setKeyObjectStorage(String keyObjectStorage) {
		this.keyObjectStorage = keyObjectStorage;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JobOrderInputFile [filename=" + filename + ", keyObjectStorage=" + keyObjectStorage + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filename == null) ? 0 : filename.hashCode());
		result = prime * result + ((keyObjectStorage == null) ? 0 : keyObjectStorage.hashCode());
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
		JobOrderInputFile other = (JobOrderInputFile) obj;
		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;
		if (keyObjectStorage == null) {
			if (other.keyObjectStorage != null)
				return false;
		} else if (!keyObjectStorage.equals(other.keyObjectStorage))
			return false;
		return true;
	}

}
