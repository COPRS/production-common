package esa.s1pdgs.cpoc.validation.service;

import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.LAST_INSERTION_IN_COMPRESSED_STORAGE;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.LAST_INSERTION_IN_UNCOMPRESSED_STORAGE;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.LAST_MODIFIED;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter.DataLifecycleRangeValueFilter.Operator.GE;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter.DataLifecycleRangeValueFilter.Operator.LE;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.datalifecycle.client.DataLifecycleClientUtil;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleSortTerm;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleSortTerm.DataLifecycleSortOrder;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.RetentionPolicy;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter.DataLifecycleDateTimeFilter;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter.DataLifecycleQueryFilter;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepositoryException;
import esa.s1pdgs.cpoc.datalifecycle.client.error.DataLifecycleTriggerInternalServerErrorException;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;
import esa.s1pdgs.cpoc.validation.config.ApplicationProperties;
import esa.s1pdgs.cpoc.validation.config.DataLifecycleSyncConfig;

@Service
public class DataLifecycleSyncService {

	private static final Logger LOG = LogManager.getLogger(DataLifecycleSyncService.class);

	private final ObsClient obsClient;

	private final DataLifecycleMetadataRepository lifecycleMetadataRepo;

	private final ApplicationProperties appProperties;

	private final DataLifecycleSyncConfig lifecycleSyncConfig;

	@Autowired
	public DataLifecycleSyncService(final ObsClient obsClient,
			final DataLifecycleMetadataRepository lifecycleMetadataRepo, final ApplicationProperties appProperties,
			final DataLifecycleSyncConfig lifecycleSyncConfig) {

		this.obsClient = obsClient;
		this.lifecycleMetadataRepo = lifecycleMetadataRepo;
		this.appProperties = appProperties;
		this.lifecycleSyncConfig = lifecycleSyncConfig;
	}

	/**
	 * Iterating OBS (buckets derived from product families with retention configuration) and adding missing product families
	 * and paths to existing data lifecycle metadata entries or if needed create new entries.
	 *
	 * @param startDate start of the time window to limit operation
	 * @param endDate   end time of the time window to limit operation
	 * @return statistics
	 */
	public DataLifecycleSyncStats syncDataLifecycleIndexFromOBS(final Date startDate, final Date endDate) {

		final Reporting reporting = ReportingUtils.newReportingBuilder().newReporting("SyncDataLifecycleIndexFromOBS");
		final String beginSyncMsg = "Start synchronising";
		reporting.begin(new ReportingMessage(beginSyncMsg));
		LOG.info(beginSyncMsg);

		final DataLifecycleSyncStats stats = new DataLifecycleSyncStats();

		for (final ProductFamily family : this.determineFamiliesToCheck()) {

			final Reporting reportFamily = reporting.newReporting("SyncFamily");
			final String beginSyncFamilyMsg = String.format("Start synchronising family %s", family);
			reportFamily.begin(new ReportingMessage(beginSyncFamilyMsg));
			LOG.info(beginSyncFamilyMsg);

			try {
				this.syncFamily(startDate, endDate, family, reportFamily, stats);
				final String endSyncFamilyMsg = String.format("End synchronising family %s", family);
				reportFamily.end(new ReportingMessage(endSyncFamilyMsg));
				LOG.info(endSyncFamilyMsg);

			} catch (final SdkClientException e) {
				stats.incrErrors();
				final String errorMsg = String.format("Error synchronising family %s, start: %s, end: %s: %s", family,
						startDate, endDate, LogUtils.toString(e));
				reportFamily.error(new ReportingMessage(errorMsg));
				LOG.warn(errorMsg);
				continue;
			}
		}

		if (stats.getErrors() == 0) {
			final String endSyncMsg = String.format("Synchronisation was successful, stats: %s", stats);
			reporting.end(new ReportingMessage(endSyncMsg));
			LOG.info(endSyncMsg);

		} else {
			final String endSyncMsg = String.format("End of synchronisation with errors, stats: %s", stats);
			reporting.error(new ReportingMessage(endSyncMsg));
			LOG.warn(endSyncMsg);
		}

		return stats;
	}

	Set<ProductFamily> determineFamiliesToCheck() {

		final Set<ProductFamily> families = EnumSet.noneOf(ProductFamily.class);

		for (final RetentionPolicy r : this.lifecycleSyncConfig.getRetentionPolicies()) {

			families.add(ProductFamily.valueOf(r.getProductFamily()));
		}

		return families;
	}

	void syncFamily(final Date startDate, final Date endDate, ProductFamily family, final Reporting reporting,
			final DataLifecycleSyncStats stats) throws SdkClientException {

		final Map<String, ObsObject> obsResults = this.obsClient.listInterval(family, startDate, endDate);

		for (final String key : this.extractRealKeysForDataLifecycle(obsResults.values(), family)) {

			final Reporting reportFile = reporting.newReporting("SyncFile");
			final String beginSyncFileMsg = String.format("Start synchronising file %s", key);
			reportFile.begin(new ReportingMessage(beginSyncFileMsg));
			LOG.info(beginSyncFileMsg);

			try {
				this.syncFile(family, key, stats);
				final String endSyncFileMsg = String.format("File synchronising was successful for %s", key);
				reportFile.end(new ReportingMessage(endSyncFileMsg));
				LOG.info(endSyncFileMsg);

			} catch (final DataLifecycleMetadataRepositoryException e) {
				stats.incrErrors();
				final String errorMsg = String.format("Error synchronising file: %s: %s", key, LogUtils.toString(e));
				reportFile.error(new ReportingMessage(errorMsg));
				LOG.warn(errorMsg);
				continue;
			}
		}

	}

	DataLifecycleMetadata syncFile(final ProductFamily family, final String key, final DataLifecycleSyncStats stats)
			throws DataLifecycleMetadataRepositoryException {
		final String productName = DataLifecycleClientUtil.getProductName(key);
		final String fileName = DataLifecycleClientUtil.getFileName(key);
		final Optional<DataLifecycleMetadata> existingMetadata = this.lifecycleMetadataRepo.findByProductName(productName);

		boolean changed = false;
		DataLifecycleMetadata metadata = null;

		if (existingMetadata.isPresent()) {

			metadata = existingMetadata.get();

			if (family.isCompressed()) {
				if (metadata.getProductFamilyInCompressedStorage() == null) {
					metadata.setProductFamilyInCompressedStorage(family);
					LOG.debug("Setting family {} on {} for compressed storage", family, key);
					stats.incrFamilyUpdated();
					changed = true;
				}
				if (metadata.getPathInCompressedStorage() == null) {
					metadata.setPathInCompressedStorage(key);
					LOG.debug("Setting path {} for compressed storage");
					stats.incrPathUpdated();
					changed = true;
				}
			} else {
				if (metadata.getProductFamilyInUncompressedStorage() == null) {
					metadata.setProductFamilyInUncompressedStorage(family);
					LOG.debug("Setting family {} on {} for uncompressed storage", family, key);
					stats.incrFamilyUpdated();
					changed = true;
				}
				if (metadata.getPathInUncompressedStorage() == null) {
					metadata.setPathInUncompressedStorage(key);
					LOG.debug("Setting path {} for uncompressed storage");
					stats.incrPathUpdated();
					changed = true;
				}
			}

			if (changed) {
				LOG.info("Updating metadata: {}", metadata);
			} else {
				stats.incrUnchanged();
				LOG.debug("Metadata for file {} is complete", key);
			}

		} else {
			metadata = new DataLifecycleMetadata();
			metadata.setProductName(productName);

			final Date now = new Date();
			final LocalDateTime insertionDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
			final Date calculatedEvictionDate = DataLifecycleClientUtil
					.calculateEvictionDate(this.lifecycleSyncConfig.getRetentionPolicies(), now, family, fileName);
			final LocalDateTime evictionDate = (calculatedEvictionDate != null)
					? LocalDateTime.ofInstant(calculatedEvictionDate.toInstant(), ZoneId.systemDefault())
							: null;

					if (family.isCompressed()) {
						metadata.setProductFamilyInCompressedStorage(family);
						metadata.setPathInCompressedStorage(key);
						metadata.setEvictionDateInCompressedStorage(evictionDate);
						metadata.setLastInsertionInCompressedStorage(insertionDate);
					} else {
						metadata.setProductFamilyInUncompressedStorage(family);
						metadata.setPathInUncompressedStorage(key);
						metadata.setEvictionDateInUncompressedStorage(evictionDate);
						metadata.setLastInsertionInUncompressedStorage(insertionDate);
					}
					LOG.info("Adding new metadata: {}", metadata.toString());
					stats.incrNewCreated();
					changed = true;
		}

		if (changed) {
			this.lifecycleMetadataRepo.save(metadata);
		}
		return metadata;
	}

	Set<String> extractRealKeysForDataLifecycle(final Collection<ObsObject> obsResults, final ProductFamily family) {
		final Set<String> realProducts = new HashSet<>();
		for (final ObsObject obsResult : obsResults) {
			final String key = obsResult.getKey();
			final int index = key.indexOf("/");
			String realKey = null;

			if (family == ProductFamily.EDRS_SESSION) {
				realKey = key;
			} else if (index != -1) {
				realKey = key.substring(0, index);
			} else {
				realKey = key;
			}
			LOG.trace("key is {}", realKey);
			realProducts.add(realKey);
		}
		return realProducts;
	}

	/**
	 * Iterating the data lifecycle index and checking whether the files still exist in OBS and if not removing
	 * the path value from the data lifecycle metadata, indicating the file does not exist anymore.
	 *
	 * @param startDate start of the time window to limit operation
	 * @param endDate   end time of the time window to limit operation
	 * @throws DataLifecycleTriggerInternalServerErrorException on internal server error
	 */
	public void syncDataLifecycleIndexWithOBS(final LocalDateTime startDate, final LocalDateTime endDate)
			throws DataLifecycleTriggerInternalServerErrorException {

		final Reporting reporting = ReportingUtils.newReportingBuilder().newReporting("SyncDataLifecycleIndexWithOBS");
		final String beginSyncMsg = "Start synchronising";
		reporting.begin(new ReportingMessage(beginSyncMsg));
		LOG.info(beginSyncMsg);

		final int pageSize = 100;

		// iterate data lifecycle index for uncompressed files
		final List<DataLifecycleQueryFilter> filtersForUncompressed = new ArrayList<>();
		filtersForUncompressed.add(new DataLifecycleDateTimeFilter(LAST_INSERTION_IN_UNCOMPRESSED_STORAGE, GE, startDate));
		filtersForUncompressed.add(new DataLifecycleDateTimeFilter(LAST_INSERTION_IN_UNCOMPRESSED_STORAGE, LE, endDate));

		final DataLifecycleSyncStats statsUncompressed = new DataLifecycleSyncStats();
		final Reporting reportUncompressed = reporting.newReporting("SyncWithUncompressedStorage");
		try {
			final String beginSyncUncompressedMsg = "Start synchronising data lifecycle index with uncompressed storage";
			reportUncompressed.begin(new ReportingMessage(beginSyncUncompressedMsg));
			LOG.info(beginSyncUncompressedMsg);

			this.syncMetadataWithOBS(pageSize, false, filtersForUncompressed, statsUncompressed, reportUncompressed);

			final String endSyncUncompressedMsg = "End synchronising data lifecycle index with uncompressed storage";
			reportUncompressed.end(new ReportingMessage(endSyncUncompressedMsg));
			LOG.info(endSyncUncompressedMsg);
		} catch (final DataLifecycleTriggerInternalServerErrorException e) {
			statsUncompressed.incrErrors();
			final String errorMsg = String.format("Error synchronising data lifecycle index with uncompressed storage, start: %s, end: %s: %s", startDate,
					endDate, LogUtils.toString(e));
			reportUncompressed.error(new ReportingMessage(errorMsg));
			LOG.warn(errorMsg);
		}

		final long totalUncompressed = statsUncompressed.getUnchanged() + statsUncompressed.getPathUpdated() + statsUncompressed.getIgnored();
		LOG.info(String.format(
				"removed paths in uncompressed storage from %d data lifecycle metadata entries of a total of %d with insertion times between %s and %s",
				statsUncompressed.getPathUpdated(), totalUncompressed, DateUtils.formatToMetadataDateTimeFormat(startDate),
				DateUtils.formatToMetadataDateTimeFormat(endDate)));

		// iterate data lifecycle index for compressed files
		final List<DataLifecycleQueryFilter> filtersForCompressed = new ArrayList<>();
		filtersForCompressed.add(new DataLifecycleDateTimeFilter(LAST_INSERTION_IN_COMPRESSED_STORAGE, GE, startDate));
		filtersForCompressed.add(new DataLifecycleDateTimeFilter(LAST_INSERTION_IN_COMPRESSED_STORAGE, LE, endDate));

		final DataLifecycleSyncStats statsCompressed = new DataLifecycleSyncStats();
		final Reporting reportCompressed = reporting.newReporting("SyncWithCompressedStorage");
		try {
			final String beginSyncCompressedMsg = "Start synchronising data lifecycle index with compressed storage";
			reportCompressed.begin(new ReportingMessage(beginSyncCompressedMsg));
			LOG.info(beginSyncCompressedMsg);

			this.syncMetadataWithOBS(pageSize, true, filtersForCompressed, statsCompressed, reportCompressed);

			final String endSyncCompressedMsg = "End synchronising data lifecycle index with compressed storage";
			reportCompressed.end(new ReportingMessage(endSyncCompressedMsg));
			LOG.info(endSyncCompressedMsg);
		} catch (final DataLifecycleTriggerInternalServerErrorException e) {
			statsCompressed.incrErrors();
			final String errorMsg = String.format("Error synchronising data lifecycle index with compressed storage, start: %s, end: %s: %s", startDate,
					endDate, LogUtils.toString(e));
			reportCompressed.error(new ReportingMessage(errorMsg));
			LOG.warn(errorMsg);
		}

		final long totalCompressed = statsCompressed.getUnchanged() + statsCompressed.getPathUpdated() + statsCompressed.getIgnored();
		LOG.info(String.format(
				"removed paths in compressed storage from %d data lifecycle metadata entries of a total of %d with insertion times between %s and %s",
				statsCompressed.getPathUpdated(), totalCompressed, DateUtils.formatToMetadataDateTimeFormat(startDate),
				DateUtils.formatToMetadataDateTimeFormat(endDate)));

		// summary reporting/logging
		if (statsUncompressed.getErrors() == 0 && statsCompressed.getErrors() == 0) {
			final String endSyncMsg = String.format("Synchronisation was successful, stats for uncompressed storage: %s, stats for compressed storage: %s",
					statsUncompressed, statsCompressed);
			reporting.end(new ReportingMessage(endSyncMsg));
			LOG.info(endSyncMsg);
		} else {
			final String endSyncMsg = String.format("End of synchronisation with errors, stats for uncompressed storage: %s, stats for compressed storage: %s",
					statsUncompressed, statsCompressed);
			reporting.error(new ReportingMessage(endSyncMsg));
			LOG.warn(endSyncMsg);
		}
	}

	/**
	 * Query data lifecycle index with the given filters using paging and sync the results of the query with OBS.
	 * This is separated for compressed and uncompressed storage, depending on value of inCompressedStorage.
	 */
	private void syncMetadataWithOBS(final int pageSize, final boolean inCompressedStorage, final List<DataLifecycleQueryFilter> filters,
			final DataLifecycleSyncStats stats, final Reporting reporting) throws DataLifecycleTriggerInternalServerErrorException {
		final DataLifecycleSortTerm sortTerm = new DataLifecycleSortTerm(LAST_MODIFIED, DataLifecycleSortOrder.ASCENDING);

		int offset = 0;
		List<DataLifecycleMetadata> productsToSync;
		do {
			// get result page
			productsToSync = CollectionUtil.nullToEmptyList(this.lifecycleMetadataRepo.findWithFilters(filters, Optional.of(pageSize),
					Optional.of(offset), Collections.singletonList(sortTerm)));

			final int page = (offset > 0 ? offset / pageSize : 0);
			LOG.debug(String.format(
					"found %s data lifecycle metadata entries for products in %s storage to sync with OBS (page: %d / page size: %d / offset: %d)",
					productsToSync.size(), inCompressedStorage ? "compressed" : "uncompressed", page, pageSize, offset));

			// sync metadata of page
			productsToSync.forEach(metadata -> this.syncMetadataWithOBS(metadata, inCompressedStorage, stats, reporting));

			// calculate offset for next page
			if (((long) offset + pageSize) > Integer.MAX_VALUE) {
				throw new DataLifecycleTriggerInternalServerErrorException("paging offset exceeds limit of " + Integer.MAX_VALUE);
			}
			offset += pageSize;
		} while (CollectionUtil.isNotEmpty(productsToSync));
	}

	/**
	 * Check whether file still exists in OBS and if not remove path from lifecycle metadata.
	 * This is separated for compressed and uncompressed storage, depending on value of inCompressedStorage.
	 */
	private void syncMetadataWithOBS(DataLifecycleMetadata metadata, final boolean inCompressedStorage, final DataLifecycleSyncStats stats,
			final Reporting reporting) {
		final String pathInStorage = inCompressedStorage ? metadata.getPathInCompressedStorage() : metadata.getPathInUncompressedStorage();
		final ProductFamily productFamily = inCompressedStorage ? metadata.getProductFamilyInCompressedStorage()
				: metadata.getProductFamilyInUncompressedStorage();
		final String comprsStr = inCompressedStorage ? "compressed" : "uncompressed";

		if (StringUtil.isBlank(pathInStorage)) {
			// this is perfectly fine, it just means the file was already deleted from storage (or was never created)
			LOG.debug(String.format("data lifecycle entry for %s has no path in %s storage: nothing to do", metadata.getProductName(), comprsStr));
			stats.incrUnchanged();
			return;
		}

		final Reporting reportFile = reporting.newReporting("SyncWith" + (inCompressedStorage ? "Compressed" : "Uncompressed") + "File");
		final String beginSyncWithFileMsg = String.format("Start synchronising data lifecycle index with %s file %s", comprsStr, pathInStorage);
		reportFile.begin(new ReportingMessage(beginSyncWithFileMsg));
		LOG.debug(beginSyncWithFileMsg);

		if (null == productFamily) {
			// now we do have a problem, because we have a path in storage but no product family, ignore, please do lifecycle sync from OBS first
			final String errSyncFileMsg = String.format("Error synchronising data lifecycle index with %s file %s: product family missing", comprsStr,
					pathInStorage);
			reportFile.error(new ReportingMessage(errSyncFileMsg));
			LOG.warn(errSyncFileMsg);
			stats.incrIgnored();
			return;
		}

		final ObsObject obsObject = new ObsObject(productFamily, pathInStorage);
		try {
			if (this.obsClient.exists(obsObject)) {
				final String endSyncFileMsg = String.format("End synchronising data lifecycle index with %s file %s: file exists in OBS, nothing to do",
						comprsStr, pathInStorage);
				reportFile.end(new ReportingMessage(endSyncFileMsg));
				LOG.debug(endSyncFileMsg);
				stats.incrUnchanged();
				return;
			} else {
				if (inCompressedStorage) {
					metadata.setPathInCompressedStorage(null);
				} else {
					metadata.setPathInUncompressedStorage(null);
				}
				this.lifecycleMetadataRepo.save(metadata);

				final String endSyncFileMsg = String.format(
						"End synchronising data lifecycle index with %s file %s: file does not exist in OBS anymore, removed path from data lifecycle index",
						comprsStr, pathInStorage);
				reportFile.end(new ReportingMessage(endSyncFileMsg));
				LOG.info(endSyncFileMsg);
				stats.incrPathUpdated();
				return;
			}
		} catch (final SdkClientException e) {
			final String errSyncFileMsg = String.format("Error synchronising data lifecycle index with %s file %s: %s", comprsStr, pathInStorage,
					LogUtils.toString(e));
			reportFile.error(new ReportingMessage(errSyncFileMsg));
			LOG.error(errSyncFileMsg);

			stats.incrIgnored();
			stats.incrErrors();
			return;
		} catch (final DataLifecycleMetadataRepositoryException e) {
			final String errSyncFileMsg = String.format("Error synchronising data lifecycle index with %s file %s: %s", comprsStr, pathInStorage,
					LogUtils.toString(e));
			reportFile.error(new ReportingMessage(errSyncFileMsg));
			LOG.error(errSyncFileMsg);

			stats.incrIgnored();
			stats.incrErrors();
			return;
		}
	}

}
