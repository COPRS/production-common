/**
 * 
 */
package fr.viveris.s1pdgs.mdcatalog.model;

import java.util.Objects;

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
	 * Constructor 
	 * 
	 * @param productType
	 * @param productClass
	 * @param resolution
	 * @param swathtype
	 * @param polarisation
	 * @param dataTakeId
	 */
	public L0OutputFileDescriptor(String productType, String productClass, String resolution, String swathtype,
			String polarisation, String dataTakeId) {
		super();
		this.productType = productType;
		this.productClass = productClass;
		this.resolution = resolution;
		this.swathtype = swathtype;
		this.polarisation = polarisation;
		this.dataTakeId = dataTakeId;
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

	@Override
	public boolean equals(Object o) {
		// self check
		if (this == o)
			return true;
		// null check
		if (o == null)
			return false;
		// type check and cast
		if (getClass() != o.getClass())
			return false;
		L0OutputFileDescriptor l0OutputFileDescriptor = (L0OutputFileDescriptor) o;
		// field comparison
		return Objects.equals(keyObjectStorage, l0OutputFileDescriptor.getKeyObjectStorage());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(relativePath, filename, extension, productName, productClass, productType, missionId,
				satelliteId, keyObjectStorage, dataTakeId);
	}
}
