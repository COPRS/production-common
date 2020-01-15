package esa.s1pdgs.cpoc.ingestion.trigger;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.InboxFilter;

public class TestInbox {
	
	@Ignore
	@Test
	public final void testPoll_OnFindingNewProducts_ShallStoreProductsAndPutInKafkaQueue() {
		final InboxAdapter fakeAdapter = new InboxAdapter() {
			@Override
			public Collection<InboxEntry> read(final InboxFilter filter) {
				return Arrays.asList(new InboxEntry("foo1", "foo1", "/tmp"),
						new InboxEntry("foo2", "foo2", "/tmp"));
			}

			@Override
			public String description() {
				return "fakeAdapter";
			}

			@Override
			public String inboxPath() {
				return "/tmp";
			}
		};
		final MockInboxEntryRepository fakeRepo = new MockInboxEntryRepository(2);
		final MockSubmissionClient fakeKafkaClient = new MockSubmissionClient(2);

		final Inbox uut = new Inbox(
				fakeAdapter, 
				InboxFilter.ALLOW_ALL,
				new IngestionTriggerServiceTransactional(fakeRepo), 
				fakeKafkaClient
		);
		uut.poll();
		fakeRepo.verify();
		fakeKafkaClient.verify();
	}

	@Test
	public final void testPoll_OnFindingAlreadyStoredProducts_ShallDoNothing() {
		final InboxAdapter fakeAdapter = new InboxAdapter() {
			@Override
			public Collection<InboxEntry> read(final InboxFilter filter) {
				return Arrays.asList(
						new InboxEntry("foo1", "foo1", "/tmp"),
						new InboxEntry("foo2", "foo2", "/tmp"));
			}

			@Override
			public String description() {
				return "fakeAdapter";
			}
			
			@Override
			public String inboxPath() {
				return "/tmp";
			}
		};
		final MockInboxEntryRepository fakeRepo = new MockInboxEntryRepository(0) {
			@Override
			public List<InboxEntry> findByPickupPath(final String pickupPath) {
				return Arrays.asList(new InboxEntry("foo2", "foo2", "/tmp"),
						new InboxEntry("foo1", "foo1", "/tmp"));
			}
		};
		final MockSubmissionClient fakeKafkaClient = new MockSubmissionClient(0);

		final Inbox uut = new Inbox(fakeAdapter, InboxFilter.ALLOW_ALL,
				new IngestionTriggerServiceTransactional(fakeRepo), fakeKafkaClient);
		uut.poll();
		fakeRepo.verify();
		fakeKafkaClient.verify();
	}
}
