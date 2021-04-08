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

	public DataLifecycleSyncStats syncOBSwithDataLifecycleIndex(final Date startDate, final Date endDate) {

		final Reporting reporting = ReportingUtils.newReportingBuilder().newReporting("SyncOBSwithDataLifecycleIndex");
		final String beginSyncMsg = "Start synchronising";
		reporting.begin(new ReportingMessage(beginSyncMsg));
		LOG.info(beginSyncMsg);

		DataLifecycleSyncStats stats = new DataLifecycleSyncStats();
		
		for (ProductFamily family : determineFamiliesToCheck()) {

			Reporting reportFamily = reporting.newReporting("SyncFamily");
			final String beginSyncFamilyMsg = String.format("Start synchronising family %s", family);
			reportFamily.begin(new ReportingMessage(beginSyncFamilyMsg));
			LOG.info(beginSyncFamilyMsg);

			try {
				syncFamily(startDate, endDate, family, reporting, stats);
				final String endSyncFamilyMsg = String.format("End synchronising family %s", family);
				reportFamily.end(new ReportingMessage(endSyncFamilyMsg));
				LOG.info(endSyncFamilyMsg);

			} catch (SdkClientException e) {
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

		Set<ProductFamily> families = EnumSet.noneOf(ProductFamily.class);

		for (RetentionPolicy r : lifecycleSyncConfig.getRetentionPolicies()) {

			families.add(ProductFamily.valueOf(r.getProductFamily()));
		}

		return families;
	}

	void syncFamily(final Date startDate, final Date endDate, ProductFamily family, final Reporting reporting,
			final DataLifecycleSyncStats stats) throws SdkClientException {

		Map<String, ObsObject> obsResults = obsClient.listInterval(family, startDate, endDate);

		for (String key : extractRealKeysForDataLifecycle(obsResults.values(), family)) {

			Reporting reportFile = reporting.newReporting("SyncFile");
			final String beginSyncFileMsg = String.format("Start synchronising file %s", key);
			reportFile.begin(new ReportingMessage(beginSyncFileMsg));
			LOG.info(beginSyncFileMsg);

			try {
				syncFile(family, key, stats);
				final String endSyncFileMsg = String.format("File synchronising was successful for %s", key);
				reportFile.end(new ReportingMessage(endSyncFileMsg));
				LOG.info(endSyncFileMsg);

			} catch (DataLifecycleMetadataRepositoryException e) {
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
		final Optional<DataLifecycleMetadata> existingMetadata = lifecycleMetadataRepo.findByProductName(productName);

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

			Date now = new Date();
			LocalDateTime insertionDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
			Date calculatedEvictionDate = DataLifecycleClientUtil
					.calculateEvictionDate(lifecycleSyncConfig.getRetentionPolicies(), now, family, fileName);
			LocalDateTime evictionDate = (calculatedEvictionDate != null)
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
			lifecycleMetadataRepo.save(metadata);
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

	public DataLifecycleSyncStats syncDataLifecycleIndexWithOBS(LocalDateTime startDate, LocalDateTime endDate)
			throws DataLifecycleTriggerInternalServerErrorException {
		if (null == startDate) {
			startDate = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
			LOG.info(String.format("no start date provided for data lifecycle metadata synchronization with OBS, using %s",
					DateUtils.formatToMetadataDateTimeFormat(startDate)));
		}
		if (null == endDate) {
			endDate = LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999999999);
			LOG.info(String.format("no end date provided for data lifecycle metadata synchronization with OBS, using %s",
					DateUtils.formatToMetadataDateTimeFormat(endDate)));
		}

		final Reporting reporting = ReportingUtils.newReportingBuilder().newReporting("SyncDataLifecycleIndexFromOBS");
		final String beginSyncMsg = "Start synchronising";
		reporting.begin(new ReportingMessage(beginSyncMsg));
		LOG.info(beginSyncMsg);

		final DataLifecycleSyncStats stats = new DataLifecycleSyncStats();

		final int pageSize = 100;
		List<DataLifecycleMetadata> productsToSync;
		final DataLifecycleSortTerm sortTerm = new DataLifecycleSortTerm(LAST_MODIFIED, DataLifecycleSortOrder.ASCENDING);

		// iterate data lifecycle index for uncompressed files
		final ArrayList<DataLifecycleQueryFilter> filtersForUncompressed = new ArrayList<>();
		filtersForUncompressed.add(new DataLifecycleDateTimeFilter(LAST_INSERTION_IN_UNCOMPRESSED_STORAGE, GE, startDate));
		filtersForUncompressed.add(new DataLifecycleDateTimeFilter(LAST_INSERTION_IN_UNCOMPRESSED_STORAGE, LE, endDate));

		int offset = 0;
		long iterateCounterUncompressed = 0;
		long removedCounterUncompressed = 0;
		do {
			// get result page
			productsToSync = CollectionUtil.nullToEmptyList(this.lifecycleMetadataRepo.findWithFilters(filtersForUncompressed,
					Optional.of(pageSize), Optional.of(offset), Collections.singletonList(sortTerm)));

			iterateCounterUncompressed += productsToSync.size();
			final int page = (offset > 0 ? offset / pageSize : 0);
			LOG.debug(String.format(
					"found %s data lifecycle metadata entries for products in uncompressed storage to sync with OBS (page: %d / page size: %d / offset: %d)",
					productsToSync.size(), page, pageSize, offset));

			final int removedOnPage = 0;
			for (final DataLifecycleMetadata metadata : productsToSync) {
				//try
				// TODO: check obs + delete path if necessary + increment removedOnPage if necessary
				//catch
			}
			removedCounterUncompressed += removedOnPage;

			LOG.debug(String.format("removed path in uncompressed storage from %d of %d on page %d", removedOnPage, pageSize, page));

			// calculate offset for next page
			if (((long) offset + pageSize) > Integer.MAX_VALUE) {
				throw new DataLifecycleTriggerInternalServerErrorException("paging offset exceeds limit of " + Integer.MAX_VALUE);
			}
			offset += pageSize;
		} while (CollectionUtil.isNotEmpty(productsToSync));

		LOG.info(String.format(
				"removed paths in uncompressed storage from %d data lifecycle metadata entries of a total of %d with insertion times between %s and %s",
				removedCounterUncompressed, iterateCounterUncompressed, DateUtils.formatToMetadataDateTimeFormat(startDate),
				DateUtils.formatToMetadataDateTimeFormat(endDate)));

		// iterate data lifecycle index for compressed files
		final ArrayList<DataLifecycleQueryFilter> filtersForCompressed = new ArrayList<>();
		filtersForCompressed.add(new DataLifecycleDateTimeFilter(LAST_INSERTION_IN_COMPRESSED_STORAGE, GE, startDate));
		filtersForCompressed.add(new DataLifecycleDateTimeFilter(LAST_INSERTION_IN_COMPRESSED_STORAGE, LE, endDate));

		// TODO @MSc: impl iterate lifecycle index for compressed files (with paging) and search files in OBS, when not found, delete path from lifecycle index
		// (analog oben mit uncompressed)

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

}
