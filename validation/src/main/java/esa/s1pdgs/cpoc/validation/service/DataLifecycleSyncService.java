package esa.s1pdgs.cpoc.validation.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.datalifecycle.client.DataLifecycleClientUtil;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepositoryException;
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

		for (ProductFamily family : appProperties.getFamilies().keySet()) {

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
			;
			reporting.error(new ReportingMessage(endSyncMsg));
			LOG.warn(endSyncMsg);
		}

		return stats;
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

}
