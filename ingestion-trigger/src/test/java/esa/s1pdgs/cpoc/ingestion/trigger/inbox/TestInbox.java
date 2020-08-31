package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntryRepository;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.InboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.JoinedFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.MinimumModificationDateFilter;
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
                new FlatProductNameEvaluator()
        );
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
                new FlatProductNameEvaluator()
        );
        uut.poll();

        verify(fakeRepo, times(0)).save(any());
        verify(fakeKafkaClient, times(0)).publish(any());
    }
    
    @Test
    public final void testHandleEntry_OnMatchingMinimumDateFilter_ShallIgnoreOlderEntries() {
    	when(processConfiguration.getHostname()).thenReturn("ingestor-01");
    	 
        final Inbox uut = new Inbox(
                fakeAdapter,
                new JoinedFilter(new MinimumModificationDateFilter(new Date(123456))),
                new IngestionTriggerServiceTransactional(fakeRepo, processConfiguration),
                fakeKafkaClient,
                ProductFamily.EDRS_SESSION,
				"WILE",
				"NOMINAL",
                "FAST24",
                new FlatProductNameEvaluator()
        );
        
        // old entry shall be ignored
        final Optional<InboxEntry> ignored = uut.handleEntry(
        		new InboxEntry("foo1", "foo1", "/tmp", new Date(0), 1, "ingestor-01")
        );
        // new entry shall be accepted
        final Optional<InboxEntry> accepted = uut.handleEntry(
        		new InboxEntry("foo2", "foo2", "/tmp", new Date(), 1, "ingestor-01")
        );
        assertEquals(false, ignored.isPresent());
        assertEquals(true, accepted.isPresent());
    }
}
