package esa.s1pdgs.cpoc.reqrepo.service;

import java.util.Arrays;
import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.common.FailedProcessing;
import esa.s1pdgs.cpoc.appcatalog.common.Processing;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

public interface RequestRepository {	
	public static final List<String> PROCESSING_TYPES_LIST = Arrays.asList("t-pdgs-session-file-ingestion-events", "t-pdgs-aux-ingestion-events", "t-pdgs-aio-execution-jobs",
			"t-pdgs-l0asp-execution-jobs", "t-pdgs-aio-l0-segment-production-events", "t-pdgs-aio-l0-slice-production-events-nrt", "t-pdgs-aio-l0-acn-production-events-nrt",
			"t-pdgs-l0asp-l0-slice-production-events-fast", "t-pdgs-l0asp-l0-acn-production-events-fast", "t-pdgs-aio-production-report-events", "t-pdgs-l0asp-production-report-events",
			"t-pdgs-aio-l0-blank-production-events", "t-pdgs-l1-slices-production-events-nrt", "t-pdgs-l1-acn-production-events-nrt", "t-pdgs-l1-slices-production-events-fast",
			"t-pdgs-l1-acn-production-events-fast", "t-pdgs-l1-production-report-events", "t-pdgs-l1-execution-jobs-nrt", "t-pdgs-l1-execution-jobs-fast",
			"t-pdgs-l2-acn-production-events-fast", "t-pdgs-l2-slices-production-events-fast", "t-pdgs-l2-execution-jobs-fast", "t-pdgs-l2-reports",
			"t-pdgs-compressed-products");
	
	public static final List<MessageState> PROCESSING_STATE_LIST = Arrays.asList(MessageState.values());
	
	List<String> getProcessingTypes();
	
	List<Processing> getProcessings(Integer pageSize, Integer pageNumber, List<String> processingType, List<MessageState> processingStatus);
	
	long getProcessingsCount(List<String> processingType, List<MessageState> processingStatus);

	Processing getProcessing(long id);

	void saveFailedProcessing(FailedProcessingDto failedProcessing);

	List<FailedProcessing> getFailedProcessings();

	FailedProcessing getFailedProcessingById(long id);

	void restartAndDeleteFailedProcessing(long id);

	void deleteFailedProcessing(long id);

	long getFailedProcessingsCount();
}
