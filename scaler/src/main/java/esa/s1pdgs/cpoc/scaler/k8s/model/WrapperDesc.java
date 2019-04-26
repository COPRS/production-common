package esa.s1pdgs.cpoc.scaler.k8s.model;

public class WrapperDesc {
	
	private String name;
	
	private PodLogicalStatus status;
	
	private long timeSinceLastChange;
	
	private int errorCounter;

	public WrapperDesc(String name) {
		this.name = name;
		this.status = PodLogicalStatus.WAITING;
		timeSinceLastChange = 0;
		errorCounter = 0;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the status
	 */
	public PodLogicalStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(PodLogicalStatus status) {
		this.status = status;
	}

	/**
	 * @return the timeSinceLastChange
	 */
	public long getTimeSinceLastChange() {
		return timeSinceLastChange;
	}

	/**
	 * @param timeSinceLastChange the timeSinceLastChange to set
	 */
	public void setTimeSinceLastChange(long timeSinceLastChange) {
		this.timeSinceLastChange = timeSinceLastChange;
	}

	/**
	 * @return the errorCounter
	 */
	public int getErrorCounter() {
		return errorCounter;
	}

	/**
	 * @param errorCounter the errorCounter to set
	 */
	public void setErrorCounter(int errorCounter) {
		this.errorCounter = errorCounter;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{name: " + name + ", status: " + status + ", timeSinceLastChange: " + timeSinceLastChange
				+ ", errorCounter: " + errorCounter + "}";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + errorCounter;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + (int) (timeSinceLastChange ^ (timeSinceLastChange >>> 32));
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
		WrapperDesc other = (WrapperDesc) obj;
		if (errorCounter != other.errorCounter)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (status != other.status)
			return false;
		if (timeSinceLastChange != other.timeSinceLastChange)
			return false;
		return true;
	}

}
