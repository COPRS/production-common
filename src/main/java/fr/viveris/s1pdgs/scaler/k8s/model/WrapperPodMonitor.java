package fr.viveris.s1pdgs.scaler.k8s.model;

public class WrapperPodMonitor {
	
	private String podName;
	
	private PodStatus status;
	
	private PodLogicalStatus logicalStatus;
	
	private long passedExecutionTime;
	
	private long remainingExecutionTime;

	public WrapperPodMonitor() {
		this.logicalStatus = PodLogicalStatus.WAITING;
		this.passedExecutionTime = 0;
		this.remainingExecutionTime = 0;
	}

	/**
	 * @param podName
	 * @param status
	 * @param logicalStatus
	 */
	public WrapperPodMonitor(String podName) {
		this();
	}

	/**
	 * @return the podName
	 */
	public String getPodName() {
		return podName;
	}

	/**
	 * @param podName the podName to set
	 */
	public void setPodName(String podName) {
		this.podName = podName;
	}

	/**
	 * @return the status
	 */
	public PodStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(PodStatus status) {
		this.status = status;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{podName: " + podName + ", status: " + status + ", logicalStatus: " + logicalStatus
				+ ", passedExecutionTime: " + passedExecutionTime + ", remainingExecutionTime: " + remainingExecutionTime
				+ "}";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((logicalStatus == null) ? 0 : logicalStatus.hashCode());
		result = prime * result + (int) (passedExecutionTime ^ (passedExecutionTime >>> 32));
		result = prime * result + ((podName == null) ? 0 : podName.hashCode());
		result = prime * result + (int) (remainingExecutionTime ^ (remainingExecutionTime >>> 32));
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
		WrapperPodMonitor other = (WrapperPodMonitor) obj;
		if (logicalStatus != other.logicalStatus)
			return false;
		if (passedExecutionTime != other.passedExecutionTime)
			return false;
		if (podName == null) {
			if (other.podName != null)
				return false;
		} else if (!podName.equals(other.podName))
			return false;
		if (remainingExecutionTime != other.remainingExecutionTime)
			return false;
		if (status != other.status)
			return false;
		return true;
	}

}
