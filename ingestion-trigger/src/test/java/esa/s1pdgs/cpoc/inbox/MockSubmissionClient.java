package esa.s1pdgs.cpoc.inbox;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import esa.s1pdgs.cpoc.inbox.kafka.producer.SubmissionClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

class MockSubmissionClient implements SubmissionClient
{
	private final List<IngestionJob> elements = new ArrayList<>();
	private final int expectedCalls;
	
	public MockSubmissionClient(int expectedCalls) {
		this.expectedCalls = expectedCalls;
	}
	
	@Override
	public void publish(IngestionJob dto) {
		elements.add(dto);			
	}
	
	final void verify() throws AssertionError {
		assertEquals(expectedCalls, elements.size());
	}
	
}