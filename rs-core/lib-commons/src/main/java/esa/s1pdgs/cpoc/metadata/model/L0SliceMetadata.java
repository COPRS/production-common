package esa.s1pdgs.cpoc.metadata.model;

import java.util.Objects;

/**
 * 
 * @author Cyrielle Gailliard
 *
 */
public class L0SliceMetadata extends AbstractMetadata {

	/**
	 * Instrument configuration identifier
	 */
	private int instrumentConfigurationId;

	/**
	 * Slice number
	 */
	private int numberSlice;

	/**
	 * Data take identifier
	 */
	private String datatakeId;

	/**
	 * @param instrumentConfigurationId
	 * @param numberSlice
	 */
	public L0SliceMetadata(final String productName, final String productType, final String keyObjectStorage,
			final String validityStart, final String validityStop, final String missionId, final String satelliteId, final String stationCode,
			final int instrumentConfigurationId, final int numberSlice, final String dataTakeId) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop, missionId, satelliteId, stationCode);
		this.instrumentConfigurationId = instrumentConfigurationId;
		this.numberSlice = numberSlice;
		this.datatakeId = dataTakeId;
	}
	
	public L0SliceMetadata(final String productName, final String productType, final String keyObjectStorage,
			final String validityStart, final String validityStop, final String missionId, final String satelliteId, final String stationCode) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop, missionId, satelliteId, stationCode);
	}
	
	public L0SliceMetadata() {
		
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
	 * @return the numberSlice
	 */
	public int getNumberSlice() {
		return numberSlice;
	}

	/**
	 * @param numberSlice
	 *            the numberSlice to set
	 */
	public void setNumberSlice(final int numberSlice) {
		this.numberSlice = numberSlice;
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
		return String.format("{%s,\"instrumentConfigurationId\":%s,\"numberSlice\":%s,\"datatakeId\":\"%s\"}", superToString,
				instrumentConfigurationId, numberSlice, datatakeId);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int superHash = super.hashCode();
		return Objects.hash(instrumentConfigurationId, numberSlice, datatakeId, superHash);
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
			L0SliceMetadata other = (L0SliceMetadata) obj;
			ret = super.equals(other) && instrumentConfigurationId == other.instrumentConfigurationId
					&& numberSlice == other.numberSlice && Objects.equals(datatakeId, other.datatakeId);
		}
		return ret;
	}

	@Override
	public String toString() {
		return "L0SliceMetadata [instrumentConfigurationId=" + instrumentConfigurationId + ", numberSlice="
				+ numberSlice + ", datatakeId=" + datatakeId + ", productName=" + productName + ", productType="
				+ productType + ", keyObjectStorage=" + keyObjectStorage + ", validityStart=" + validityStart
				+ ", validityStop=" + validityStop + ", missionId=" + missionId + ", satelliteId=" + satelliteId
				+ ", stationCode=" + stationCode + "]";
	}
	
	
}
