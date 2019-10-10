package esa.s1pdgs.cpoc.reqrepo.kafka.producer;

import esa.s1pdgs.cpoc.appcatalog.common.FailedProcessing;

public interface SubmissionClient {
	
	public static final SubmissionClient NULL = new SubmissionClient() {
		@Override
		public void resubmit(FailedProcessing failedProcessing, Object message) {}
	};	
	void resubmit(final FailedProcessing failedProcessing, final Object message);
}
