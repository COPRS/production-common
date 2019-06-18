package esa.s1pdgs.cpoc.errorrepo.service;

import java.util.List;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

public interface ErrorRepository {

	@SuppressWarnings("rawtypes")
	void saveFailedProcessing(FailedProcessingDto failedProcessing);

	@SuppressWarnings("rawtypes")
	List<FailedProcessingDto> getFailedProcessings();

	@SuppressWarnings("rawtypes")
	FailedProcessingDto getFailedProcessingsById(long id);

	void restartAndDeleteFailedProcessing(long id);

	boolean deleteFailedProcessing(long id);
}
