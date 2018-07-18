package esa.s1pdgs.cpoc.mdcatalog.model.metadata;

public class L0AcnMetadata extends AbstractMetadata {
	
	private int instrumentConfigurationId;
	
	private int numberOfSlices;
	
	private String datatakeId;

	public L0AcnMetadata() {
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "L0AcnMetadata [instrumentConfigurationId= " + instrumentConfigurationId + ", numberOfSlices= "
				+ numberOfSlices + ", datatakeId= " + datatakeId + ", productName= " + productName + ", productType= "
				+ productType + ", keyObjectStorage= " + keyObjectStorage + ", validityStart= " + validityStart
				+ ", validityStop= " + validityStop + "]";
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
		L0AcnMetadata other = (L0AcnMetadata) obj;
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
