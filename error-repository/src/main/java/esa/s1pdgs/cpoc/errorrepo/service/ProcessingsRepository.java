package esa.s1pdgs.cpoc.errorrepo.service;

import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.common.Processing;

public interface ProcessingsRepository {

	List<String> getProcessingTypes();
	
	List<Processing> getProcessings();

	Processing getProcessing(long id);
}
