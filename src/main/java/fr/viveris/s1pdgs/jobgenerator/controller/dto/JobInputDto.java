package fr.viveris.s1pdgs.jobgenerator.controller.dto;

import fr.viveris.s1pdgs.jobgenerator.model.ProductFamily;

/**
 * DTO object used to describe a input in the job kafka topic
 * @author Cyrielle Gailliard
 * @see JobDto
 *
 */
public class JobInputDto {

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
	 * The reference to the content. Can be the object storage or directly in the
	 * string according the family
	 */
	private String contentRef;

	public JobInputDto() {

	}

	/**
	 * @param family
	 * @param localPath
	 * @param contentRef
	 */
	public JobInputDto(String family, String localPath, String contentRef) {
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
	public void setFamily(String family) {
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
	public void setLocalPath(String localPath) {
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
	public void setContentRef(String contentRef) {
		this.contentRef = contentRef;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JobInputDto [family=" + family + ", localPath=" + localPath + ", contentRef=" + contentRef + "]";
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
		result = prime * result + ((contentRef == null) ? 0 : contentRef.hashCode());
		result = prime * result + ((family == null) ? 0 : family.hashCode());
		result = prime * result + ((localPath == null) ? 0 : localPath.hashCode());
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
		JobInputDto other = (JobInputDto) obj;
		if (contentRef == null) {
			if (other.contentRef != null)
				return false;
		} else if (!contentRef.equals(other.contentRef))
			return false;
		if (family == null) {
			if (other.family != null)
				return false;
		} else if (!family.equals(other.family))
			return false;
		if (localPath == null) {
			if (other.localPath != null)
				return false;
		} else if (!localPath.equals(other.localPath))
			return false;
		return true;
	}

}
