package esa.s1pdgs.cpoc.evictionmanagement.worker.service;

import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.LAST_MODIFIED;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleSortTerm;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleSortTerm.DataLifecycleSortOrder;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.datalifecycle.client.error.DataLifecycleTriggerInternalServerErrorException;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;

public class EvictionManagementService {
	
	private static final Logger LOG = LogManager.getLogger(EvictionManagementService.class);
	
	private final DataLifecycleMetadataRepository metadataRepo;
	private final EvictionUpdater evictionUpdater;
	private final ObsClient obsClient;
	
	public EvictionManagementService(final DataLifecycleMetadataRepository metadataRepo, final ObsClient obsClient) {
		this.metadataRepo = metadataRepo;
		this.obsClient = obsClient;
		this.evictionUpdater = new EvictionUpdater(metadataRepo);
	}
	
	
	@Scheduled(fixedRateString = "${eviction-management-worker.eviction-interval-ms}" )
	public void evict() {
		try {
			updateAndDelete();
		} catch (DataLifecycleTriggerInternalServerErrorException e) {
			LOG.error(e.getMessage());
		}
	}
	
	
	private void updateAndDelete() throws DataLifecycleTriggerInternalServerErrorException {
		LOG.debug("starting general eviction ");

		final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
		final DataLifecycleSortTerm sortTerm = new DataLifecycleSortTerm(LAST_MODIFIED, DataLifecycleSortOrder.ASCENDING);

		final int pageSize = 100;
		int offset = 0;
		List<DataLifecycleMetadata> productsToDelete;
		final List<Exception> errors = new ArrayList<>();
		long numEvictions = 0;

		try {
			do {
				// get result page
				productsToDelete = CollectionUtil.nullToEmptyList(this.metadataRepo.findByEvictionDateBefore(now, Optional.of(pageSize),
						Optional.of(offset), Collections.singletonList(sortTerm)));
				LOG.debug("found " + productsToDelete.size() + " products (page size: " + pageSize + ") to evict ");

				// process result page
				for (final DataLifecycleMetadata metadata : productsToDelete) {
					try {
						numEvictions += this.updateAndDelete(metadata, false, false);
					} catch (final DataLifecycleTriggerInternalServerErrorException | ObsException | ObsServiceException e) {
						LOG.error("error on evicting product, will skip this one: " + e.getMessage());
						errors.add(e);
						continue;
					}
				}
				// calculate offset for next page
				if (((long) offset + pageSize) > Integer.MAX_VALUE) {
					throw new DataLifecycleTriggerInternalServerErrorException("paging offset exceeds limit of " + Integer.MAX_VALUE);
				}
				offset += pageSize;
			} while (CollectionUtil.isNotEmpty(productsToDelete));
		} catch (final DataLifecycleTriggerInternalServerErrorException e) {
			LOG.error("error searching for products to evict: "+ e.getMessage());
			throw e;
		}

		if (!errors.isEmpty()) {
			if (errors.size() == 1) {
				throw new DataLifecycleTriggerInternalServerErrorException("an error ocurred on general eviction, " + numEvictions
						+ " evictions performed though: " + Exceptions.toString(errors.get(0)));
			} else {
				throw new DataLifecycleTriggerInternalServerErrorException(errors.size() + " errors ocurred on general eviction, " + numEvictions
						+ " evictions performed though.");
			}
		}

		LOG.info(numEvictions + " evictions performed");
	}
	
	private int updateAndDelete(@NonNull DataLifecycleMetadata dataLifecycleMetadata, boolean forceCompressed,
			boolean forceUncompressed)
			throws DataLifecycleTriggerInternalServerErrorException, ObsException, ObsServiceException {
		int numEvicted = 0;
		final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));

		// uncompressed
		final String pathInUncompressedStorage = dataLifecycleMetadata.getPathInUncompressedStorage();
		final LocalDateTime evictionDateInUncompressedStorage = dataLifecycleMetadata.getEvictionDateInUncompressedStorage();
		if (StringUtil.isNotBlank(pathInUncompressedStorage)
				&& (forceUncompressed || (null != evictionDateInUncompressedStorage && now.isAfter(evictionDateInUncompressedStorage)))) {
			
			final ProductFamily productFamilyInUncompressedStorage = dataLifecycleMetadata.getProductFamilyInUncompressedStorage();
			if (null == productFamilyInUncompressedStorage || ProductFamily.BLANK == productFamilyInUncompressedStorage) {
				throw new DataLifecycleTriggerInternalServerErrorException(
						"error trigger eviction of product, no valid product family found for uncompressed storage for: " + dataLifecycleMetadata);
			}
			LOG.debug("eviction of product from uncompressed storage: " + dataLifecycleMetadata);
			evictionUpdater.updateEvictedMetadata(pathInUncompressedStorage, productFamilyInUncompressedStorage);
			obsClient.delete(new ObsObject(productFamilyInUncompressedStorage, pathInUncompressedStorage));
			numEvicted++;
			
		} else {
			LOG.debug("cannot evict product from uncompressed storage: " + dataLifecycleMetadata);
		}

		// compressed
		final String pathInCompressedStorage = dataLifecycleMetadata.getPathInCompressedStorage();
		final LocalDateTime evictionDateInCompressedStorage = dataLifecycleMetadata.getEvictionDateInCompressedStorage();

		if (StringUtil.isNotBlank(pathInCompressedStorage)
				&& (forceCompressed || (null != evictionDateInCompressedStorage && now.isAfter(evictionDateInCompressedStorage)))) {
			final ProductFamily productFamilyInCompressedStorage = dataLifecycleMetadata.getProductFamilyInCompressedStorage();
			if (null == productFamilyInCompressedStorage || ProductFamily.BLANK == productFamilyInCompressedStorage) {
				throw new DataLifecycleTriggerInternalServerErrorException(
						"error trigger evictiion of product, no valid product family found for compressed storage for: " + dataLifecycleMetadata);
			}
			LOG.debug("eviction of product from compressed storage: " + dataLifecycleMetadata);
			evictionUpdater.updateEvictedMetadata(pathInCompressedStorage, productFamilyInCompressedStorage);
			obsClient.delete(new ObsObject(productFamilyInCompressedStorage, pathInCompressedStorage));
			numEvicted++;
		} else {
			LOG.debug("cannot evict product from compressed storage: " + dataLifecycleMetadata);
		}

		return numEvicted;
	}

}
