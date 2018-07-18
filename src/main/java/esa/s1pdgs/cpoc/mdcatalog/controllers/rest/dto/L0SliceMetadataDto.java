package esa.s1pdgs.cpoc.mdcatalog.controllers.rest.dto;

/**
 * Class describing the metdata of a file
 * 
 * @author Cyrielle Gailliard
 *
 */
public class L0SliceMetadataDto extends MetadataDto {
	
	private int instrumentConfigurationId;
	
	private int numberSlice;
	
	private String datatakeId;

	public L0SliceMetadataDto(String productName, String productType, String keyObjectStorage,
			String validityStart, String validityStop) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop);
	}

	public L0SliceMetadataDto(L0SliceMetadataDto obj) {
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
	 * @return the numberSlice
	 */
	public int getNumberSlice() {
		return numberSlice;
	}

	/**
	 * @param numberSlice the numberSlice to set
	 */
	public void setNumberSlice(int numberSlice) {
		this.numberSlice = numberSlice;
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
		return String.format("{%s,\"instrumentConfigurationId\":%s,\"numberSlice\":%s,\"datatakeId\":\"%s\"}", superToString,
				instrumentConfigurationId, numberSlice, datatakeId);
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
		result = prime * result + numberSlice;
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
		L0SliceMetadataDto other = (L0SliceMetadataDto) obj;
		if (datatakeId == null) {
			if (other.datatakeId != null)
				return false;
		} else if (!datatakeId.equals(other.datatakeId))
			return false;
		if (instrumentConfigurationId != other.instrumentConfigurationId)
			return false;
		if (numberSlice != other.numberSlice)
			return false;
		return true;
	}
	
}
