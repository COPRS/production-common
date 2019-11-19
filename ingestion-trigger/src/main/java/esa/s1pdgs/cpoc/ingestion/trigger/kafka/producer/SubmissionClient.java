package esa.s1pdgs.cpoc.ingestion.trigger.kafka.producer;

import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

public interface SubmissionClient {
	
	public static final SubmissionClient NULL = new SubmissionClient() {
		@Override
		public void publish(IngestionJob dto) {}
	};	
	void publish(final IngestionJob dto);
}
