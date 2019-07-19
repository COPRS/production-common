package esa.s1pdgs.cpoc.inbox;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import esa.s1pdgs.cpoc.inbox.entity.InboxEntry;

class MockInboxEntryRepository extends AbstractInboxEntryRepository
{
	private final List<InboxEntry> saved = new ArrayList<>();
	private final int expectedSaves;
	
	public MockInboxEntryRepository(int expectedSaves) {
		this.expectedSaves = expectedSaves;
	}
	
	@Override
	public <S extends InboxEntry> S save(S entity) {
		saved.add(entity);
		return entity;
	}
	
	final void verify() throws AssertionError {
		assertEquals(expectedSaves, saved.size());
	}
}