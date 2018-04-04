package fr.viveris.s1pdgs.scaler.k8s.model;

import java.util.Date;

/**
 * @author Olivier Bex-Chauvet
 *
 */
public class WrapperStatus {
	
	/**
	 * Enum of all the status of the wrappers
	 *
	 */
	public enum Status {
		WAITING, PROCESSING, STOPPING, ERROR, FATALERROR
	}

	private Status status;
	
	private Date lastChange;
	
	private int errorCounter;
	
	public WrapperStatus() {
	}
	
	public WrapperStatus(Status status, Date lastChange, int errorCounter) {
		this.status = status;
		this.lastChange = lastChange;
		this.errorCounter = errorCounter;
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
	 * @return the lastChange
	 */
	public Date getLastChange() {
		return lastChange;
	}

	/**
	 * @param lastChange the lastChange to set
	 */
	public void setLastChange(Date lastChange) {
		this.lastChange = lastChange;
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
