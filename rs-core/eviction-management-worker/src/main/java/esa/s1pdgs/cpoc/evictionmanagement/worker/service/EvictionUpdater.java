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

package esa.s1pdgs.cpoc.evictionmanagement.worker.service;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.datalifecycle.client.DataLifecycleClientUtil;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepositoryException;
import esa.s1pdgs.cpoc.datalifecycle.client.error.DataLifecycleTriggerInternalServerErrorException;

public class EvictionUpdater {
	
	private static final Logger LOG = LogManager.getLogger(EvictionUpdater.class);
	
	private final DataLifecycleMetadataRepository metadataRepo;
	
	public EvictionUpdater(final DataLifecycleMetadataRepository metadataRepo) {
		
		this.metadataRepo = metadataRepo;
	}
	
	public void updateEvictedMetadata(final String obsKey, final ProductFamily productFamily)
			throws DataLifecycleTriggerInternalServerErrorException {
		final String productName = DataLifecycleClientUtil.getProductName(obsKey);
		final boolean isCompressedStorage = productFamily.isCompressed();

		final Optional<DataLifecycleMetadata> oExistingMetadata;
		try {
			oExistingMetadata = metadataRepo.findByProductName(productName);
		} catch (final NoSuchElementException e) {
			LOG.error("error updating lifecycle metadata due to eviction of " + productName + ": " + LogUtils.toString(e), e);
			throw e;
		}

		if (!oExistingMetadata.isPresent()) {
			LOG.error("error updating lifecycle metadata due to eviction of " + productName + ": no lifecycle metadata found");
			throw new DataLifecycleTriggerInternalServerErrorException(
					"error updating lifecycle metadata due to eviction of " + productName + ": no lifecycle metadata found");
		}

		final Map<String,Object> updateFields = new HashMap<>();

		// erase storage path as it is no longer valid
		if (isCompressedStorage) {
			updateFields.put(DataLifecycleMetadata.FIELD_NAME.PATH_IN_COMPRESSED_STORAGE.fieldName(), null);
			LOG.debug("erasing path in compressed storage from lifecycle metadata due to eviction of: " + productName);
		} else {
			updateFields.put(DataLifecycleMetadata.FIELD_NAME.PATH_IN_UNCOMPRESSED_STORAGE.fieldName(), null);
			LOG.debug("erasing path in uncompressed storage from lifecycle metadata due to eviction of: " + productName);
		}

		try {
			this.metadataRepo.update(productName, updateFields);
		} catch (final DataLifecycleMetadataRepositoryException e) {
			LOG.error("error updating lifecycle metadata due to eviction of " + productName + ": " + LogUtils.toString(e), e);
			throw e;
		}
	}

}
