package esa.s1pdgs.cpoc.status;

import java.util.Map;

import esa.s1pdgs.cpoc.common.ProductCategory;

public abstract class AbstractAppStatus implements AppStatus {

    /**
     * Status of application
     */
    protected final Status status;
    
    /**
     * Indicate if the application shall be stopped
     */
    private boolean shallBeStopped;

    public AbstractAppStatus(Status status) {
		this.status = status;
		shallBeStopped = false;
	}
    
    /**
     * @return the application status
     */
    @Override
	public synchronized Status getStatus() {
        return status;
    }

    /**
     * @return the status per category
     */
	@Override
	public Map<ProductCategory, Status> getSubStatuses() {
		return status.getSubStatuses();
	}
	
	@Override
	public void addSubStatus(Status status) {
		if (!status.getCategory().isPresent()) {
			throw new IllegalArgumentException("Assignment as a substatus failed because category attribute is not present");
		}
		this.status.getSubStatuses().put(status.getCategory().get(), status);
	}
    
    /**
     * @return the processingMsgId
     */
    @Override
	public long getProcessingMsgId() {
        return status.getProcessingMsgId();
    }

    /**
     * Set application as waiting
     */
    @Override
	public synchronized void setWaiting() {
        status.setWaiting();
    }

    /**
     * Set application as processing
     */
    @Override
	public synchronized void setProcessing(final long processingMsgId) {
        this.status.setProcessing(processingMsgId);
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
    	if("NEXT_MESSAGE".equals(type) || "MQI".equals(type)) {
    		this.status.incrementErrorCounterNextMessage();
    	} else  if("PROCESSING".equals(type) || "JOB".equals(type)) {
            this.status.incrementErrorCounterProcessing();
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
