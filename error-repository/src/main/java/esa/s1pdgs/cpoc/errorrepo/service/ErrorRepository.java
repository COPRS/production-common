package esa.s1pdgs.cpoc.errorrepo.service;

import java.util.List;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;

public interface ErrorRepository {
	List<FailedProcessing> getFailedProcessings();
	FailedProcessing getFailedProcessingsById(String id);
	void restartAndDeleteFailedProcessing(String id);
}
