package esa.s1pdgs.cpoc.status;

public interface AppStatus {	
	public static final AppStatus NULL = new AppStatus() {		
		@Override public final void setWaiting() {}		
		@Override public final void setStopping() {}		
		@Override public final void setShallBeStopped(boolean shallBeStopped) {}		
		@Override public final void setProcessing(long processingMsgId) {}
		@Override public final void setError(String type) {}
		@Override public final boolean isShallBeStopped() {return false;}
		@Override public final Status getStatus() {return null;}		
		@Override public long getProcessingMsgId() { return -1;}
		@Override public final void forceStopping() {}
	};

	/**
	 * @return the status
	 */
	Status getStatus();

	/**
	 * @return the processingMsgId
	 */
	long getProcessingMsgId();

	/**
	 * Set application as waiting
	 */
	void setWaiting();

	/**
	 * Set application as processing
	 */
	void setProcessing(long processingMsgId);

	/**
	 * Set application as stopping
	 */
	void setStopping();

	/**
	 * Set application as error
	 */
	void setError(String type);

	/**
	 * @return the shallBeStopped
	 */
	boolean isShallBeStopped();

	/**
	 * @param shallBeStopped
	 *            the shallBeStopped to set
	 */
	void setShallBeStopped(boolean shallBeStopped);

	/**
	 * Stop the application if someone asks for forcing stop
	 */
	void forceStopping();

}