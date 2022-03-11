package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.metadata.PathMetadataExtractor;
import esa.s1pdgs.cpoc.ingestion.trigger.config.IngestionTriggerConfigurationProperties;
import esa.s1pdgs.cpoc.ingestion.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntryRepository;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.InboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.JoinedFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.MinimumModificationDateFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.Inbox.InboxReturnValue;
import esa.s1pdgs.cpoc.ingestion.trigger.name.FlatProductNameEvaluator;
import esa.s1pdgs.cpoc.ingestion.trigger.service.IngestionTriggerServiceTransactional;
import esa.s1pdgs.cpoc.message.MessageProducer;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

public class TestInbox {

    @Mock
    InboxAdapter fakeAdapter;
    
    @Mock
    InboxAdapter fakeAdapterThatSupportsProductFamily;

    @Mock
    InboxEntryRepository fakeRepo;

    IngestionTriggerConfigurationProperties configuration = new IngestionTriggerConfigurationProperties();
    
    @Mock
    ProcessConfiguration processConfiguration;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        fakeAdapterThatSupportsProductFamily = Mockito.mock(InboxAdapter.class, withSettings().extraInterfaces(SupportsProductFamily.class));
        
        configuration.setProcess(processConfiguration);
    }

    @Test
    public final void testPoll_OnFindingNewProducts_ShallStoreProductsAndPutInKafkaQueue() throws IOException {
    	final ProductFamily productFamily = ProductFamily.EDRS_SESSION;
        when(processConfiguration.getHostname()).thenReturn("ingestor-01");
        when(fakeAdapter.read(any())).thenReturn(Arrays.asList(new InboxEntry("foo1", "foo1", "/tmp", new Date(), 10, null, null, productFamily.name(), "WILE", "S1"),
                new InboxEntry("foo2", "foo2", "/tmp", new Date(), 10, null, null, productFamily.name(), "WILE", "S1")));
        when(fakeAdapter.description()).thenReturn("fakeAdapter");
        when(fakeAdapter.inboxURL()).thenReturn("/tmp");

        final Inbox uut = new Inbox(
                fakeAdapter,
                InboxFilter.ALLOW_ALL,
                new IngestionTriggerServiceTransactional(fakeRepo, configuration),
                productFamily,
                "S1",
                "WILE",
                0,
                "NOMINAL",
                "FAST24",
                new FlatProductNameEvaluator(),
                PathMetadataExtractor.NULL
        );
        uut.poll();

        verify(fakeRepo, times(2)).save(any());
    }

    @Test
    public final void testPoll_WithSupportForProductFamily_OnFindingAlreadyStoredProducts_ShallDoNothing() throws IOException {
    	final ProductFamily productFamily = ProductFamily.EDRS_SESSION;

        when(processConfiguration.getHostname()).thenReturn("ingestor-01");
        when(fakeAdapterThatSupportsProductFamily.read(any())).thenReturn(Arrays.asList(
                new InboxEntry("foo1", "foo1", "/tmp", new Date(), 0, "ingestor-01", null, productFamily.name(), "WILE", "S1"),
                new InboxEntry("foo2", "foo2", "/tmp", new Date(), 0, "ingestor-01", null, productFamily.name(), "WILE", "S1")));
        when(fakeAdapterThatSupportsProductFamily.description()).thenReturn("fakeAuxipInboxAdapter");
        when(fakeAdapterThatSupportsProductFamily.inboxURL()).thenReturn("/tmp");

        when(fakeRepo.findByProcessingPodAndPickupURLAndStationNameAndMissionIdAndProductFamily(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Arrays.asList(
                        new InboxEntry("foo2", "foo2", "/tmp", new Date(), 0, "ingestor-01", null, productFamily.name(), "WILE", "S1"),
                        new InboxEntry("foo1", "foo1", "/tmp", new Date(), 0, "ingestor-01", null, productFamily.name(), "WILE", "S1")));

        final Inbox uut = new Inbox(
        		fakeAdapterThatSupportsProductFamily,
                InboxFilter.ALLOW_ALL,
                new IngestionTriggerServiceTransactional(fakeRepo, configuration),
                productFamily,
				"S1",
                "WILE",
				0,
				"NOMINAL",
                "FAST24",
                new FlatProductNameEvaluator(),
                PathMetadataExtractor.NULL
        );
        uut.poll();

        verify(fakeAdapterThatSupportsProductFamily, times(1)).read(any());
        verify(fakeRepo, times(1)).findByProcessingPodAndPickupURLAndStationNameAndMissionIdAndProductFamily(anyString(), anyString(), anyString(), anyString(), anyString()); // only called when SupportsProductFamily
        verify(fakeRepo, times(0)).findByProcessingPodAndPickupURLAndStationNameAndMissionId(anyString(), anyString(), anyString(), anyString()); // only called when not SupportsProductFamily
        verify(fakeRepo, times(0)).save(any());
    }
    
    @Test
    public final void testPoll_WithoutSupportForProductFamily_OnFindingAlreadyStoredProducts_ShallDoNothing() throws IOException {
    	final ProductFamily productFamily = ProductFamily.EDRS_SESSION;

        when(processConfiguration.getHostname()).thenReturn("ingestor-01");
        when(fakeAdapter.read(any())).thenReturn(Arrays.asList(
                new InboxEntry("foo1", "foo1", "/tmp", new Date(), 0, "ingestor-01", null, productFamily.name(), "WILE", "S1"),
                new InboxEntry("foo2", "foo2", "/tmp", new Date(), 0, "ingestor-01", null, productFamily.name(), "WILE", "S1")));
        when(fakeAdapter.description()).thenReturn("fakeAdapter");
        when(fakeAdapter.inboxURL()).thenReturn("/tmp");


        when(fakeRepo.findByProcessingPodAndPickupURLAndStationNameAndMissionId(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Arrays.asList(
                        new InboxEntry("foo2", "foo2", "/tmp", new Date(), 0, "ingestor-01", null, productFamily.name(), "WILE", "S1"),
                        new InboxEntry("foo1", "foo1", "/tmp", new Date(), 0, "ingestor-01", null, productFamily.name(), "WILE", "S1")));

        final Inbox uut = new Inbox(
                fakeAdapter,
                InboxFilter.ALLOW_ALL,
                new IngestionTriggerServiceTransactional(fakeRepo, configuration),
                productFamily,
                "S1",
				"WILE",
				0,
				"NOMINAL",
                "FAST24",
                new FlatProductNameEvaluator(),
                PathMetadataExtractor.NULL
        );
        uut.poll();

        verify(fakeAdapter, times(1)).read(any());
        verify(fakeRepo, times(0)).findByProcessingPodAndPickupURLAndStationNameAndMissionIdAndProductFamily(anyString(), anyString(), anyString(), anyString(), anyString()); // only called when SupportsProductFamily
        verify(fakeRepo, times(1)).findByProcessingPodAndPickupURLAndStationNameAndMissionId(anyString(), anyString(), anyString(), anyString()); // only called when not SupportsProductFamily
        verify(fakeRepo, times(0)).save(any());
    }
    
    @Test
    public final void testHandleEntry_OnMatchingMinimumDateFilter_ShallIgnoreOlderEntries() {
    	when(processConfiguration.getHostname()).thenReturn("ingestor-01");
    	
    	final ProductFamily productFamily = ProductFamily.EDRS_SESSION;
    	 
        final Inbox uut = new Inbox(
                fakeAdapter,
                new JoinedFilter(new MinimumModificationDateFilter(new Date(123456))),
                new IngestionTriggerServiceTransactional(fakeRepo, configuration),
                productFamily,
                "S1",
				"WILE",
				0,
				"NOMINAL",
                "FAST24",
                new FlatProductNameEvaluator(),
                PathMetadataExtractor.NULL
        );
        
        // old entry shall be ignored
        final Optional<InboxReturnValue> ignored = uut.handleEntry(
        		new InboxEntry("foo1", "foo1", "/tmp", new Date(0), 1, "ingestor-01", null, productFamily.name(), "WILE", "S1")
        );
        // new entry shall be accepted
        final Optional<InboxReturnValue> accepted = uut.handleEntry(
        		new InboxEntry("foo2", "foo2", "/tmp", new Date(), 1, "ingestor-01", null, productFamily.name(), "WILE", "S1")
        );
        assertFalse(ignored.isPresent());
        assertTrue(accepted.isPresent());
    }
    
    @ParameterizedTest
	@MethodSource("inboxEntryProvider")
    public final void testAbsolutePathOf(final String url, final String relPath, final String expected) throws Exception {
    	final InboxEntry entry = new InboxEntry();
    	entry.setPickupURL(url);
    	entry.setRelativePath(relPath);
    	
        final Inbox uut = new Inbox(
                fakeAdapter,
                new JoinedFilter(new MinimumModificationDateFilter(new Date(123456))),
                new IngestionTriggerServiceTransactional(fakeRepo, configuration),
                ProductFamily.EDRS_SESSION,
                "S1",
				"WILE",
				0,
				"NOMINAL",
                "FAST24",
                new FlatProductNameEvaluator(),
                PathMetadataExtractor.NULL
        );
        assertEquals(expected, uut.absolutePathOf(entry));
    }

	static Stream<Arguments> inboxEntryProvider() {
        return Stream.of(
            Arguments.of(
            		"https://cgs10.sentinel1.eo.esa.int/NOMINAL", 
            		"S1A/DCS_01_S1A_20210109073807036058_dat/ch_2/DCS_01_S1A_20210109073807036058_ch2_DSDB_00019.raw",
            		"/NOMINAL/S1A/DCS_01_S1A_20210109073807036058_dat/ch_2/DCS_01_S1A_20210109073807036058_ch2_DSDB_00019.raw"
            ),
            Arguments.of(
            		"https://cgs10.sentinel1.eo.esa.int/NOMINAL/S1A/", 
            		"DCS_01_S1A_20210109073807036058_dat/ch_2/DCS_01_S1A_20210109073807036058_ch2_DSDB_00019.raw",
            		"/NOMINAL/S1A/DCS_01_S1A_20210109073807036058_dat/ch_2/DCS_01_S1A_20210109073807036058_ch2_DSDB_00019.raw"
            )
        );
    }
    
}
