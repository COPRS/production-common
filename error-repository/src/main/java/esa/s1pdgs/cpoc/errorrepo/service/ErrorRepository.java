package esa.s1pdgs.cpoc.errorrepo.service;

import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.common.FailedProcessing;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

public interface ErrorRepository {

	void saveFailedProcessing(FailedProcessingDto failedProcessing);

	List<FailedProcessing> getFailedProcessings();

	FailedProcessing getFailedProcessingById(long id);

	void restartAndDeleteFailedProcessing(long id);

	void deleteFailedProcessing(long id);
}
