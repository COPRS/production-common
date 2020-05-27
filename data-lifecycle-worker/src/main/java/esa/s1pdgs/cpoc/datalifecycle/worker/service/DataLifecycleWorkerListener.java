package esa.s1pdgs.cpoc.datalifecycle.worker.service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.datalifecycle.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.datalifecycle.worker.es.ElasticsearchDAO;
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
    private final ElasticsearchDAO elasticSearchDAO;

    public DataLifecycleWorkerListener(
    		final ErrorRepoAppender errorRepoAppender, 
    		final ProcessConfiguration processConfiguration, 
    		final ObsClient obsClient, 
    		final ElasticsearchDAO elasticSearchDAO
    ) {
        this.errorRepoAppender = errorRepoAppender;
        this.processConfiguration = processConfiguration;
        this.obsClient = obsClient;
        this.elasticSearchDAO = elasticSearchDAO;
    }

    @Override
    public final void onMessage(final GenericMessageDto<EvictionManagementJob> message) throws ObsServiceException, IOException {
        final EvictionManagementJob job = message.getBody();
        LOG.info("set/update eviction time {} for family {} key {}", job.getEvictionDate(), job.getProductFamily(), job.getKeyObjectStorage());
        updateRetentionInObs(job);
        updateRetentionInEs(job);
    }
    
    @Override
    public final void onTerminalError(final GenericMessageDto<EvictionManagementJob> message, final Exception error) {
        LOG.error(error);
        errorRepoAppender.send(new FailedProcessingDto(processConfiguration.getHostname(), new Date(), error.getMessage(), message));
    }

    private void updateRetentionInEs(final EvictionManagementJob job) throws ObsServiceException, IOException {
    	final LocalDateTime lastModifiedInObs = lastModifiedInObsFor(job).atOffset(ZoneOffset.UTC).toLocalDateTime();
    	final LocalDateTime evictionTimeOfJob = job.getEvictionDate().toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime();
    	
    	final String lastModified = DateUtils.formatToMetadataDateTimeFormat(lastModifiedInObs);
    	final String evictionDate = DateUtils.formatToMetadataDateTimeFormat(evictionTimeOfJob);
    	
        final JSONObject expirationMetadata = new JSONObject();
        expirationMetadata.put("obsKey", job.getKeyObjectStorage());
        expirationMetadata.put("productFamily", job.getProductFamily());
        expirationMetadata.put("lastModified", lastModified);
        expirationMetadata.put("evictionDate",evictionDate);
        expirationMetadata.put("isUnlimited", job.isUnlimited());

        elasticSearchDAO.index(
                new IndexRequest("data-lifecycle", "metadata", job.getKeyObjectStorage()).source(expirationMetadata.toString(), XContentType.JSON)
        );
    }
    
    private final void updateRetentionInObs(final EvictionManagementJob job) throws ObsServiceException {
        obsClient.setExpirationTime(new ObsObject(job.getProductFamily(), job.getKeyObjectStorage()), job.getEvictionDate().toInstant());
    }

    private final Instant lastModifiedInObsFor(final EvictionManagementJob job) throws ObsServiceException {
        return obsClient.getMetadata(new ObsObject(job.getProductFamily(), job.getKeyObjectStorage())).getLastModified();
    }
}
