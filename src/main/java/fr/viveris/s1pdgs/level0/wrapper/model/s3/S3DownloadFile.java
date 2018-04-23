package fr.viveris.s1pdgs.level0.wrapper.model.s3;

import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;

public class S3DownloadFile extends S3CustomObject {
	
	private String localPath;

	public S3DownloadFile(ProductFamily family, String key, String localPath) {
		super(family, key);
		this.localPath = localPath;
	}

	/**
	 * @return the localPath
	 */
	public String getLocalPath() {
		return localPath;
	}

	/**
	 * @param localPath the localPath to set
	 */
	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "S3DownloadFile [localPath=" + localPath + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((localPath == null) ? 0 : localPath.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		S3DownloadFile other = (S3DownloadFile) obj;
		if (localPath == null) {
			if (other.localPath != null)
				return false;
		} else if (!localPath.equals(other.localPath))
			return false;
		return true;
	}

}
