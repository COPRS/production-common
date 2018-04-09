package fr.viveris.s1pdgs.scaler.k8s.model;

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

}
