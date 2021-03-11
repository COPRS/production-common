package esa.s1pdgs.cpoc.datalifecycle.trigger.service;

import java.time.LocalDateTime;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.persistence.DataLifecycleMetadataRepositoryException;

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

		final Optional<DataLifecycleMetadata> oLifecycleMetadata = this.lifecycleMetadataRepo.findByProductName(productname);

		if (oLifecycleMetadata.isPresent()) {
			//this.evict(oLifecycleMetadata.get(), forceCompressed, forceUncompressed, operatorName);
		} else {
			LOG.debug("cannot evict product as no data lifecycle metadata found for product: " + productname);
			throw new DataLifecycleMetadataNotFoundException("no data lifecycle metadata found for product: " + productname);
		}
	}

	@Override
	public DataLifecycleMetadata updateRetention(String productname, LocalDateTime evictionTimeInCompressedStorage,
			LocalDateTime evictionTimeInUncompressedStorage, String operatorName) throws DataLifecycleMetadataRepositoryException {
		// TODO @MSc: impl
		return null;
	}

	// --------------------------------------------------------------------------

	//	private EvictionManagementJob toEvictionManagementJob(DataLifecycleMetadata dataLifecycleMetadata, boolean forceCompressed, boolean forceUncompressed,
	//			String operatorName) {
	//		final EvictionManagementJob evictionManagementJob = new EvictionManagementJob();
	//
	//		// TODO @MSc: in dataLifecycleMetadata schauen ob evictiondate erreicht oder force=true, dann storagekey in EvictionManagementJob setzen
	//		evictionManagementJob.setKeyObjectStorage(keyObjectStorage);
	//		evictionManagementJob.setProductFamily(productFamily);
	//		evictionManagementJob.setOperatorName(operatorName);
	//
	//		return evictionManagementJob;
	//	}
	//
	//	private DataLifecycleMetadata evict(DataLifecycleMetadata dataLifecycleMetadata, boolean forceCompressed, boolean forceUncompressed, String operatorName) {
	//
	//		// TODO @MSc: in dataLifecycleMetadata schauen ob evictiondate erreicht oder force=true, jeweils für compressed und uncompressed
	//		// TODO @MSc: dann wenn nötig EvictionManagementJob(s) daraus erstellen und abschicken
	//
	//		return null;
	//	}

}
