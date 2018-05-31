package fr.viveris.s1pdgs.jobgenerator.model.product;

import java.util.Objects;

/**
 * Describing a L0 slice
 * 
 * @author Cyrielle Gailliard
 *
 */
public class L0Slice {

	/**
	 * Acquisition
	 */
	private String acquisition;

	/**
	 * Data take identifier
	 */
	private String dataTakeId;

	/**
	 * Slice number
	 */
	private int numberSlice;

	/**
	 * Total number of slice for its segment
	 */
	private int totalNbOfSlice;

	/**
	 * Start date of the segment
	 */
	private String segmentStartDate;

	/**
	 * Stop date of the segment
	 */
	private String segmentStopDate;

	/**
	 * Default constructor
	 */
	public L0Slice() {
		numberSlice = 0;
		totalNbOfSlice = 0;
	}

	/**
	 * 
	 * @param acquisition
	 */
	public L0Slice(final String acquisition) {
		this();
		this.acquisition = acquisition;
	}

	/**
	 * @return the acquisition
	 */
	public String getAcquisition() {
		return acquisition;
	}

	/**
	 * @param acquisition
	 *            the acquisition to set
	 */
	public void setAcquisition(final String acquisition) {
		this.acquisition = acquisition;
	}

	/**
	 * @return the dataTakeId
	 */
	public String getDataTakeId() {
		return dataTakeId;
	}

	/**
	 * @param dataTakeId
	 *            the dataTakeId to set
	 */
	public void setDataTakeId(final String dataTakeId) {
		this.dataTakeId = dataTakeId;
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
	 * @return the totalNbOfSlice
	 */
	public int getTotalNbOfSlice() {
		return totalNbOfSlice;
	}

	/**
	 * @param totalNbOfSlice
	 *            the totalNbOfSlice to set
	 */
	public void setTotalNbOfSlice(final int totalNbOfSlice) {
		this.totalNbOfSlice = totalNbOfSlice;
	}

	/**
	 * @return the segmentStartDate
	 */
	public String getSegmentStartDate() {
		return segmentStartDate;
	}

	/**
	 * @param segmentStartDate
	 *            the segmentStartDate to set
	 */
	public void setSegmentStartDate(final String segmentStartDate) {
		this.segmentStartDate = segmentStartDate;
	}

	/**
	 * @return the segmentStopDate
	 */
	public String getSegmentStopDate() {
		return segmentStopDate;
	}

	/**
	 * @param segmentStopDate
	 *            the segmentStopDate to set
	 */
	public void setSegmentStopDate(final String segmentStopDate) {
		this.segmentStopDate = segmentStopDate;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format(
				"{acquisition: %s, dataTakeId: %s, numberSlice: %s, totalNbOfSlice: %s, segmentStartDate: %s, segmentStopDate: %s}",
				acquisition, dataTakeId, numberSlice, totalNbOfSlice, segmentStartDate, segmentStopDate);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(acquisition, dataTakeId, numberSlice, totalNbOfSlice, segmentStartDate, segmentStopDate);
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
			L0Slice other = (L0Slice) obj;
			ret = Objects.equals(acquisition, other.acquisition) && Objects.equals(dataTakeId, other.dataTakeId)
					&& numberSlice == other.numberSlice && totalNbOfSlice == other.totalNbOfSlice
					&& Objects.equals(segmentStartDate, other.segmentStartDate)
					&& Objects.equals(segmentStopDate, other.segmentStopDate);
		}
		return ret;
	}

}
