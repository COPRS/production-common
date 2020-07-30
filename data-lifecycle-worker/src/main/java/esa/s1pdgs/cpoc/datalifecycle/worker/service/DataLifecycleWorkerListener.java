package esa.s1pdgs.cpoc.datalifecycle.worker.service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.datalifecycle.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.datalifecycle.worker.es.ElasticsearchDAO;
import esa.s1pdgs.cpoc.datalifecycle.worker.report.EvictionReportingOutput;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.model.queue.EvictionManagementJob;
import esa.s1pdgs.cpoc.mqi.model.queue.NullMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingOutput;
import esa.s1pdgs.cpoc.report.ReportingUtils;

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
    public final MqiMessageEventHandler onMessage(final GenericMessageDto<EvictionManagementJob> message) throws Exception {
        final EvictionManagementJob job = message.getBody();
        LOG.info("set/update eviction time {} for family {} key {}", job.getEvictionDate(), job.getProductFamily(), job.getKeyObjectStorage());
        
		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.predecessor(job.getUid())
				.newReporting("DataLifecycleWorker");
		
		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(job.getProductFamily(), job.getKeyObjectStorage()),
				new ReportingMessage("Updating eviction time for %s", job.getKeyObjectStorage())
		);  
		
		return new MqiMessageEventHandler.Builder<NullMessage>(ProductCategory.UNDEFINED)
				.onError(e -> 	reporting.error(
						new ReportingMessage(
								"Error updating eviction time for %s: %s", 
								job.getKeyObjectStorage(), 
								LogUtils.toString(e)
						)
				))
				.publishMessageProducer(() -> {
					updateRetentionInObs(job);
					final ReportingOutput out = updateRetentionInEs(job);
					reporting.end(
							out,
							new ReportingMessage("Updated eviction time for %s", job.getKeyObjectStorage())
					);
					return Collections.emptyList();
				})
				.newResult();
    }
    
    @Override
    public final void onTerminalError(final GenericMessageDto<EvictionManagementJob> message, final Exception error) {
        LOG.error(error);
        errorRepoAppender.send(new FailedProcessingDto(processConfiguration.getHostname(), new Date(), error.getMessage(), message));
    }

    private final ReportingOutput updateRetentionInEs(final EvictionManagementJob job) throws ObsServiceException, IOException {	
    	// TODO not sure if this will actually return the desired date as expiration time has just been updated a couple of calls
    	// before and not clear whether this changes the last modified date.
    	final LocalDateTime lastModifiedInObs = lastModifiedInObsFor(job).atOffset(ZoneOffset.UTC).toLocalDateTime();
    	final LocalDateTime evictionTimeOfJob = job.getEvictionDate().toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime();
    	
    	final String lastModified = DateUtils.formatToMetadataDateTimeFormat(lastModifiedInObs);
    	final String evictionDate = DateUtils.formatToMetadataDateTimeFormat(evictionTimeOfJob);
    	LOG.debug("Updating ES 'lastModified'='{}' and 'evictionDate'='{}' for product {}", lastModified, evictionDate, job.getKeyObjectStorage());

		final JSONObject expirationMetadata = new JSONObject();
		expirationMetadata.put("obsKey", job.getKeyObjectStorage());
		expirationMetadata.put("productFamily", job.getProductFamily());
		expirationMetadata.put("lastModified", lastModified);
		expirationMetadata.put("evictionDate", evictionDate);
		expirationMetadata.put("isUnlimited", job.isUnlimited());
		
		elasticSearchDAO.index(
		        new IndexRequest("data-lifecycle", "metadata", job.getKeyObjectStorage()).source(expirationMetadata.toString(), XContentType.JSON)
		);
		return new EvictionReportingOutput(Date.from(evictionTimeOfJob.toInstant(ZoneOffset.UTC)));
    }
    
    private final void updateRetentionInObs(final EvictionManagementJob job) throws ObsServiceException {
        obsClient.setExpirationTime(new ObsObject(job.getProductFamily(), job.getKeyObjectStorage()), job.getEvictionDate().toInstant());
    }

    private final Instant lastModifiedInObsFor(final EvictionManagementJob job) throws ObsServiceException {
        return obsClient.getMetadata(new ObsObject(job.getProductFamily(), job.getKeyObjectStorage())).getLastModified();
    }
}
