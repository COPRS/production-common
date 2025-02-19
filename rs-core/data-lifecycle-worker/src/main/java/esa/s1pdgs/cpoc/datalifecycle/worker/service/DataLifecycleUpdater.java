/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.datalifecycle.worker.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.datalifecycle.client.DataLifecycleClientUtil;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.RetentionPolicy;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepositoryException;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;

public class DataLifecycleUpdater {
	
	private static final Logger LOG = LogManager.getLogger(DataLifecycleUpdater.class);
	
	private static final List<Class<? extends AbstractMessage>> UPDATE_INSERTIONTIME_ON = Arrays.asList(
			ProductCategory.COMPRESSED_PRODUCTS.getDtoClass(),
			ProductCategory.CATALOG_EVENT.getDtoClass(),
			ProductCategory.LTA_DOWNLOAD_EVENT.getDtoClass()
	);
	
	private final Collection<RetentionPolicy> retentionPolicies;
	private final Map<ProductFamily, Integer> shortingEvictionTimeAfterCompression;
	private final DataLifecycleMetadataRepository metadataRepo;
	
	public DataLifecycleUpdater(
		final Collection<RetentionPolicy> retentionPolicies,
		final Map<ProductFamily, Integer> shortingEvictionTimeAfterCompression,
		final DataLifecycleMetadataRepository metadataRepo) {
		
		this.retentionPolicies = retentionPolicies;
		this.shortingEvictionTimeAfterCompression = null != shortingEvictionTimeAfterCompression ? shortingEvictionTimeAfterCompression
				: Collections.emptyMap();
		this.metadataRepo = metadataRepo;
		
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
		
		if (!this.shortingEvictionTimeAfterCompression.isEmpty()) {
			LOG.info("retention time update configuration found: " + this.shortingEvictionTimeAfterCompression);
		}
	}
	
	public void updateMetadata(final AbstractMessage inputEvent) throws DataLifecycleMetadataRepositoryException, InterruptedException {
		updateMetadata(inputEvent, LocalDateTime.now(ZoneId.of("UTC")));
	}
	
	public void updateMetadata(final AbstractMessage inputEvent, final LocalDateTime now) throws DataLifecycleMetadataRepositoryException, InterruptedException {
		final String obsKey = inputEvent.getKeyObjectStorage();
		
		final String fileName = DataLifecycleClientUtil.getFileName(obsKey);
		final String productName = DataLifecycleClientUtil.getProductName(obsKey);
		final ProductFamily productFamily = inputEvent.getProductFamily();
		final boolean isCompressedStorage = productFamily.isCompressed();

		final Date evictionDate = DataLifecycleClientUtil.calculateEvictionDate(this.retentionPolicies, inputEvent.getCreationDate(), productFamily, fileName);
		LocalDateTime evictionDateTime = null;
		if (null != evictionDate) {
			evictionDateTime = LocalDateTime.ofInstant(evictionDate.toInstant(), ZoneId.of("UTC"));
		}
		
		final DataLifecycleMetadata metadata = new DataLifecycleMetadata();
		final Map<String,Object> updateFields = new HashMap<>();

		metadata.setProductName(productName); // _id field (hence no update field)
		
		if (isCompressedStorage) {
			metadata.setEvictionDateInCompressedStorage(evictionDateTime);
			updateFields.put(DataLifecycleMetadata.FIELD_NAME.EVICTION_DATE_IN_COMPRESSED_STORAGE.fieldName(),
					metadata.getEvictionDateInCompressedStorage());

			metadata.setPathInCompressedStorage(obsKey);
			updateFields.put(DataLifecycleMetadata.FIELD_NAME.PATH_IN_COMPRESSED_STORAGE.fieldName(),
					metadata.getPathInCompressedStorage());

			metadata.setProductFamilyInCompressedStorage(productFamily);
			updateFields.put(DataLifecycleMetadata.FIELD_NAME.PRODUCT_FAMILY_IN_COMPRESSED_STORAGE.fieldName(),
					metadata.getProductFamilyInCompressedStorage());
			
			if (needsInsertionTimeUpdate(inputEvent)) {
				metadata.setLastInsertionInCompressedStorage(now);
				updateFields.put(DataLifecycleMetadata.FIELD_NAME.LAST_INSERTION_IN_COMPRESSED_STORAGE.fieldName(),
						metadata.getLastInsertionInCompressedStorage());
			}

			if (needsEvictionTimeShorteningInUncompressedStorage(inputEvent, this.shortingEvictionTimeAfterCompression)) {
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
					updateFields.put(DataLifecycleMetadata.FIELD_NAME.EVICTION_DATE_IN_UNCOMPRESSED_STORAGE.fieldName(),
							metadata.getEvictionDateInUncompressedStorage());
					LOG.info("shortening eviction date in uncompressed storage after compression of %s to %s", productName, shortenedEvictionDate);
				}
			}

			LOG.debug(String.format("upserting lifecycle metadata with compressed storage information for product %s with update to '%s' or index of '%s'",
					metadata.getProductName(), updateFields, metadata));
		} else {
			metadata.setEvictionDateInUncompressedStorage(evictionDateTime);
			updateFields.put(DataLifecycleMetadata.FIELD_NAME.EVICTION_DATE_IN_UNCOMPRESSED_STORAGE.fieldName(),
					metadata.getEvictionDateInUncompressedStorage());
			
			metadata.setPathInUncompressedStorage(obsKey);
			updateFields.put(DataLifecycleMetadata.FIELD_NAME.PATH_IN_UNCOMPRESSED_STORAGE.fieldName(),
					metadata.getPathInUncompressedStorage());
			
			metadata.setProductFamilyInUncompressedStorage(productFamily);
			updateFields.put(DataLifecycleMetadata.FIELD_NAME.PRODUCT_FAMILY_IN_UNCOMPRESSED_STORAGE.fieldName(),
					metadata.getProductFamilyInUncompressedStorage());

			if (needsInsertionTimeUpdate(inputEvent)) {
				metadata.setLastInsertionInUncompressedStorage(now);
				updateFields.put(DataLifecycleMetadata.FIELD_NAME.LAST_INSERTION_IN_UNCOMPRESSED_STORAGE.fieldName(),
						metadata.getLastInsertionInUncompressedStorage());
			}

			LOG.debug(String.format("upserting lifecycle metadata with uncompressed storage information for product %s with update to '%s' or index of '%s'",
					metadata.getProductName(), updateFields, metadata));
		}
		
		try {
			this.metadataRepo.upsert(metadata, updateFields);
		} catch (DataLifecycleMetadataRepositoryException e) {
			LOG.error("error saving data lifecycle metadata for " + productFamily.name() + ": "
					+ fileName + ": " + LogUtils.toString(e), e);
			throw e;
		}
	}
	
	
	
	static <E extends AbstractMessage> boolean needsInsertionTimeUpdate(final E event) {
		for (final Class<? extends AbstractMessage> updateClazz : UPDATE_INSERTIONTIME_ON) {
			if (updateClazz.isInstance(event)) {
				return true;
			}
		}

		return false;
	}

	static <E extends AbstractMessage> boolean needsEvictionTimeShorteningInUncompressedStorage(final E event,
			final Map<ProductFamily, Integer> shortingEvictionTimeAfterCompression) {
		// when compression event + shortening configuration available
		return ProductCategory.COMPRESSED_PRODUCTS.getDtoClass().isInstance(event)
				&& shortingEvictionTimeAfterCompression.containsKey(event.getProductFamily());
	}
	

}
