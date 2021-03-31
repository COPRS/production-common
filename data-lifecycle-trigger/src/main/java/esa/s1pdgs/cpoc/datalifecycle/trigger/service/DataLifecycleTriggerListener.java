package esa.s1pdgs.cpoc.datalifecycle.trigger.service;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.common.utils.StringUtil;
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

	private static final List<Class<? extends AbstractMessage>> UPDATE_INSERTIONTIME_ON = Arrays.asList(
			ProductCategory.INGESTION_EVENT.getDtoClass(), //
			ProductCategory.COMPRESSED_PRODUCTS.getDtoClass(), //
			ProductCategory.PRODUCTION_EVENT.getDtoClass(), //
			ProductCategory.LTA_DOWNLOAD_EVENT.getDtoClass() //
	);

	private final ErrorRepoAppender errorRepoAppender;
	private final ProcessConfiguration processConfig;
	private final List<RetentionPolicy> retentionPolicies;
	private final Map<ProductFamily, Integer> shortingEvictionTimeAfterCompression;
	private final DataLifecycleMetadataRepository metadataRepo;
	private final Pattern persistentInUncompressedStoragePattern;
	private final Pattern persistentInCompressedStoragePattern;
	private final Pattern availableInLtaPattern;

	public DataLifecycleTriggerListener(
			final ErrorRepoAppender errorRepoAppender,
			final ProcessConfiguration processConfig, 
			final List<RetentionPolicy> retentionPolicies,
			final Map<ProductFamily, Integer> shortingEvictionTimeAfterCompression,
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
		this.shortingEvictionTimeAfterCompression = null != shortingEvictionTimeAfterCompression ? shortingEvictionTimeAfterCompression
				: Collections.emptyMap();

		// validate eviction time shortening configuration
		final Iterator<Map.Entry<ProductFamily, Integer>> iter = this.shortingEvictionTimeAfterCompression.entrySet().iterator();
		while (iter.hasNext()) {
			final Map.Entry<ProductFamily, Integer> entry = iter.next();
			final ProductFamily productFamily = entry.getKey();
			final Integer evictTimeOffset = entry.getValue();

			if (null == productFamily || !productFamily.isCompressed()) {
				LOG.warn(String.format("eviction time shortening configuration for %s is invalid and will be ignored: only compressed product families allowed",
						productFamily));
				iter.remove();
			} else if (null == evictTimeOffset || evictTimeOffset < 0) {
				LOG.warn(String.format(
						"eviction time shortening configuration for %s is invalid (offset value: %s) and will be ignored: only values > 0 allowed",
						productFamily, evictTimeOffset));
				iter.remove();
			}
		}
	}
	
	// --------------------------------------------------------------------------

	@Override
	public MqiMessageEventHandler onMessage(final GenericMessageDto<E> inputMessage) throws Exception {
		LOG.debug("Starting data lifecycle management, got message: {}", inputMessage);
		final E inputEvent = inputMessage.getBody();

		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.predecessor(inputEvent.getUid())
				.newReporting("DataLifecycleTrigger");

		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(inputEvent.getProductFamily(), inputEvent.getKeyObjectStorage()),
				new ReportingMessage("Handling event for %s", inputEvent.getKeyObjectStorage()));

		return new MqiMessageEventHandler.Builder<NullMessage>(ProductCategory.of(inputEvent.getProductFamily()))
				.onSuccess(res -> reporting.end(new ReportingMessage("End handling event for %s", inputEvent.getKeyObjectStorage())))
				.onError(e -> reporting.error(new ReportingMessage("Error handling event for %s: on %s -> %s", inputEvent.getKeyObjectStorage(),
						inputEvent.getClass().getSimpleName(), LogUtils.toString(e))))
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
		final ProductFamily productFamily = inputEvent.getProductFamily();
		final boolean isCompressedStorage = productFamily.isCompressed();
		final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));

		final Optional<DataLifecycleMetadata> oExistingMetadata;
		try {
			oExistingMetadata = this.metadataRepo.findByProductName(productName);
		} catch (DataLifecycleMetadataRepositoryException e) {
			LOG.error("error searching data lifecycle metadata by product name: " + LogUtils.toString(e), e);
			throw e;
		}

		final Date evictionDate = this.calculateEvictionDate(this.retentionPolicies, inputEvent.getCreationDate(), productFamily, fileName);
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
			metadata.setProductFamilyInCompressedStorage(productFamily);

			if (needsInsertionTimeUpdate(inputEvent)) {
				metadata.setLastInsertionInCompressedStorage(now);
			}

			if (needsEvictionTimeShorteningInUncompressedStorage(inputEvent)) {
				final Integer evictionTimeOffsetInHours = this.shortingEvictionTimeAfterCompression.get(productFamily);
				final LocalDateTime shortenedEvictionDate = now.plusHours(evictionTimeOffsetInHours);
				final LocalDateTime evictionDateInUncompressedStorage = metadata.getEvictionDateInUncompressedStorage();

				if (StringUtil.isBlank(metadata.getPathInUncompressedStorage())) {
					LOG.debug(String.format("skip shortening eviction date in uncompressed storage after compression: no path in uncompressed storage for %s",
							productName));
				} else if (null == evictionDateInUncompressedStorage || evictionDateInUncompressedStorage.plusYears(1000).isBefore(now)) {
					LOG.debug(String.format(
							"skip shortening eviction date in uncompressed storage after compression: %s is freezed in uncompressed storage, eviction date %s",
							productName,
							(null != evictionDateInUncompressedStorage ? DateUtils.formatToMetadataDateTimeFormat(evictionDateInUncompressedStorage)
									: "null")));
				} else if (evictionDateInUncompressedStorage.isBefore(shortenedEvictionDate)) {
					LOG.debug(String.format(
							"skip shortening eviction date in uncompressed storage after compression: eviction date of %s in uncompressed storage is already shorter (%s < %s)",
							productName,
							(null != evictionDateInUncompressedStorage ? DateUtils.formatToMetadataDateTimeFormat(evictionDateInUncompressedStorage) : "null"),
							DateUtils.formatToMetadataDateTimeFormat(shortenedEvictionDate)));
				} else {
					metadata.setEvictionDateInUncompressedStorage(shortenedEvictionDate);
					LOG.info("shortening eviction date in uncompressed storage after compression of %s to %s", productName, shortenedEvictionDate);
				}
			}

			LOG.debug(String.format("%s lifecycle metadata with information for compressed storage: %s",
					(oExistingMetadata.isPresent() ? "updating" : "creating"), metadata));
		} else {
			metadata.setEvictionDateInUncompressedStorage(evictionDateTime);
			metadata.setPathInUncompressedStorage(obsKey);
			metadata.setPersistentInUncompressedStorage(this.isPersistentInUncompressedStorage(obsKey));
			metadata.setProductFamilyInUncompressedStorage(productFamily);

			if (needsInsertionTimeUpdate(inputEvent)) {
				metadata.setLastInsertionInUncompressedStorage(now);
			}

			LOG.debug(String.format("%s lifecycle metadata with information for uncompressed storage: %s",
					(oExistingMetadata.isPresent() ? "updating" : "creating"), metadata));
		}
		
		metadata.setAvailableInLta(this.isAvailableInLta(obsKey));
		
		try {
			this.metadataRepo.save(metadata);
		} catch (DataLifecycleMetadataRepositoryException e) {
			LOG.error("error saving data lifecycle metadata for " + productFamily.name() + ": "
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

	private static <E extends AbstractMessage> boolean needsInsertionTimeUpdate(final E event) {
		final Class<? extends AbstractMessage> eventClass = event.getClass();

		for (final Class<? extends AbstractMessage> updateClazz : UPDATE_INSERTIONTIME_ON) {
			if (updateClazz.isInstance(eventClass)) {
				return true;
			}
		}

		return false;
	}

	private static <E extends AbstractMessage> boolean needsEvictionTimeShorteningInUncompressedStorage(final E event) {
		// when compression event + compressed product family
		return ProductCategory.COMPRESSED_PRODUCTS.getDtoClass().isInstance(event.getClass()) && event.getProductFamily().isCompressed();
	}

}
