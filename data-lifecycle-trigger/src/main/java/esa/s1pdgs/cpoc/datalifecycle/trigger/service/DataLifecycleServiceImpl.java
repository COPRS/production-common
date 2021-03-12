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

import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.persistence.DataLifecycleMetadataRepositoryException;
import esa.s1pdgs.cpoc.mqi.model.queue.EvictionManagementJob;

@Service
public class DataLifecycleServiceImpl implements DataLifecycleService {
	private static final Logger LOG = LogManager.getLogger(DataLifecycleServiceImpl.class);

	private final DataLifecycleMetadataRepository lifecycleMetadataRepo;

	// --------------------------------------------------------------------------

	@Autowired
	public DataLifecycleServiceImpl(final DataLifecycleMetadataRepository lifecycleMetadataRepo) {
		this.lifecycleMetadataRepo = lifecycleMetadataRepo;
	}

	// --------------------------------------------------------------------------

	@Override
	public void evict(String operatorName) throws DataLifecycleMetadataRepositoryException {
		LOG.debug("starting general eviction request ...");
		// TODO @MSc: impl
	}

	@Override
	public void evict(String productname, boolean forceCompressed, boolean forceUncompressed, String operatorName)
			throws DataLifecycleMetadataRepositoryException, DataLifecycleMetadataNotFoundException {
		LOG.debug("starting eviction request for '" + productname + "' (forceCompressed=" + forceCompressed + ", forceUncompressed=" + forceUncompressed + ")");

		final DataLifecycleMetadata lifecycleMetadata;
		try {
			lifecycleMetadata = this.getAndCheckExists(productname);
		} catch (final DataLifecycleMetadataNotFoundException e) {
			LOG.info("cannot evict product: " + e.getMessage());
			throw e;
		} catch (final DataLifecycleMetadataRepositoryException e) {
			LOG.error("error on evicting product: " + e.getMessage());
			throw e;
		}

		this.evict(lifecycleMetadata, forceCompressed, forceUncompressed, operatorName);
	}

	@Override
	public DataLifecycleMetadata updateRetention(String productname, LocalDateTime evictionTimeInCompressedStorage,
			LocalDateTime evictionTimeInUncompressedStorage, String operatorName) throws DataLifecycleMetadataRepositoryException {
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

	private DataLifecycleMetadata evict(@NonNull DataLifecycleMetadata dataLifecycleMetadata, boolean forceCompressed, boolean forceUncompressed,
			String operatorName) {
		final List<EvictionManagementJob> evictionJobs = new ArrayList<>();
		final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

		// uncompressed
		final String pathInUncompressedStorage = dataLifecycleMetadata.getPathInUncompressedStorage();
		final LocalDateTime evictionDateInUncompressedStorage = dataLifecycleMetadata.getEvictionDateInUncompressedStorage();

		if (StringUtil.isNotBlank(pathInUncompressedStorage)
				&& (forceUncompressed || (null != evictionDateInUncompressedStorage && now.isAfter(evictionDateInUncompressedStorage)))) {
			final EvictionManagementJob evictionJobUncompressed = new EvictionManagementJob();

			evictionJobUncompressed.setKeyObjectStorage(pathInUncompressedStorage);
			// TODO @MSc: evictionJobUncompressed.setProductFamily(productFamily);
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
			final EvictionManagementJob evictionJobCompressed = new EvictionManagementJob();

			evictionJobCompressed.setKeyObjectStorage(pathInCompressedStorage);
			// TODO @MSc: evictionJobCompressed.setProductFamily(productFamily);
			evictionJobCompressed.setOperatorName(operatorName);

			evictionJobs.add(evictionJobCompressed);
			LOG.debug("will evict product from compressed storage: " + dataLifecycleMetadata);
		} else {
			LOG.debug("cannot evict product from compressed storage: " + dataLifecycleMetadata);
		}

		// TODO @MSc: EvictionManagementJob(s) abschicken über mqiclient (dazu brauch man product family)

		return null;
	}

}
