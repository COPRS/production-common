package esa.s1pdgs.cpoc.datalifecycle.trigger.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.model.DataLifecycleMetadata;

@Service
public class DataLifecycleServiceImpl implements DataLifecycleService {

	// --------------------------------------------------------------------------

	@Override
	public void evict(String operatorName) {
		// TODO @MSc: impl

	}

	@Override
	public void evict(String productname, boolean forceCompressed, boolean forceUncompressed, String operatorName) {
		// TODO @MSc: impl

	}

	@Override
	public DataLifecycleMetadata updateRetention(String productname, LocalDateTime evictionTimeInCompressedStorage,
			LocalDateTime evictionTimeInUncompressedStorage, String operatorName) {
		// TODO @MSc: impl
		return null;
	}

}
