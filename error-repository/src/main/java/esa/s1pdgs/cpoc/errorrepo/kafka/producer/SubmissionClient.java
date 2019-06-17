package esa.s1pdgs.cpoc.errorrepo.kafka.producer;

import esa.s1pdgs.cpoc.appcatalog.rest.MqiGenericMessageDto;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

public interface SubmissionClient {
	
	public static final SubmissionClient NULL = new SubmissionClient() {		
		@Override
		public void resubmit(FailedProcessingDto failedProcessing, MqiGenericMessageDto<?> message) {}
	};

	void resubmit(final FailedProcessingDto failedProcessing, final MqiGenericMessageDto<?> message);
}
