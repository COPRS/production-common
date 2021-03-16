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

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.datalifecycle.trigger.config.DataLifecycleTriggerConfigurationProperties;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.persistence.DataLifecycleMetadataRepositoryException;
import esa.s1pdgs.cpoc.message.MessageProducer;
import esa.s1pdgs.cpoc.mqi.model.queue.EvictionManagementJob;

@Service
public class DataLifecycleServiceImpl implements DataLifecycleService {
	private static final Logger LOG = LogManager.getLogger(DataLifecycleServiceImpl.class);

	private final DataLifecycleMetadataRepository lifecycleMetadataRepo;
	private final MessageProducer<EvictionManagementJob> messageProducer;
	private final String evictionTopic;

	// --------------------------------------------------------------------------

	@Autowired
	public DataLifecycleServiceImpl(final DataLifecycleTriggerConfigurationProperties configurationProperties,
			final DataLifecycleMetadataRepository lifecycleMetadataRepo, final MessageProducer<EvictionManagementJob> messageProducer) {
		this.lifecycleMetadataRepo = lifecycleMetadataRepo;
		this.messageProducer = messageProducer;
		this.evictionTopic = configurationProperties.getEvictionTopic();
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
			this.publish(job);
		}
	}

	private void publish(final EvictionManagementJob job) throws DataLifecycleTriggerInternalServerErrorException {
		try {
			this.messageProducer.send(this.evictionTopic, job);
		} catch (final Exception e) {
			throw new DataLifecycleTriggerInternalServerErrorException(
					String.format("Error on publishing EvictionManagementJob for %s to %s: %s", job.getKeyObjectStorage(), this.evictionTopic,
							Exceptions.messageOf(e)), e);
		}
	}

}
