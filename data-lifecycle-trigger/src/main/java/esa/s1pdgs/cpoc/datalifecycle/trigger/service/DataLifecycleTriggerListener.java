package esa.s1pdgs.cpoc.datalifecycle.trigger.service;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.datalifecycle.trigger.config.DataLifecycleTriggerConfigurationProperties.RetentionPolicy;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.persistence.DataLifecycleMetadataRepositoryException;
import esa.s1pdgs.cpoc.datalifecycle.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.EvictionManagementJob;
import esa.s1pdgs.cpoc.mqi.model.queue.NullMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class DataLifecycleTriggerListener<E extends AbstractMessage> implements MqiListener<E> {
	private static final Logger LOG = LogManager.getLogger(DataLifecycleTriggerListener.class);

	private final ErrorRepoAppender errorRepoAppender;
	private final ProcessConfiguration processConfig;
	private final List<RetentionPolicy> retentionPolicies;
	private final DataLifecycleMetadataRepository metadataRepo;
	private final Pattern persistentInUncompressedStoragePattern;
	private final Pattern persistentInCompressedStoragePattern;
	private final Pattern availableInLtaPattern;

	public DataLifecycleTriggerListener(
			final ErrorRepoAppender errorRepoAppender,
			final ProcessConfiguration processConfig, 
			final List<RetentionPolicy> retentionPolicies,
			final DataLifecycleMetadataRepository metadataRepo,
			final String patternPersistentInUncompressedStorage,
			final String patternPersistentInCompressedStorage,
			final String patternAvailableInLta
	) {
		this.errorRepoAppender = errorRepoAppender;
		this.processConfig = processConfig;
		this.retentionPolicies = retentionPolicies;
		this.metadataRepo = metadataRepo;
		this.persistentInUncompressedStoragePattern = null != patternPersistentInUncompressedStorage
				? Pattern.compile(patternPersistentInUncompressedStorage)
				: null;
		this.persistentInCompressedStoragePattern = null != patternPersistentInCompressedStorage
				? Pattern.compile(patternPersistentInCompressedStorage)
				: null;
		this.availableInLtaPattern = null != patternAvailableInLta ? Pattern.compile(patternAvailableInLta) : null;
	}
	
	// --------------------------------------------------------------------------

	@Override
	public MqiMessageEventHandler onMessage(final GenericMessageDto<E> inputMessage) throws Exception {
		final E inputEvent = inputMessage.getBody();

		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.predecessor(inputEvent.getUid())
				.newReporting("DataLifecycleTrigger");

		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(inputEvent.getProductFamily(), inputEvent.getKeyObjectStorage()),
				new ReportingMessage("Handling event for %s", inputEvent.getKeyObjectStorage()));
		
		this.updateMetadata(inputMessage);
		
		//
		return new MqiMessageEventHandler.Builder<NullMessage>(ProductCategory.EVICTION_MANAGEMENT_JOBS)
				.onSuccess(res -> reporting.end(new ReportingMessage("End handling event for %s", inputEvent.getKeyObjectStorage())))
				.onError(e -> reporting.error(new ReportingMessage(
						"Error on handling event for %s: %s",
						inputEvent.getKeyObjectStorage(),
						LogUtils.toString(e))))
				.publishMessageProducer(() -> {
					return new MqiPublishingJob<NullMessage>(Collections.emptyList());
				}).newResult();
		// the data lifecycle trigger was reactivated in course of S1PRO-1869, but not so the worker.
		// therefore, we prevent the trigger from firing any messages for now
		// to reactivate the previous bevhaviour in the trigger, remove the return block above and uncomment the return block below
		
//		return new MqiMessageEventHandler.Builder<EvictionManagementJob>(ProductCategory.EVICTION_MANAGEMENT_JOBS)
//				.onSuccess(res -> reporting.end(new ReportingMessage("End handling event for %s", inputEvent.getKeyObjectStorage())))
//				.onError(e -> reporting.error(new ReportingMessage(
//						"Error on handling event for %s: %s", 
//						inputEvent.getKeyObjectStorage(),
//						LogUtils.toString(e)
//				)))
//				.publishMessageProducer(() ->{
//					final EvictionManagementJob evictionManagementJob = toEvictionManagementJob(
//							inputEvent, 
//							retentionPolicies,
//							reporting.getUid()
//					);
//
//					final GenericPublicationMessageDto<EvictionManagementJob> outputMessage = new GenericPublicationMessageDto<EvictionManagementJob>(
//							inputMessage.getId(), 
//							inputEvent.getProductFamily(), 
//							evictionManagementJob
//					);
//					return new MqiPublishingJob<EvictionManagementJob>(Collections.singletonList(outputMessage));
//				})
//				.newResult();
	}
	
	@Override
	public final void onTerminalError(final GenericMessageDto<E> message, final Exception error) {
		LOG.error(error);
		errorRepoAppender.send(new FailedProcessingDto(
				processConfig.getHostname(), 
				new Date(), 
				error.getMessage(),
				message
		));
	}
	
	// --------------------------------------------------------------------------
	
	/* Creating and updating data lifecycle metadata */
	private void updateMetadata(final GenericMessageDto<E> inputMessage) {
		final E inputEvent = inputMessage.getBody();
		final String obsKey = inputEvent.getKeyObjectStorage();
		
		final String fileName = this.getFileName(obsKey);
		final String productName = this.getProductName(obsKey);
		final boolean isCompressedStorage = inputEvent.getProductFamily().isCompressed();
		
		final Optional<DataLifecycleMetadata> oExistingMetadata;
		try {
			oExistingMetadata = this.metadataRepo.findByProductName(productName);
		} catch (DataLifecycleMetadataRepositoryException e) {
			LOG.error("error searching data lifecycle metadata by product name: " + LogUtils.toString(e), e);
			return;
		}
		
		final Date evictionDate = this.calculateEvictionDate(this.retentionPolicies, inputEvent.getCreationDate(),
				inputEvent.getProductFamily(), fileName);
		LocalDateTime evictionDateTime = null;
		if (null != evictionDate) {
			evictionDateTime = LocalDateTime.ofInstant(evictionDate.toInstant(), ZoneId.of("UTC"));
		}
		
		final DataLifecycleMetadata metadata = oExistingMetadata.orElse(new DataLifecycleMetadata() );
		metadata.setProductName(productName);
		
		if (isCompressedStorage) {
			metadata.setEvictionDateInCompressedStorage(evictionDateTime);
			metadata.setPathInCompressedStorage(obsKey);
			metadata.setPersistentInCompressedStorage(this.isPersistentInCompressedStorage(obsKey));
			metadata.setProductFamilyInCompressedStorage(inputEvent.getProductFamily());
		} else {
			metadata.setEvictionDateInUncompressedStorage(evictionDateTime);
			metadata.setPathInUncompressedStorage(obsKey);
			metadata.setPersistentInUncompressedStorage(this.isPersistentInUncompressedStorage(obsKey));
			metadata.setProductFamilyInUncompressedStorage(inputEvent.getProductFamily());
		}
		
		metadata.setAvailableInLta(this.isAvailableInLta(obsKey));
		
		try {
			this.metadataRepo.save(metadata);
		} catch (DataLifecycleMetadataRepositoryException e) {
			LOG.error("error saving data lifecycle metadata for " + inputEvent.getProductFamily().name() + ": "
					+ fileName + ": " + LogUtils.toString(e), e);
		}
	}

	final Date calculateEvictionDate(
			final List<RetentionPolicy> retentionPolicies, 
			final Date creationDate, 
			final ProductFamily productFamily,
			final String fileName
	) {
		for (final RetentionPolicy r : retentionPolicies) {

			if (r.getProductFamily().equals(productFamily.name()) && Pattern.matches(r.getFilePattern(), fileName)) {
				if (r.getRetentionTimeDays() > 0) {
					LOG.info("retention time is {} days for file: {}", r.getRetentionTimeDays(), fileName);
					return Date.from(creationDate.toInstant().plus(Period.ofDays(r.getRetentionTimeDays())));
				} else {
					LOG.info("retention time is unlimited for file: {}", fileName);
					return null;
				}
			}
		}
		LOG.warn("no retention time found for file: {}", fileName);
		return null;
	}
	
	String getFileName(final String obsKey) {
		return FilenameUtils.getName(obsKey);
	}
	
	String getProductName(final String obsKey) {
		if (FilenameUtils.getExtension(obsKey).equalsIgnoreCase("ZIP")) {
			return FilenameUtils.getBaseName(obsKey);
		}else {
			return FilenameUtils.getName(obsKey);
		}
	}
	
	boolean isAvailableInLta(final String obsKey) {
		return (null != this.availableInLtaPattern) && (null != obsKey)
				&& this.availableInLtaPattern.matcher(obsKey).matches();
	}
	
	boolean isPersistentInUncompressedStorage(final String obsKey) {
		return (null != this.persistentInUncompressedStoragePattern) && (null != obsKey)
				&& this.persistentInUncompressedStoragePattern.matcher(obsKey).matches();
	}
	
	boolean isPersistentInCompressedStorage(final String obsKey) {
		return (null != this.persistentInCompressedStoragePattern) && (null != obsKey)
				&& this.persistentInCompressedStoragePattern.matcher(obsKey).matches();
	}
	
}
