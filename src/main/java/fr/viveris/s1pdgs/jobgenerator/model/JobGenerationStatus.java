package fr.viveris.s1pdgs.jobgenerator.model;

public class JobGenerationStatus {
	
	private GenerationStatusEnum status;
	
	private long lastModifiedTime;
	
	private int nbRetries;

	public JobGenerationStatus() {
		status = GenerationStatusEnum.NOT_READY;
		lastModifiedTime = 0;
		nbRetries = 0;
	}

	/**
	 * @return the status
	 */
	public GenerationStatusEnum getStatus() {
		return status;
	}
	
	/**
	 * If same status, consider it is an error
	 * @param status
	 */
	public void updateStatus(GenerationStatusEnum status) {
		if (this.status == status) {
			lastModifiedTime = System.currentTimeMillis();
			nbRetries ++;
		} else {
			this.status = status;
			nbRetries = 0;
		}
	}

	/**
	 * @return the lastModifiedTime
	 */
	public long getLastModifiedTime() {
		return lastModifiedTime;
	}

	/**
	 * @return the nbRetries
	 */
	public int getNbRetries() {
		return nbRetries;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JobGenerationStatus [status=" + status + ", lastModifiedTime=" + lastModifiedTime + ", nbRetries="
				+ nbRetries + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (lastModifiedTime ^ (lastModifiedTime >>> 32));
		result = prime * result + nbRetries;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
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
		JobGenerationStatus other = (JobGenerationStatus) obj;
		if (lastModifiedTime != other.lastModifiedTime)
			return false;
		if (nbRetries != other.nbRetries)
			return false;
		if (status != other.status)
			return false;
		return true;
	}

}
