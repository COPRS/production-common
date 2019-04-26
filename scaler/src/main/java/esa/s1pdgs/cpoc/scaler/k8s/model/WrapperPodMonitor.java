package esa.s1pdgs.cpoc.scaler.k8s.model;

public class WrapperPodMonitor {
	
	private PodDesc description;
	
	private PodLogicalStatus logicalStatus;
	
	private long passedExecutionTime;
	
	private long remainingExecutionTime;

	public WrapperPodMonitor(PodDesc desc) {
		this.description = desc;
		this.logicalStatus = PodLogicalStatus.STOPPING;
		this.passedExecutionTime = 0;
		this.remainingExecutionTime = 0;
	}

	/**
	 * @return the podName
	 */
	public String getPodName() {
		return description.getName();
	}

	/**
	 * @return the status
	 */
	public PodStatus getStatus() {
		return description.getStatus();
	}

	/**
	 * @return the logicalStatus
	 */
	public PodLogicalStatus getLogicalStatus() {
		return logicalStatus;
	}

	/**
	 * @param logicalStatus the logicalStatus to set
	 */
	public void setLogicalStatus(PodLogicalStatus logicalStatus) {
		this.logicalStatus = logicalStatus;
	}

	/**
	 * @return the passExecutionTime
	 */
	public long getPassedExecutionTime() {
		return passedExecutionTime;
	}

	/**
	 * @param passExecutionTime the passExecutionTime to set
	 */
	public void setPassedExecutionTime(long passedExecutionTime) {
		this.passedExecutionTime = passedExecutionTime;
	}

	/**
	 * @return the remainingExecutionTime
	 */
	public long getRemainingExecutionTime() {
		return remainingExecutionTime;
	}

	/**
	 * @param remainingExecutionTime the remainingExecutionTime to set
	 */
	public void setRemainingExecutionTime(long remainingExecutionTime) {
		this.remainingExecutionTime = remainingExecutionTime;
	}

	/**
	 * @return the description
	 */
	public PodDesc getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(PodDesc description) {
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{description: " + description + ", logicalStatus: " + logicalStatus + ", passedExecutionTime: "
				+ passedExecutionTime + ", remainingExecutionTime: " + remainingExecutionTime + "}";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((logicalStatus == null) ? 0 : logicalStatus.hashCode());
		result = prime * result + (int) (passedExecutionTime ^ (passedExecutionTime >>> 32));
		result = prime * result + (int) (remainingExecutionTime ^ (remainingExecutionTime >>> 32));
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
		WrapperPodMonitor other = (WrapperPodMonitor) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (logicalStatus != other.logicalStatus)
			return false;
		if (passedExecutionTime != other.passedExecutionTime)
			return false;
		if (remainingExecutionTime != other.remainingExecutionTime)
			return false;
		return true;
	}

}
