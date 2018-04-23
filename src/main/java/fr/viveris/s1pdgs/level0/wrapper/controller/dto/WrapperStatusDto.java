package fr.viveris.s1pdgs.level0.wrapper.controller.dto;

import fr.viveris.s1pdgs.level0.wrapper.AppState;

/**
 * @author Olivier Bex-Chauvet
 *
 */
public class WrapperStatusDto {

	private AppState status;
	
	private long timeSinceLastChange;
	
	private int errorCounter;
	
	/**
	 * @param state
	 * @param lastChange
	 * @param errorCounter
	 */
	public WrapperStatusDto(AppState state, long timeSinceLastChange, int errorCounter) {
		super();
		this.status = state;
		this.timeSinceLastChange = timeSinceLastChange;
		this.errorCounter = errorCounter;
	}

	/**
	 * @return the status
	 */
	public AppState getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(AppState status) {
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
