package esa.s1pdgs.cpoc.errorrepo.service;

import java.util.List;

import esa.s1pdgs.cpoc.mqi.model.queue.ErrorDto;

public interface ErrorRepository {
	List<ErrorDto> getFailedProcessings();
	ErrorDto getFailedProcessingsById(String id);
}
