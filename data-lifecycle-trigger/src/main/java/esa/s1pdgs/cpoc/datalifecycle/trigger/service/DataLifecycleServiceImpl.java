package esa.s1pdgs.cpoc.datalifecycle.trigger.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.persistence.DataLifecycleMetadataRepositoryException;

@Service
public class DataLifecycleServiceImpl implements DataLifecycleService {

	private final DataLifecycleMetadataRepository lifecycleMetadataRepo;

	// --------------------------------------------------------------------------

	@Autowired
	public DataLifecycleServiceImpl(final DataLifecycleMetadataRepository lifecycleMetadataRepo) {
		this.lifecycleMetadataRepo = lifecycleMetadataRepo;
	}

	// --------------------------------------------------------------------------

	@Override
	public void evict(String operatorName) throws DataLifecycleMetadataRepositoryException {
		// TODO @MSc: impl
	}

	@Override
	public void evict(String productname, boolean forceCompressed, boolean forceUncompressed, String operatorName)
			throws DataLifecycleMetadataRepositoryException {
		// TODO @MSc: impl
	}

	@Override
	public DataLifecycleMetadata updateRetention(String productname, LocalDateTime evictionTimeInCompressedStorage,
			LocalDateTime evictionTimeInUncompressedStorage, String operatorName)
			throws DataLifecycleMetadataRepositoryException {
		// TODO @MSc: impl
		return null;
	}

	// --------------------------------------------------------------------------

}
