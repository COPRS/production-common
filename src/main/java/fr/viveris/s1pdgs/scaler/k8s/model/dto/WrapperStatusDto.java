package fr.viveris.s1pdgs.scaler.k8s.model.dto;

/**
 * @author Olivier Bex-Chauvet
 *
 */
public class WrapperStatusDto {
	
	/**
	 * Enum of all the status of the wrappers
	 *
	 */
	public enum Status {
		WAITING, PROCESSING, STOPPING, ERROR, FATALERROR
	}

	private Status status;
	
	private long timeSinceLastChange;
	
	private int errorCounter;
	
	public WrapperStatusDto() {
		
	}

	/**
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Status status) {
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
