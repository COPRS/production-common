package esa.s1pdgs.cpoc.appstatus;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductCategory;

public abstract class AbstractAppStatus implements AppStatus {
	
	private static final Logger LOG = LogManager.getLogger(AbstractAppStatus.class);
	
    /**
     * Status of application
     */
    protected final Status status;
    
    /**
     * Indicate if the application shall be stopped
     */
    private boolean shallBeStopped = false;
    	
	private final Runnable systemExitCall;

    public AbstractAppStatus(final Status status, final Runnable systemExitCall) {
		this.status = status;
		this.systemExitCall = systemExitCall;
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
	public void addSubStatus(final Status status) {
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
	public synchronized void setError(final String type) {
    	if("NEXT_MESSAGE".equals(type) || "MQI".equals(type)) { // TODO: Refactor these MQI client error synonyms
    		this.status.incrementErrorCounterNextMessage();
    	} else if("PROCESSING".equals(type) || "JOB".equals(type)) { // TODO: Refactor these main task synonyms
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
	public boolean isProcessing(final String category, final long messageId) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @return kubernetes readiness
	 */
	@Override
	public boolean getKubernetesReadiness() {
		return true;
	}
	
	@Override
	abstract public void forceStopping();
	
	protected final void systemExit() {
		LOG.warn("========== System exit called ========== ");
		systemExitCall.run();
	}

}
