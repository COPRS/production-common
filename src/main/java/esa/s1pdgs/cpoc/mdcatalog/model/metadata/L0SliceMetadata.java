package esa.s1pdgs.cpoc.mdcatalog.model.metadata;

public class L0SliceMetadata extends AbstractMetadata {
	
	private int instrumentConfigurationId;
	
	private int numberSlice;
	
	private String datatakeId;

	public L0SliceMetadata() {
		super();
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
		return "L0SliceMetadata [instrumentConfigurationId= " + instrumentConfigurationId + ", numberSlice= "
				+ numberSlice + ", datatakeId= " + datatakeId + ", productName= " + productName + ", productType= "
				+ productType + ", keyObjectStorage= " + keyObjectStorage + ", validityStart= " + validityStart
				+ ", validityStop= " + validityStop + "]";
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
		L0SliceMetadata other = (L0SliceMetadata) obj;
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
