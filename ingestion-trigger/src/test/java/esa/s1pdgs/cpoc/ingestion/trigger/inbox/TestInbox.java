package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntryRepository;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.InboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.kafka.producer.SubmissionClient;
import esa.s1pdgs.cpoc.ingestion.trigger.service.IngestionTriggerServiceTransactional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestInbox {

    @Mock
    SubmissionClient fakeKafkaClient;

    @Mock
    InboxAdapter fakeAdapter;

    @Mock
    InboxEntryRepository fakeRepo;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public final void testPoll_OnFindingNewProducts_ShallStoreProductsAndPutInKafkaQueue() throws IOException {

        when(fakeAdapter.read(any())).thenReturn(Arrays.asList(new InboxEntry(0, "foo1", "foo1", "/tmp", new Date(), 10),
                new InboxEntry(0, "foo2", "foo2", "/tmp", new Date(), 10)));
        when(fakeAdapter.description()).thenReturn("fakeAdapter");
        when(fakeAdapter.inboxURL()).thenReturn("/tmp");

        final Inbox uut = new Inbox(
                fakeAdapter,
                InboxFilter.ALLOW_ALL,
                new IngestionTriggerServiceTransactional(fakeRepo),
                fakeKafkaClient,
                ProductFamily.EDRS_SESSION
        );
        uut.poll();

        verify(fakeRepo, times(2)).save(any());
        verify(fakeKafkaClient, times(2)).publish(any());
    }

    @Test
    public final void testPoll_OnFindingAlreadyStoredProducts_ShallDoNothing() throws IOException {

        when(fakeAdapter.read(any())).thenReturn(Arrays.asList(
                new InboxEntry(0, "foo1", "foo1", "/tmp", new Date(), 0),
                new InboxEntry(0, "foo2", "foo2", "/tmp", new Date(), 0)));
        when(fakeAdapter.description()).thenReturn("fakeAdapter");
        when(fakeAdapter.inboxURL()).thenReturn("/tmp");


        when(fakeRepo.findByPickupURL(anyString())).thenReturn(Arrays.asList(new InboxEntry(0, "foo2", "foo2", "/tmp", new Date(), 0),
                new InboxEntry(0, "foo1", "foo1", "/tmp", new Date(), 0)));

        final Inbox uut = new Inbox(
                fakeAdapter,
                InboxFilter.ALLOW_ALL,
                new IngestionTriggerServiceTransactional(fakeRepo),
                fakeKafkaClient,
                ProductFamily.EDRS_SESSION
        );
        uut.poll();

        verify(fakeRepo, times(0)).save(any());
        verify(fakeKafkaClient, times(0)).publish(any());
    }
}
