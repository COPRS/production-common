package esa.s1pdgs.cpoc.mdcatalog.rest.dto;

/**
 * Class describing the metdata of a file
 * 
 * @author Cyrielle Gailliard
 *
 */
public class L0AcnMetadataDto extends MetadataDto {
	
	private int instrumentConfigurationId;
	
	private int numberOfSlices;
	
	private String datatakeId;

	public L0AcnMetadataDto(String productName, String productType, String keyObjectStorage,
			String validityStart, String validityStop) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop);
	}

	public L0AcnMetadataDto(L0AcnMetadataDto obj) {
		this(obj.getProductName(), obj.getProductType(), obj.getKeyObjectStorage(), obj.getValidityStart(),
				obj.getValidityStop());
	}

	/**
	 * @return the instrumentConfigurationId
	 */
	public int getInstrumentConfigurationId() {
		return instrumentConfigurationId;
	}

	/**
	 * @param instrumentConfigurationId the instrumentConfigurationId to set
	 */
	public void setInstrumentConfigurationId(int instrumentConfigurationId) {
		this.instrumentConfigurationId = instrumentConfigurationId;
	}

	/**
	 * @return the numberOfSlices
	 */
	public int getNumberOfSlices() {
		return numberOfSlices;
	}

	/**
	 * @param numberOfSlices the numberOfSlices to set
	 */
	public void setNumberOfSlices(int numberOfSlices) {
		this.numberOfSlices = numberOfSlices;
	}

	/**
	 * @return the datatakeId
	 */
	public String getDatatakeId() {
		return datatakeId;
	}

	/**
	 * @param datatakeId the datatakeId to set
	 */
	public void setDatatakeId(String datatakeId) {
		this.datatakeId = datatakeId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String superToString = super.toMetadataString();
		return String.format("{%s,\"instrumentConfigurationId\":%s,\"numberOfSlices\":%s,\"datatakeId\":\"%s\"}", superToString,
				instrumentConfigurationId, numberOfSlices, datatakeId);
	}
	


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((datatakeId == null) ? 0 : datatakeId.hashCode());
		result = prime * result + instrumentConfigurationId;
		result = prime * result + numberOfSlices;
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
		L0AcnMetadataDto other = (L0AcnMetadataDto) obj;
		if (datatakeId == null) {
			if (other.datatakeId != null)
				return false;
		} else if (!datatakeId.equals(other.datatakeId))
			return false;
		if (instrumentConfigurationId != other.instrumentConfigurationId)
			return false;
		if (numberOfSlices != other.numberOfSlices)
			return false;
		return true;
	}
	
}
