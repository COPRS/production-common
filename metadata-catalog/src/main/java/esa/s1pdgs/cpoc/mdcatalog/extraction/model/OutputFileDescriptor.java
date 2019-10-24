/**
 * 
 */
package esa.s1pdgs.cpoc.mdcatalog.extraction.model;


/**
 * Class describing a output file
 * 
 * @author Olivier Bex-Chauvet
 *
 */
public class OutputFileDescriptor extends AbstractFileDescriptor {
	
	/**
	 * Resolution
	 */
	private String resolution;
	
	/**
	 * Swathtype
	 */
	private String swathtype;
	
	/**
	 * Polarisation
	 */
	private String polarisation;
	
	/**
	 * DataTakeId
	 */
	private String dataTakeId;
	
	/**
	 * Default Constructor
	 */
	public OutputFileDescriptor() {
	}


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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "OutputFileDescriptor [productType=" + productType + ", productClass=" + productClass + ", resolution="
				+ resolution + ", swathtype=" + swathtype + ", polarisation=" + polarisation + ", dataTakeId="
				+ dataTakeId + ", relativePath=" + relativePath + ", filename=" + filename + ", extension=" + extension
				+ ", productName=" + productName + ", missionId=" + missionId + ", satelliteId=" + satelliteId
				+ ", keyObjectStorage=" + keyObjectStorage + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
	    if (obj == null || getClass() != obj.getClass())
	    	return false;
		if (!super.equals(obj))
			return false;
		OutputFileDescriptor other = (OutputFileDescriptor) obj;
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
	
	/* (non-Javadoc)
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
}
