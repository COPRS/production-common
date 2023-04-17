package de.werum.coprs.requestparkinglot.service;

import java.util.Arrays;
import java.util.List;

import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;

public interface RequestParkingLot {	

	@SuppressWarnings("java:S2386")
	public static final List<MessageState> PROCESSING_STATE_LIST = Arrays.asList(MessageState.values());

	List<FailedProcessing> getFailedProcessings();

	FailedProcessing getFailedProcessingById(String id);

	void restartAndDeleteFailedProcessing(String id) throws AllowedActionNotAvailableException;

	void resubmitAndDeleteFailedProcessing(String id) throws AllowedActionNotAvailableException;

	void deleteFailedProcessing(String id);

	long getFailedProcessingsCount();
}
