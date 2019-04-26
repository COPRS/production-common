/**
 * 
 */
package esa.s1pdgs.cpoc.mdcatalog.extraction.model;


/**
 * Class describing a L0 output file
 * 
 * @author Olivier Bex-Chauvet
 *
 */
public class L0OutputFileDescriptor extends AbstractFileDescriptor {
	
	/**
	 * Product type
	 */
	private String productType;

	/**
	 * File class
	 */
	private String productClass;
	
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
	public L0OutputFileDescriptor() {
	}


	/**
	 * @return the productType
	 */
	public String getProductType() {
		return productType;
	}

	/**
	 * @param productType the productType to set
	 */
	public void setProductType(String productType) {
		this.productType = productType;
	}

	/**
	 * @return the productClass
	 */
	public String getProductClass() {
		return productClass;
	}

	/**
	 * @param productClass the productClass to set
	 */
	public void setProductClass(String productClass) {
		this.productClass = productClass;
	}

	/**
	 * @return the resolution
	 */
	public String getResolution() {
		return resolution;
	}

	/**
	 * @param resolution the resolution to set
	 */
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	/**
	 * @return the swathtype
	 */
	public String getSwathtype() {
		return swathtype;
	}

	/**
	 * @param swathtype the swathtype to set
	 */
	public void setSwathtype(String swathtype) {
		this.swathtype = swathtype;
	}

	/**
	 * @return the polarisation
	 */
	public String getPolarisation() {
		return polarisation;
	}

	/**
	 * @param polarisation the polarisation to set
	 */
	public void setPolarisation(String polarisation) {
		this.polarisation = polarisation;
	}
	
	/**
	 * @return the dataTakeId
	 */
	public String getDataTakeId() {
		return dataTakeId;
	}

	/**
	 * @param dataTakeId the dataTakeId to set
	 */
	public void setDataTakeId(String dataTakeId) {
		this.dataTakeId = dataTakeId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "L0OutputFileDescriptor [productType=" + productType + ", productClass=" + productClass + ", resolution="
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
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		L0OutputFileDescriptor other = (L0OutputFileDescriptor) obj;
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
