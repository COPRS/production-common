package esa.s1pdgs.cpoc.reqrepo.service;

import java.util.Arrays;
import java.util.List;

import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;

public interface RequestRepository {	

	public static final List<MessageState> PROCESSING_STATE_LIST = Arrays.asList(MessageState.values());
	
	List<String> getProcessingTypes();
	
	List<FailedProcessing> getFailedProcessings();

	FailedProcessing getFailedProcessingById(String id);

	void restartAndDeleteFailedProcessing(String id);
	
	void deleteFailedProcessing(String id);

	long getFailedProcessingsCount();
}
