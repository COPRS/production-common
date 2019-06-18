package esa.s1pdgs.cpoc.errorrepo.kafka.producer;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

public interface SubmissionClient {
	
	public static final SubmissionClient NULL = new SubmissionClient() {		
		@Override
		public void resubmit(FailedProcessingDto failedProcessing, Object message) {}
	};

	void resubmit(final FailedProcessingDto failedProcessing, final Object message);
}
