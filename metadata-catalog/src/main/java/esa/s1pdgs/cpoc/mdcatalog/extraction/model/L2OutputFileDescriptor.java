package esa.s1pdgs.cpoc.mdcatalog.extraction.model;

/**
 * @author birol_colak@net.werum
 *
 */
public class L2OutputFileDescriptor extends AbstractFileDescriptor {

	private String productType;

	private String productClass;

	private String resolution;

	private String swathtype;

	private String polarisation;

	private String dataTakeId;

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getProductClass() {
		return productClass;
	}

	public void setProductClass(String productClass) {
		this.productClass = productClass;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public String getSwathtype() {
		return swathtype;
	}

	public void setSwathtype(String swathtype) {
		this.swathtype = swathtype;
	}

	public String getPolarisation() {
		return polarisation;
	}

	public void setPolarisation(String polarisation) {
		this.polarisation = polarisation;
	}

	public String getDataTakeId() {
		return dataTakeId;
	}

	public void setDataTakeId(String dataTakeId) {
		this.dataTakeId = dataTakeId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "L2OutputFileDescriptor [productType=" + productType + ", productClass=" + productClass + ", resolution="
				+ resolution + ", swathtype=" + swathtype + ", polarisation=" + polarisation + ", dataTakeId="
				+ dataTakeId + ", relativePath=" + relativePath + ", filename=" + filename + ", extension=" + extension
				+ ", productName=" + productName + ", missionId=" + missionId + ", satelliteId=" + satelliteId
				+ ", keyObjectStorage=" + keyObjectStorage + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((dataTakeId == null) ? 0 : dataTakeId.hashCode());
		result = prime * result + ((polarisation == null) ? 0 : polarisation.hashCode());
		result = prime * result + ((productClass == null) ? 0 : productClass.hashCode());
		result = prime * result + ((productType == null) ? 0 : productType.hashCode());
		result = prime * result + ((resolution == null) ? 0 : resolution.hashCode());
		result = prime * result + ((swathtype == null) ? 0 : swathtype.hashCode());
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
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		L2OutputFileDescriptor other = (L2OutputFileDescriptor) obj;
		if (dataTakeId == null) {
			if (other.dataTakeId != null)
				return false;
		} else if (!dataTakeId.equals(other.dataTakeId))
			return false;
		if (polarisation == null) {
			if (other.polarisation != null)
				return false;
		} else if (!polarisation.equals(other.polarisation))
			return false;
		if (productClass == null) {
			if (other.productClass != null)
				return false;
		} else if (!productClass.equals(other.productClass))
			return false;
		if (productType == null) {
			if (other.productType != null)
				return false;
		} else if (!productType.equals(other.productType))
			return false;
		if (resolution == null) {
			if (other.resolution != null)
				return false;
		} else if (!resolution.equals(other.resolution))
			return false;
		if (swathtype == null) {
			if (other.swathtype != null)
				return false;
		} else if (!swathtype.equals(other.swathtype))
			return false;
		return true;
	}

}
