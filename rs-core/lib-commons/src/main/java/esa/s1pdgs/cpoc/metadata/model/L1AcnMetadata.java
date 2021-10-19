package esa.s1pdgs.cpoc.metadata.model;

import java.util.Objects;

public class L1AcnMetadata extends AbstractMetadata {

	/**
	 * Instrument configuration id
	 */
	private int instrumentConfigurationId;

	/**
	 * Number of slices
	 */
	private int numberOfSlices;

	/**
	 * Data take identifier
	 */
	private String datatakeId;

	/**
	 * @param instrumentConfigurationId
	 * @param numberSlice
	 */
	public L1AcnMetadata(final String productName, final String productType, final String keyObjectStorage,
			final String validityStart, final String validityStop, final String missionId, final String satelliteId, final String stationCode, final int instrumentConfigurationId,
			final int numberOfSlices, final String dataTakeId) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop, missionId, satelliteId, stationCode);
		this.instrumentConfigurationId = instrumentConfigurationId;
		this.numberOfSlices = numberOfSlices;
		this.datatakeId = dataTakeId;
	}
	
	public L1AcnMetadata(final String productName, final String productType, final String keyObjectStorage,
			final String validityStart, final String validityStop, final String missionId, final String satelliteId, final String stationCode) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop, missionId, satelliteId, stationCode);
	}
	
	public L1AcnMetadata() {
		
	}

	/**
	 * @return the instrumentConfigurationId
	 */
	public int getInstrumentConfigurationId() {
		return instrumentConfigurationId;
	}

	/**
	 * @param instrumentConfigurationId
	 *            the instrumentConfigurationId to set
	 */
	public void setInstrumentConfigurationId(final int instrumentConfigurationId) {
		this.instrumentConfigurationId = instrumentConfigurationId;
	}

	/**
	 * @return the numberOfSlices
	 */
	public int getNumberOfSlices() {
		return numberOfSlices;
	}

	/**
	 * @param numberOfSlices
	 *            the numberOfSlices to set
	 */
	public void setNumberOfSlices(final int numberOfSlices) {
		this.numberOfSlices = numberOfSlices;
	}

	/**
	 * @return the datatakeId
	 */
	public String getDatatakeId() {
		return datatakeId;
	}

	/**
	 * @param datatakeId
	 *            the datatakeId to set
	 */
	public void setDatatakeId(final String datatakeId) {
		this.datatakeId = datatakeId;
	}


	public String toJsonString() {
		String superToString = super.toAbstractString();
		return String.format("{%s,\"instrumentConfigurationId\":%s,\"numberOfSlices\":%s,\"datatakeId\":\"%s\"}", superToString,
				instrumentConfigurationId, numberOfSlices, datatakeId);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int superHash = super.hashCode();
		return Objects.hash(instrumentConfigurationId, numberOfSlices, datatakeId, superHash);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (this == obj) {
			ret = true;
		} else if (obj == null || getClass() != obj.getClass()) {
			ret = false;
		} else {
			L1AcnMetadata other = (L1AcnMetadata) obj;
			ret = super.equals(other) && instrumentConfigurationId == other.instrumentConfigurationId
					&& numberOfSlices == other.numberOfSlices && Objects.equals(datatakeId, other.datatakeId);
		}
		return ret;
	}

	@Override
	public String toString() {
		return "L1AcnMetadata [instrumentConfigurationId=" + instrumentConfigurationId + ", numberOfSlices="
				+ numberOfSlices + ", datatakeId=" + datatakeId + ", productName=" + productName + ", productType="
				+ productType + ", keyObjectStorage=" + keyObjectStorage + ", validityStart=" + validityStart
				+ ", validityStop=" + validityStop + ", missionId=" + missionId + ", satelliteId=" + satelliteId
				+ ", stationCode=" + stationCode + "]";
	}
	
}
