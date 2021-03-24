package esa.s1pdgs.cpoc.datalifecycle.trigger.service;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.datalifecycle.trigger.config.DataLifecycleTriggerConfigurationProperties.RetentionPolicy;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepositoryException;
import esa.s1pdgs.cpoc.datalifecycle.client.error.DataLifecycleTriggerInternalServerErrorException;
import esa.s1pdgs.cpoc.datalifecycle.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.MqiMessageEventHandler;
import esa.s1pdgs.cpoc.mqi.client.MqiPublishingJob;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.EvictionEvent;
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

		return new MqiMessageEventHandler.Builder<NullMessage>(ProductCategory.of(inputEvent.getProductFamily()))
				.onSuccess(res -> reporting.end(new ReportingMessage("End handling event for %s", inputEvent.getKeyObjectStorage())))
				.onError(e -> reporting.error(new ReportingMessage("Error handling %s for %s: %s", inputEvent.getClass().getSimpleName(),
						inputEvent.getKeyObjectStorage(), LogUtils.toString(e))))
				.publishMessageProducer(() -> {
					if (inputEvent instanceof EvictionEvent) {
						this.updateEvictedMetadata((EvictionEvent) inputEvent);
					} else {
						this.updateMetadata(inputMessage);
					}
					return new MqiPublishingJob<NullMessage>(Collections.emptyList());
				}).newResult();
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
	private void updateMetadata(final GenericMessageDto<E> inputMessage) throws DataLifecycleMetadataRepositoryException {
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
			throw e;
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

			LOG.debug(String.format("%s lifecycle metadata with information for compressed storage: %s",
					(oExistingMetadata.isPresent() ? "updating" : "creating"), metadata));
		} else {
			metadata.setEvictionDateInUncompressedStorage(evictionDateTime);
			metadata.setPathInUncompressedStorage(obsKey);
			metadata.setPersistentInUncompressedStorage(this.isPersistentInUncompressedStorage(obsKey));
			metadata.setProductFamilyInUncompressedStorage(inputEvent.getProductFamily());

			LOG.debug(String.format("%s lifecycle metadata with information for uncompressed storage: %s",
					(oExistingMetadata.isPresent() ? "updating" : "creating"), metadata));
		}
		
		metadata.setAvailableInLta(this.isAvailableInLta(obsKey));
		
		try {
			this.metadataRepo.save(metadata);
		} catch (DataLifecycleMetadataRepositoryException e) {
			LOG.error("error saving data lifecycle metadata for " + inputEvent.getProductFamily().name() + ": "
					+ fileName + ": " + LogUtils.toString(e), e);
			throw e;
		}
	}

	/* Removing storage path from data lifecycle metadata after eviction of product */
	private void updateEvictedMetadata(final EvictionEvent inputEvent) throws DataLifecycleTriggerInternalServerErrorException {
		final String obsKey = inputEvent.getKeyObjectStorage();
		final String productName = this.getProductName(obsKey);
		final boolean isCompressedStorage = inputEvent.getProductFamily().isCompressed();

		final Optional<DataLifecycleMetadata> oExistingMetadata;
		try {
			oExistingMetadata = this.metadataRepo.findByProductName(productName);
		} catch (final DataLifecycleMetadataRepositoryException e) {
			LOG.error("error updating lifecycle metadata due to eviction of " + productName + ": " + LogUtils.toString(e), e);
			throw e;
		}

		if (!oExistingMetadata.isPresent()) {
			LOG.error("error updating lifecycle metadata due to eviction of " + productName + ": no lifecycle metadata found");
			throw new DataLifecycleTriggerInternalServerErrorException(
					"error updating lifecycle metadata due to eviction of " + productName + ": no lifecycle metadata found");
		}

		final DataLifecycleMetadata metadata = oExistingMetadata.get();

		// erase storage path as it is no longer valid
		if (isCompressedStorage) {
			metadata.setPathInCompressedStorage(null);
			LOG.debug("erasing path in compressed storage from lifecycle metadata due to eviction of: " + productName);
		} else {
			metadata.setPathInUncompressedStorage(null);
			LOG.debug("erasing path in uncompressed storage from lifecycle metadata due to eviction of: " + productName);
		}

		try {
			this.metadataRepo.save(metadata);
		} catch (final DataLifecycleMetadataRepositoryException e) {
			LOG.error("error updating lifecycle metadata due to eviction of " + productName + ": " + LogUtils.toString(e), e);
			throw e;
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
