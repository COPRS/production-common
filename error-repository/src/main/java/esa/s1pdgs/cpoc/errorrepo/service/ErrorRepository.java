package esa.s1pdgs.cpoc.errorrepo.service;

import java.util.List;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

public interface ErrorRepository {

	void saveFailedProcessing(FailedProcessingDto failedProcessing);

	List<FailedProcessingDto> getFailedProcessings();

	FailedProcessingDto getFailedProcessingsById(String id);

	void restartAndDeleteFailedProcessing(String id);

	boolean deleteFailedProcessing(String id);
}
