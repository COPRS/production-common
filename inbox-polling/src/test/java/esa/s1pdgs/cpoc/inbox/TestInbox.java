package esa.s1pdgs.cpoc.inbox;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import esa.s1pdgs.cpoc.inbox.entity.InboxEntry;
import esa.s1pdgs.cpoc.inbox.filter.InboxFilter;

public class TestInbox {
	@Test
	public final void testPoll_OnFindingNewProducts_ShallStoreProductsAndPutInKafkaQueue() {
		final InboxAdapter fakeAdapter = new InboxAdapter() {
			@Override
			public Collection<InboxEntry> read(InboxFilter filter) {
				return Arrays.asList(new InboxEntry("foo1", "file:///tmp/MPS_/S1A", "S1", "A", "MPS_"),
						new InboxEntry("foo2", "file:///tmp/WILE/S1B", "S1", "B", "WILE"));
			}

			@Override
			public String description() {
				return "fakeAdapter";
			}
		};
		final MockInboxEntryRepository fakeRepo = new MockInboxEntryRepository(2);
		final MockSubmissionClient fakeKafkaClient = new MockSubmissionClient(2);

		final Inbox uut = new Inbox(fakeAdapter, InboxFilter.ALLOW_ALL, new InboxPollingServiceTransactional(fakeRepo),
				fakeKafkaClient);
		uut.poll();
		fakeRepo.verify();
		fakeKafkaClient.verify();
	}

	@Test
	public final void testPoll_OnFindingAlreadyStoredProducts_ShallDoNothing() {
		final InboxAdapter fakeAdapter = new InboxAdapter() {
			@Override
			public Collection<InboxEntry> read(InboxFilter filter) {
				return Arrays.asList(new InboxEntry("foo1", "file:///tmp/MPS_/S1A", "S1", "A", "MPS_"),
						new InboxEntry("foo2", "file:///tmp/WILE/S1B", "S1", "B", "WILE"));
			}

			@Override
			public String description() {
				return "fakeAdapter";
			}
		};
		final MockInboxEntryRepository fakeRepo = new MockInboxEntryRepository(0) {
			@Override
			public Iterable<InboxEntry> findAll() {
				return Arrays.asList(new InboxEntry("foo2", "file:///tmp/WILE/S1B", "S1", "B", "WILE"),
						new InboxEntry("foo1", "file:///tmp/MPS_/S1A", "S1", "A", "MPS_"));
			}
		};
		final MockSubmissionClient fakeKafkaClient = new MockSubmissionClient(0);

		final Inbox uut = new Inbox(fakeAdapter, InboxFilter.ALLOW_ALL, new InboxPollingServiceTransactional(fakeRepo),
				fakeKafkaClient);
		uut.poll();
		fakeRepo.verify();
		fakeKafkaClient.verify();
	}
}
