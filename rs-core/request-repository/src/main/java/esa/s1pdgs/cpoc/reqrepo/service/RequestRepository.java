package esa.s1pdgs.cpoc.reqrepo.service;

import java.util.Arrays;
import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.common.FailedProcessing;
import esa.s1pdgs.cpoc.appcatalog.common.Processing;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

public interface RequestRepository {	

	public static final List<MessageState> PROCESSING_STATE_LIST = Arrays.asList(MessageState.values());
	
	List<String> getProcessingTypes();
	
	List<Processing> getProcessings(Integer pageSize, Integer pageNumber, List<String> processingType, List<MessageState> processingStatus);
	
	long getProcessingsCount(List<String> processingType, List<MessageState> processingStatus);

	Processing getProcessing(long id);

	void saveFailedProcessing(FailedProcessingDto failedProcessing);

	List<FailedProcessing> getFailedProcessings();

	FailedProcessing getFailedProcessingById(long id);

	void restartAndDeleteFailedProcessing(long id);
	
	void reevaluateAndDeleteFailedProcessing(long id);

	void deleteFailedProcessing(long id);

	long getFailedProcessingsCount();
}
