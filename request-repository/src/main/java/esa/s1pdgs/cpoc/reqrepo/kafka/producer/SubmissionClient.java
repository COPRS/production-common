package esa.s1pdgs.cpoc.reqrepo.kafka.producer;

import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.common.FailedProcessing;
import esa.s1pdgs.cpoc.appstatus.AppStatus;

public interface SubmissionClient {
	
	public static final SubmissionClient NULL = new SubmissionClient() {
		@Override
		public void resubmit(final FailedProcessing failedProcessing, final List<Object> messages, final AppStatus appStatus) {}
	};	
	void resubmit(final FailedProcessing failedProcessing, final List<Object> messages, final AppStatus appStatus);
}
