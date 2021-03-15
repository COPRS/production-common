package esa.s1pdgs.cpoc.datalifecycle.trigger.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.persistence.DataLifecycleMetadataRepositoryException;
import esa.s1pdgs.cpoc.mqi.client.MqiClient;
import esa.s1pdgs.cpoc.mqi.model.queue.EvictionManagementJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;

@Service
public class DataLifecycleServiceImpl implements DataLifecycleService {
	private static final Logger LOG = LogManager.getLogger(DataLifecycleServiceImpl.class);

	private final DataLifecycleMetadataRepository lifecycleMetadataRepo;
	private final MqiClient mqiClient;

	// --------------------------------------------------------------------------

	@Autowired
	public DataLifecycleServiceImpl(final DataLifecycleMetadataRepository lifecycleMetadataRepo, final MqiClient mqiClient) {
		this.lifecycleMetadataRepo = lifecycleMetadataRepo;
		this.mqiClient = mqiClient;
	}

	// --------------------------------------------------------------------------

	@Override
	public void evict(String operatorName) throws DataLifecycleTriggerInternalServerErrorException {
		LOG.debug("starting general eviction request ...");
		// TODO @MSc: impl
	}

	@Override
	public void evict(String productname, boolean forceCompressed, boolean forceUncompressed, String operatorName)
			throws DataLifecycleTriggerInternalServerErrorException, DataLifecycleMetadataNotFoundException {
		LOG.debug("starting eviction request for '" + productname + "' (forceCompressed=" + forceCompressed + ", forceUncompressed=" + forceUncompressed + ")");

		try {
			final DataLifecycleMetadata lifecycleMetadata = this.getAndCheckExists(productname);
			this.evict(lifecycleMetadata, forceCompressed, forceUncompressed, operatorName);
		} catch (final DataLifecycleMetadataNotFoundException e) {
			LOG.info("cannot evict product: " + e.getMessage());
			throw e;
		} catch (final DataLifecycleTriggerInternalServerErrorException e) {
			LOG.error("error on evicting product: " + e.getMessage());
			throw e;
		}
	}

	@Override
	public DataLifecycleMetadata updateRetention(String productname, LocalDateTime evictionTimeInCompressedStorage,
			LocalDateTime evictionTimeInUncompressedStorage, String operatorName) throws DataLifecycleTriggerInternalServerErrorException {
		// TODO @MSc: impl
		return null;
	}

	@Override
	public List<DataLifecycleMetadata> getProducts(String namePattern, boolean persistentInUncompressedStorage,
			LocalDateTime minimalEvictionTimeInUncompressedStorage, LocalDateTime maximalEvictionTimeInUncompressedStorage,
			boolean persistentIncompressedStorage, LocalDateTime minimalEvictionTimeInCompressedStorage, LocalDateTime maximalEvictionTimeInCompressedStorage,
			boolean availableInLta, Integer pageSize, Integer pageNumber) {
		// TODO @MSc: impl
		return null;
	}

	@Override
	public List<DataLifecycleMetadata> getProducts(List<String> productnames) {
		// TODO @MSc: impl
		return null;
	}

	@Override
	public DataLifecycleMetadata getProduct(String productname) {
		// TODO @MSc: impl
		return null;
	}

	// --------------------------------------------------------------------------

	private DataLifecycleMetadata getAndCheckExists(final String productname)
			throws DataLifecycleMetadataNotFoundException, DataLifecycleMetadataRepositoryException {
		final Optional<DataLifecycleMetadata> oLifecycleMetadata = this.lifecycleMetadataRepo.findByProductName(productname);

		if (oLifecycleMetadata.isPresent()) {
			final DataLifecycleMetadata dataLifecycleMetadata = oLifecycleMetadata.get();

			if (StringUtil.isNotBlank(dataLifecycleMetadata.getPathInUncompressedStorage())
					|| StringUtil.isNotBlank(dataLifecycleMetadata.getPathInCompressedStorage())) {
				return dataLifecycleMetadata;
			} else {
				throw new DataLifecycleMetadataNotFoundException("data lifecycle metadata contains no storage paths for product: " + productname);
			}
		} else {
			throw new DataLifecycleMetadataNotFoundException("no data lifecycle metadata found for product: " + productname);
		}
	}

	private void evict(@NonNull DataLifecycleMetadata dataLifecycleMetadata, boolean forceCompressed, boolean forceUncompressed,
			String operatorName) throws DataLifecycleTriggerInternalServerErrorException {
		final List<EvictionManagementJob> evictionJobs = new ArrayList<>();
		final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

		// uncompressed
		final String pathInUncompressedStorage = dataLifecycleMetadata.getPathInUncompressedStorage();
		final LocalDateTime evictionDateInUncompressedStorage = dataLifecycleMetadata.getEvictionDateInUncompressedStorage();

		if (StringUtil.isNotBlank(pathInUncompressedStorage)
				&& (forceUncompressed || (null != evictionDateInUncompressedStorage && now.isAfter(evictionDateInUncompressedStorage)))) {
			final ProductFamily productFamilyInUncompressedStorage = dataLifecycleMetadata.getProductFamilyInUncompressedStorage();
			if (null == productFamilyInUncompressedStorage || ProductFamily.BLANK == productFamilyInUncompressedStorage) {
				throw new DataLifecycleTriggerInternalServerErrorException(
						"error evicting product, no valid product family found for uncompressed storage for: " + dataLifecycleMetadata);
			}

			final EvictionManagementJob evictionJobUncompressed = new EvictionManagementJob();

			evictionJobUncompressed.setKeyObjectStorage(pathInUncompressedStorage);
			evictionJobUncompressed.setProductFamily(productFamilyInUncompressedStorage);
			evictionJobUncompressed.setOperatorName(operatorName);

			evictionJobs.add(evictionJobUncompressed);
			LOG.debug("will evict product from uncompressed storage: " + dataLifecycleMetadata);
		} else {
			LOG.debug("cannot evict product from uncompressed storage: " + dataLifecycleMetadata);
		}

		// compressed
		final String pathInCompressedStorage = dataLifecycleMetadata.getPathInCompressedStorage();
		final LocalDateTime evictionDateInCompressedStorage = dataLifecycleMetadata.getEvictionDateInCompressedStorage();

		if (StringUtil.isNotBlank(pathInCompressedStorage)
				&& (forceUncompressed || (null != evictionDateInCompressedStorage && now.isAfter(evictionDateInCompressedStorage)))) {
			final ProductFamily productFamilyInCompressedStorage = dataLifecycleMetadata.getProductFamilyInCompressedStorage();
			if (null == productFamilyInCompressedStorage || ProductFamily.BLANK == productFamilyInCompressedStorage) {
				throw new DataLifecycleTriggerInternalServerErrorException(
						"error evicting product, no valid product family found for compressed storage for: " + dataLifecycleMetadata);
			}

			final EvictionManagementJob evictionJobCompressed = new EvictionManagementJob();

			evictionJobCompressed.setKeyObjectStorage(pathInCompressedStorage);
			evictionJobCompressed.setProductFamily(productFamilyInCompressedStorage);
			evictionJobCompressed.setOperatorName(operatorName);

			evictionJobs.add(evictionJobCompressed);
			LOG.debug("will evict product from compressed storage: " + dataLifecycleMetadata);
		} else {
			LOG.debug("cannot evict product from compressed storage: " + dataLifecycleMetadata);
		}

		// publish jobs
		for (final EvictionManagementJob job : evictionJobs) {
			try {
				this.publishEvictionJob(job);
			} catch (final AbstractCodedException e) {
				throw new DataLifecycleTriggerInternalServerErrorException("error publishing eviction management job: " + job, e);
			}
		}
	}

	private void publishEvictionJob(final EvictionManagementJob job) throws AbstractCodedException {
		final GenericPublicationMessageDto<EvictionManagementJob> message = new GenericPublicationMessageDto<>(job.getProductFamily(), job);
		final ProductCategory productCategory = ProductCategory.of(job.getProductFamily());

		this.mqiClient.publish(message, productCategory);
	}

}
