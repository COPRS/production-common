package esa.s1pdgs.cpoc.datalifecycle.worker.service;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.datalifecycle.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.model.queue.EvictionManagementJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;

public class DataLifecycleWorkerListener implements MqiListener<EvictionManagementJob> {

    private static final Logger LOG = LogManager.getLogger(DataLifecycleWorkerListener.class);

    private final ErrorRepoAppender errorRepoAppender;
    private final ProcessConfiguration processConfiguration;
    private final ObsClient obsClient;


    public DataLifecycleWorkerListener(ErrorRepoAppender errorRepoAppender, ProcessConfiguration processConfiguration, ObsClient obsClient) {
        this.errorRepoAppender = errorRepoAppender;
        this.processConfiguration = processConfiguration;
        this.obsClient = obsClient;
    }

    @Override
    public void onMessage(GenericMessageDto<EvictionManagementJob> message) throws ObsServiceException {
            setRetentionInObs(message.getBody());
            setRetentionInEs(message.getBody());
    }

    private void setRetentionInObs(EvictionManagementJob job) throws ObsServiceException {
        obsClient.setExpirationTime(new ObsObject(job.getProductFamily(), job.getKeyObjectStorage()), job.getEvictionDate().toInstant());
    }

    private void setRetentionInEs(EvictionManagementJob job) {
        //TODO
    }

    @Override
    public void onTerminalError(GenericMessageDto<EvictionManagementJob> message, Exception error) {
        LOG.error(error);
        errorRepoAppender.send(new FailedProcessingDto(processConfiguration.getHostname(), new Date(), error.getMessage(), message));
    }
}
