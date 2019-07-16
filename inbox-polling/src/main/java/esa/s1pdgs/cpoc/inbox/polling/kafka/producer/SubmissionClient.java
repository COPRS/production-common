package esa.s1pdgs.cpoc.inbox.polling.kafka.producer;

import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;

public interface SubmissionClient {
	
	public static final SubmissionClient NULL = new SubmissionClient() {
		@Override
		public void resubmit(final String topic, IngestionDto dto) {}
	};	
	void resubmit(final String topic, final IngestionDto dto);
}
