package esa.s1pdgs.cpoc.wrapper.status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.status.AppStatus;
import esa.s1pdgs.cpoc.status.Status;

/**
 * Application status
 * 
 * @author Viveris Technologies
 */
@Component
public class AppStatusImpl implements AppStatus {

    /**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(AppStatusImpl.class);

    /**
     * Status
     */
    private final Status status;

    /**
     * Maximal number of consecutive errors for processing
     */
    private final int maxErrorCounterProcessing;
    
    /**
     * Maximal number of consecutive errors for MQI
     */
    private final int maxErrorCounterNextMessage;

    /**
     * Indicate if the application shall be stopped
     */
    private boolean shallBeStopped;

    /**
     * MQI service for stopping the MQI
     */
    private final StatusService mqiStatusService;

    /**
     * Identifier of the processing message
     */
    private long processingMsgId;

    /**
     * Constructor
     * 
     * @param maxErrorCounter
     */
    @Autowired
    public AppStatusImpl(
            @Value("${status.max-error-counter-processing}") final int maxErrorCounterProcessing,
            @Value("${status.max-error-counter-mqi}") final int maxErrorCounterNextMessage,
            @Qualifier("mqiServiceForStatus") final StatusService mqiStatusService) {
        this.status = new Status();
        this.shallBeStopped = false;
        this.maxErrorCounterProcessing = maxErrorCounterProcessing;
        this.maxErrorCounterNextMessage = maxErrorCounterNextMessage;
        this.mqiStatusService = mqiStatusService;
        this.processingMsgId = 0;
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
        this.processingMsgId = 0;
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
            this.status.setErrorCounterProcessing(maxErrorCounterProcessing);
        } else if(type.equals("NEXT_MESSAGE")) {
            this.status.setErrorCounterNextMessage(maxErrorCounterNextMessage);
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

    /**
     * Stop the application if someone asks for forcing stop
     */
    @Override
	@Scheduled(fixedDelayString = "${status.delete-fixed-delay-ms}")
    public void forceStopping() {
        if (this.isShallBeStopped()) {
            try {
                mqiStatusService.stop();
            } catch (AbstractCodedException ace) {
                LOGGER.error(ace.getLogMessage());
            }
            System.exit(0);
        }
    }
}
