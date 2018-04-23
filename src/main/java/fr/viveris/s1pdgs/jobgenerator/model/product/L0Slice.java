package fr.viveris.s1pdgs.jobgenerator.model.product;

public class L0Slice {
	
	private String acquisition;
	
	private String dataTakeId;
	
	private int numberSlice;
	
	private int totalNumberOfSlice;
	
	private String startDateFromMetadata;
	
	private String stopDateFromMetadata;

	public L0Slice() {
		numberSlice = 0;
		totalNumberOfSlice = 0;
	}

	public L0Slice(String acquisition) {
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
	 * @return the dataTakeId
	 */
	public String getDataTakeId() {
		return dataTakeId;
	}

	/**
	 * @param dataTakeId the dataTakeId to set
	 */
	public void setDataTakeId(String dataTakeId) {
		this.dataTakeId = dataTakeId;
	}

	/**
	 * @param acquisition the acquisition to set
	 */
	public void setAcquisition(String acquisition) {
		this.acquisition = acquisition;
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
	 * @return the totalNumberOfSlice
	 */
	public int getTotalNumberOfSlice() {
		return totalNumberOfSlice;
	}

	/**
	 * @param totalNumberOfSlice the totalNumberOfSlice to set
	 */
	public void setTotalNumberOfSlice(int totalNumberOfSlice) {
		this.totalNumberOfSlice = totalNumberOfSlice;
	}

	/**
	 * @return the startDateFromMetadata
	 */
	public String getStartDateFromMetadata() {
		return startDateFromMetadata;
	}

	/**
	 * @param startDateFromMetadata the startDateFromMetadata to set
	 */
	public void setStartDateFromMetadata(String startDateFromMetadata) {
		this.startDateFromMetadata = startDateFromMetadata;
	}

	/**
	 * @return the stopDateFromMetadata
	 */
	public String getStopDateFromMetadata() {
		return stopDateFromMetadata;
	}

	/**
	 * @param stopDateFromMetadata the stopDateFromMetadata to set
	 */
	public void setStopDateFromMetadata(String stopDateFromMetadata) {
		this.stopDateFromMetadata = stopDateFromMetadata;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "L0Slice [acquisition=" + acquisition + ", dataTakeId=" + dataTakeId + ", numberSlice=" + numberSlice
				+ ", totalNumberOfSlice=" + totalNumberOfSlice + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((acquisition == null) ? 0 : acquisition.hashCode());
		result = prime * result + ((dataTakeId == null) ? 0 : dataTakeId.hashCode());
		result = prime * result + numberSlice;
		result = prime * result + totalNumberOfSlice;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		L0Slice other = (L0Slice) obj;
		if (acquisition == null) {
			if (other.acquisition != null)
				return false;
		} else if (!acquisition.equals(other.acquisition))
			return false;
		if (dataTakeId == null) {
			if (other.dataTakeId != null)
				return false;
		} else if (!dataTakeId.equals(other.dataTakeId))
			return false;
		if (numberSlice != other.numberSlice)
			return false;
		if (totalNumberOfSlice != other.totalNumberOfSlice)
			return false;
		return true;
	}

}
