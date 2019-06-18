package esa.s1pdgs.cpoc.errorrepo.service;

import java.util.List;

import esa.s1pdgs.cpoc.errorrepo.model.rest.ProcessingDto;

public interface ProcessingsRepository {

	List<String> getProcessingTypes();

	List<ProcessingDto> getProcessings();

	ProcessingDto getProcessing(long id);
}
