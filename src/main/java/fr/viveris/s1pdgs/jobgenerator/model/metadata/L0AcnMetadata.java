package fr.viveris.s1pdgs.jobgenerator.model.metadata;

import java.util.Objects;

public class L0AcnMetadata extends AbstractMetadata {

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
	public L0AcnMetadata(final String productName, final String productType, final String keyObjectStorage,
			final String validityStart, final String validityStop, final int instrumentConfigurationId,
			final int numberOfSlices, final String dataTakeId) {
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

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String superToString = super.toAbstractString();
		return String.format("{%s, instrumentConfigurationId: %s, numberOfSlices: %s, datatakeId: %s}", superToString,
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
			L0AcnMetadata other = (L0AcnMetadata) obj;
			ret = super.equals(other) && instrumentConfigurationId == other.instrumentConfigurationId
					&& numberOfSlices == other.numberOfSlices && Objects.equals(datatakeId, other.datatakeId);
		}
		return ret;
	}
}
