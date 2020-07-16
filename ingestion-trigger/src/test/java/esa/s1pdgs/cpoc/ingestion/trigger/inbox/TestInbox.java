package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntryRepository;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.InboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.kafka.producer.SubmissionClient;
import esa.s1pdgs.cpoc.ingestion.trigger.name.FlatProductNameEvaluator;
import esa.s1pdgs.cpoc.ingestion.trigger.service.IngestionTriggerServiceTransactional;

public class TestInbox {

    @Mock
    SubmissionClient fakeKafkaClient;

    @Mock
    InboxAdapter fakeAdapter;

    @Mock
    InboxEntryRepository fakeRepo;

    @Mock
    ProcessConfiguration processConfiguration;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public final void testPoll_OnFindingNewProducts_ShallStoreProductsAndPutInKafkaQueue() throws IOException {

        when(processConfiguration.getHostname()).thenReturn("ingestor-01");
        when(fakeAdapter.read(any())).thenReturn(Arrays.asList(new InboxEntry("foo1", "foo1", "/tmp", new Date(), 10),
                new InboxEntry("foo2", "foo2", "/tmp", new Date(), 10)));
        when(fakeAdapter.description()).thenReturn("fakeAdapter");
        when(fakeAdapter.inboxURL()).thenReturn("/tmp");

        final Inbox uut = new Inbox(
                fakeAdapter,
                InboxFilter.ALLOW_ALL,
                new IngestionTriggerServiceTransactional(fakeRepo, processConfiguration),
                fakeKafkaClient,
                ProductFamily.EDRS_SESSION,
                "WILE",
                "NOMINAL",
                "FAST24",
                new FlatProductNameEvaluator());
        uut.poll();

        verify(fakeRepo, times(2)).save(any());
        verify(fakeKafkaClient, times(2)).publish(any());
    }

    @Test
    public final void testPoll_OnFindingAlreadyStoredProducts_ShallDoNothing() throws IOException {

        when(processConfiguration.getHostname()).thenReturn("ingestor-01");
        when(fakeAdapter.read(any())).thenReturn(Arrays.asList(
                new InboxEntry("foo1", "foo1", "/tmp", new Date(), 0, "ingestor-01"),
                new InboxEntry("foo2", "foo2", "/tmp", new Date(), 0, "ingestor-01")));
        when(fakeAdapter.description()).thenReturn("fakeAdapter");
        when(fakeAdapter.inboxURL()).thenReturn("/tmp");


        when(fakeRepo.findByProcessingPodAndPickupURLAndStationName(anyString(), anyString(), anyString()))
                .thenReturn(Arrays.asList(
                        new InboxEntry("foo2", "foo2", "/tmp", new Date(), 0, "ingestor-01"),
                        new InboxEntry("foo1", "foo1", "/tmp", new Date(), 0, "ingestor-01")));

        final Inbox uut = new Inbox(
                fakeAdapter,
                InboxFilter.ALLOW_ALL,
                new IngestionTriggerServiceTransactional(fakeRepo, processConfiguration),
                fakeKafkaClient,
                ProductFamily.EDRS_SESSION,
				"WILE",
				"NOMINAL",
                "FAST24",
        new FlatProductNameEvaluator());
        uut.poll();

        verify(fakeRepo, times(0)).save(any());
        verify(fakeKafkaClient, times(0)).publish(any());
    }
}
