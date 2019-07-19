package esa.s1pdgs.cpoc.inbox.kafka.producer;

import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;

public interface SubmissionClient {
	
	public static final SubmissionClient NULL = new SubmissionClient() {
		@Override
		public void publish(IngestionDto dto) {}
	};	
	void publish(final IngestionDto dto);
}
