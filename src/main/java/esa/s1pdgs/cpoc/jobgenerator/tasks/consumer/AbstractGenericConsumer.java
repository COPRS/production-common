package esa.s1pdgs.cpoc.jobgenerator.tasks.consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.client.job.AbstractAppCatalogJobService;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.status.AppStatus;
import esa.s1pdgs.cpoc.jobgenerator.tasks.dispatcher.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public abstract class AbstractGenericConsumer<T> {

    protected static final Logger LOGGER =
            LogManager.getLogger(AbstractGenericConsumer.class);

    /**
     * Format of dates used in filename of the products
     */
    protected static final String DATE_FORMAT = "yyyyMMdd'T'HHmmss";

    /**
     * Dispatcher of l0 slices
     */
    protected final AbstractJobsDispatcher<T> jobsDispatcher;

    /**
     * Process settings
     */
    protected final ProcessSettings processSettings;

    /**
     * MQI service
     */
    protected final GenericMqiService<T> mqiService;

    /**
     * Applicative data service
     */
    protected final AbstractAppCatalogJobService<T> appDataService;

    /**
     * MQI service
     */
    protected final StatusService mqiStatusService;

    /**
     * Application status
     */
    protected final AppStatus appStatus;

    public AbstractGenericConsumer(
            final AbstractJobsDispatcher<T> jobsDispatcher,
            final ProcessSettings processSettings,
            final GenericMqiService<T> mqiService,
            final StatusService mqiStatusService,
            final AbstractAppCatalogJobService<T> appDataService,
            final AppStatus appStatus) {
        this.jobsDispatcher = jobsDispatcher;
        this.processSettings = processSettings;
        this.mqiService = mqiService;
        this.mqiStatusService = mqiStatusService;
        this.appDataService = appDataService;
        this.appStatus = appStatus;
    }

    protected GenericMessageDto<T> readMessage() {
        GenericMessageDto<T> message = null;
        try {
            message = mqiService.next();
        } catch (AbstractCodedException ace) {
            LOGGER.error("[MONITOR] [code {}] {}", ace.getCode().getCode(),
                    ace.getLogMessage());
            message = null;
            appStatus.setError("NEXT_MESSAGE");
        }
        return message;
    }

    /**
     * Ack job processing and stop app if needed
     * 
     * @param dto
     * @param ackOk
     * @param errorMessage
     */
    protected void ackProcessing(final GenericMessageDto<T> dto,
            final boolean ackOk, final String errorMessage) {
        boolean stopping = appStatus.getStatus().isStopping();

        // Ack
        if (ackOk) {
            ackPositively(stopping, dto);
        } else {
            ackNegatively(stopping, dto, errorMessage);
        }

        // Check status
        //TODO remove
        LOGGER.info(
                "[MONITOR] [step 3] [productName {}] Checking status application",
                getProductName(dto));
        if (appStatus.getStatus().isStopping()) {
            try {
                mqiStatusService.stop();
            } catch (AbstractCodedException ace) {
                LOGGER.error(
                        "[MONITOR] [step 3] [productName {}] [code {}] {} ",
                        getProductName(dto), ace.getCode().getCode(),
                        ace.getLogMessage());
            }
            System.exit(0);
        } else if (appStatus.getStatus().isFatalError()) {
            System.exit(-1);
        } else {
            appStatus.setWaiting();
        }
    }

    /**
     * @param dto
     * @param errorMessage
     */
    protected void ackNegatively(final boolean stop,
            final GenericMessageDto<T> dto, final String errorMessage) {
        LOGGER.error(
                "[REPORT] [MONITOR] [step 3] [s1pdgsTask {}] [subTask Consume] [productName {}] [STOP K0] Acknowledging negatively: {}",
                getTaskForFunctionalLog(), getProductName(dto), errorMessage);
        LOGGER.error(errorMessage);
        appStatus.setError("NEXT_MESSAGE");
        try {
            mqiService.ack(new AckMessageDto(dto.getIdentifier(), Ack.ERROR,
                    errorMessage, stop));
        } catch (AbstractCodedException ace) {
            LOGGER.error("[MONITOR] [step 3] [productName {}] [code {}] {}",
                    getProductName(dto), ace.getCode().getCode(),
                    ace.getLogMessage());
        }
    }

    protected void ackPositively(final boolean stop,
            final GenericMessageDto<T> dto) {
        
        // Log for functional monitoring
        LOGGER.info(
                "[REPORT] [MONITOR] [step 3] [s1pdgsTask {}] [subTask Consume] [productName {}] [STOP OK] Acknowledging positively",
                getTaskForFunctionalLog(), getProductName(dto));
        try {
            mqiService.ack(
                    new AckMessageDto(dto.getIdentifier(), Ack.OK, null, stop));
        } catch (AbstractCodedException ace) {
            LOGGER.error("[MONITOR] [step 3] [productName {}] [code {}] {}",
                    getProductName(dto), ace.getCode().getCode(),
                    ace.getLogMessage());
            appStatus.setError("NEXT_MESSAGE");
        }
    }

    protected abstract String getProductName(final GenericMessageDto<T> dto);
    
    protected abstract String getTaskForFunctionalLog();

}
