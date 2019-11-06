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
import esa.s1pdgs.cpoc.status.AbstractAppStatus;
import esa.s1pdgs.cpoc.status.Status;

@Component
public class AppStatusImpl extends AbstractAppStatus {

    /**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(AppStatusImpl.class);

    /**
     * MQI service for stopping the MQI
     */
    private final StatusService mqiStatusService;

    @Autowired
    public AppStatusImpl(
            @Value("${status.max-error-counter-processing}") final int maxErrorCounterProcessing,
            @Value("${status.max-error-counter-mqi}") final int maxErrorCounterNextMessage,
            @Qualifier("mqiServiceForStatus") final StatusService mqiStatusService) {
    	super(new Status(maxErrorCounterProcessing, maxErrorCounterNextMessage));
        this.mqiStatusService = mqiStatusService;
    }

    /**
     * Stop the application if someone asks for forcing stop
     */
    @Override
	@Scheduled(fixedDelayString = "${status.delete-fixed-delay-ms}")
    public void forceStopping() {
        if (isShallBeStopped()) {
            try {
                mqiStatusService.stop();
            } catch (AbstractCodedException ace) {
                LOGGER.error(ace.getLogMessage());
            }
            System.exit(0);
        }
    }
}
