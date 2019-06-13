package esa.s1pdgs.cpoc.errorrepo.service;

import java.util.List;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

public interface ErrorRepository {
	List<FailedProcessingDto> getFailedProcessings();
	FailedProcessingDto getFailedProcessingsById(String id);
	void restartAndDeleteFailedProcessing(String id);
}
