package esa.s1pdgs.cpoc.errorrepo.service;

import java.util.List;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

public interface ErrorRepository {

	@SuppressWarnings("rawtypes")
	void saveFailedProcessing(FailedProcessingDto failedProcessing);

	@SuppressWarnings("rawtypes")
	List<FailedProcessingDto> getFailedProcessings();

	@SuppressWarnings("rawtypes")
	FailedProcessingDto getFailedProcessingsById(String id);

	void restartAndDeleteFailedProcessing(String id);

	boolean deleteFailedProcessing(String id);
}
