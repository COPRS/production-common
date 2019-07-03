package esa.s1pdgs.cpoc.reqrepo.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import esa.s1pdgs.cpoc.appcatalog.common.Processing;

public interface ProcessingsRepository {

	List<String> getProcessingTypes();
	
	List<Processing> getProcessings(Pageable pageable);

	Processing getProcessing(long id);
}
