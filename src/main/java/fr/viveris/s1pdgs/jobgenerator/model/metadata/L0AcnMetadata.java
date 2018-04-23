package fr.viveris.s1pdgs.jobgenerator.model.metadata;

public class L0AcnMetadata extends AbstractMetadata {
	
	private int instrumentConfigurationId;
	
	private int numberOfSlices;
	
	private String datatakeId;

	public L0AcnMetadata() {
		super();
		numberOfSlices = 0;
		instrumentConfigurationId = -1;
	}

	/**
	 * @param instrumentConfigurationId
	 * @param numberSlice
	 */
	public L0AcnMetadata(String productName, String productType, String keyObjectStorage, String validityStart,
			String validityStop, int instrumentConfigurationId, int numberOfSlices, String dataTakeId) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop);
		this.instrumentConfigurationId = instrumentConfigurationId;
		this.numberOfSlices = numberOfSlices;
		this.datatakeId = dataTakeId;
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

}
