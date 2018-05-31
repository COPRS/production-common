package fr.viveris.s1pdgs.jobgenerator.model.metadata;

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
	private int insConfId;

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
			final String validityStart, final String validityStop, final int insConfId, final int numberSlice,
			final String dataTakeId) {
		super(productName, productType, keyObjectStorage, validityStart, validityStop);
		this.insConfId = insConfId;
		this.numberSlice = numberSlice;
		this.datatakeId = dataTakeId;
	}

	/**
	 * @return the instrumentConfigurationId
	 */
	public int getInsConfId() {
		return insConfId;
	}

	/**
	 * @param instrumentConfigurationId
	 *            the instrumentConfigurationId to set
	 */
	public void setInsConfId(final int instrumentConfigurationId) {
		this.insConfId = instrumentConfigurationId;
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

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String superToString = super.toAbstractString();
		return String.format("{%s, insConfId: %s, numberSlice: %s, datatakeId: %s}", superToString,
				insConfId, numberSlice, datatakeId);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int superHash = super.hashCode();
		return Objects.hash(insConfId, numberSlice, datatakeId, superHash);
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
			ret = super.equals(other) && insConfId == other.insConfId && numberSlice == other.numberSlice
					&& Objects.equals(datatakeId, other.datatakeId);
		}
		return ret;
	}
}
