package fr.viveris.s1pdgs.jobgenerator.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "dsdb_name")
@XmlAccessorType(XmlAccessType.NONE)
public class EdrsSessionFileRaw {
	
	@XmlValue
	private String fileName;
	
	private String objectStorageKey;

	public EdrsSessionFileRaw() {
		
	}

	/**
	 * @param fileName
	 * @param objectStorageKey
	 */
	public EdrsSessionFileRaw(String fileName) {
		super();
		this.fileName = fileName;
	}

	/**
	 * @param fileName
	 * @param objectStorageKey
	 */
	public EdrsSessionFileRaw(String fileName, String objectStorageKey) {
		this(fileName);
		this.objectStorageKey = objectStorageKey;
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
	 * @return the objectStorageKey
	 */
	public String getObjectStorageKey() {
		return objectStorageKey;
	}

	/**
	 * @param objectStorageKey the objectStorageKey to set
	 */
	public void setObjectStorageKey(String objectStorageKey) {
		this.objectStorageKey = objectStorageKey;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EdrsSessionFileRaw [fileName=" + fileName + ", objectStorageKey=" + objectStorageKey + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((objectStorageKey == null) ? 0 : objectStorageKey.hashCode());
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
		EdrsSessionFileRaw other = (EdrsSessionFileRaw) obj;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (objectStorageKey == null) {
			if (other.objectStorageKey != null)
				return false;
		} else if (!objectStorageKey.equals(other.objectStorageKey))
			return false;
		return true;
	}

}
