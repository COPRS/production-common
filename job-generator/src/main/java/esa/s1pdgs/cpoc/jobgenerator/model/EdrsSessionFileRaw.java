package esa.s1pdgs.cpoc.mdcatalog.extraction.model;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Class describing a raw of a session<br/>
 * This class is used for the mapping of the XML EDRS session file and an
 * internal use
 * 
 * @author Cyrielle Gailliard
 *
 */
@XmlRootElement(name = "dsdb_name")
@XmlAccessorType(XmlAccessType.NONE)
public class EdrsSessionFileRaw {

	/**
	 * Filename of the raw
	 */
	@XmlValue
	private String fileName;

	/**
	 * Raw key in the object storage
	 */
	private String objectStorageKey;

	/**
	 * Default constructor
	 */
	public EdrsSessionFileRaw() {
		super();
	}

	/**
	 * Constructor from filename
	 * 
	 * @param fileName
	 * @param objectStorageKey
	 */
	public EdrsSessionFileRaw(final String fileName) {
		super();
		this.fileName = fileName;
	}

	/**
	 * Constructor using fields
	 * 
	 * @param fileName
	 * @param objectStorageKey
	 */
	public EdrsSessionFileRaw(final String fileName, final String objectStorageKey) {
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
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the objectStorageKey
	 */
	public String getObjectStorageKey() {
		return objectStorageKey;
	}

	/**
	 * @param objectStorageKey
	 *            the objectStorageKey to set
	 */
	public void setObjectStorageKey(final String objectStorageKey) {
		this.objectStorageKey = objectStorageKey;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{fileName: %s, objectStorageKey: %s}", fileName, objectStorageKey);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(fileName, objectStorageKey);
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
			EdrsSessionFileRaw other = (EdrsSessionFileRaw) obj;
			ret = Objects.equals(fileName, other.fileName) && Objects.equals(objectStorageKey, other.objectStorageKey);
		}
		return ret;
	}

}
