package esa.s1pdgs.cpoc.status;

public abstract class AbstractAppStatus implements AppStatus {

    /**
     * Status
     */
    private final Status status;

    /**
     * Indicate if the application shall be stopped
     */
    private boolean shallBeStopped;

    /**
     * Identifier of the processing message
     */
    private long processingMsgId;

    public AbstractAppStatus(Status status) {
		this.status = status;
		shallBeStopped = false;
        processingMsgId = PROCESSING_MSG_ID_UNDEFINED;
	}
    
    /**
     * @return the status
     */
    @Override
	public synchronized Status getStatus() {
        return status;
    }

    /**
     * @return the processingMsgId
     */
    @Override
	public long getProcessingMsgId() {
        return processingMsgId;
    }

    /**
     * Set application as waiting
     */
    @Override
	public synchronized void setWaiting() {
        this.processingMsgId = PROCESSING_MSG_ID_UNDEFINED;
        this.status.setWaiting();
    }

    /**
     * Set application as processing
     */
    @Override
	public synchronized void setProcessing(final long processingMsgId) {
        this.processingMsgId = processingMsgId;
        this.status.setProcessing();
    }

    /**
     * Set application as stopping
     */
    @Override
	public synchronized void setStopping() {
        if (!this.status.isProcessing()) {
            this.setShallBeStopped(true);
        }
        this.status.setStopping();
    }

    /**
     * Set application as error
     */
    @Override
	public synchronized void setError(String type) {
        if(type.equals("PROCESSING")) {
            this.status.incrementErrorCounterProcessing();
        } else if(type.equals("NEXT_MESSAGE")) {
            this.status.incrementErrorCounterNextMessage();
        }
    }

    /**
     * @return the shallBeStopped
     */
	@Override
	public synchronized boolean isShallBeStopped() {
        return shallBeStopped;
    }

    /**
     * @param shallBeStopped
     *            the shallBeStopped to set
     */
	@Override
	public synchronized void setShallBeStopped(final boolean shallBeStopped) {
        this.shallBeStopped = shallBeStopped;
    }

	@Override
	public boolean isProcessing(String category, long messageId) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	abstract public void forceStopping();

}
