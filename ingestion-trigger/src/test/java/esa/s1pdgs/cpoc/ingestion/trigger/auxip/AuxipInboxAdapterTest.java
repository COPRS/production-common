package esa.s1pdgs.cpoc.ingestion.trigger.auxip;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.auxip.client.AuxipClient;
import esa.s1pdgs.cpoc.auxip.client.AuxipProductMetadata;
import esa.s1pdgs.cpoc.ingestion.trigger.config.AuxipConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.AbstractInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;

public class AuxipInboxAdapterTest {

    @Mock
    InboxEntryFactory inboxEntryFactory;

    @Mock
    AuxipConfiguration auxipConfiguration;

    @Mock
    ProcessConfiguration processConfiguration;

    @Mock
    AuxipClient auxipClient;

    @Mock
    AuxipStateRepository auxipRepository;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void list() {
        final LocalDateTime start = LocalDateTime.parse("2020-01-01T01:00:00.000");

        final LocalDateTime expectedStart = start.minus(Duration.ofSeconds(24));
        final LocalDateTime expectedStop = expectedStart.plus(Duration.ofSeconds(235));

        when(processConfiguration.getHostname()).thenReturn("localhost");
        when(auxipConfiguration.getMaxPageSize()).thenReturn(3);
        when(auxipConfiguration.getOffsetFromNowSec()).thenReturn(1000);
        when(auxipConfiguration.getTimeWindowOverlapSec()).thenReturn(24);
        when(auxipConfiguration.getTimeWindowSec()).thenReturn(235);
        when(auxipRepository.findByProcessingPodAndPripUrl("localhost", "https://auxip")).thenReturn(auxipState(start));
        when(auxipClient.getMetadata(eq(expectedStart), eq(expectedStop), eq(3), eq(0)))
                .thenReturn(metadata("one", "two", "three"));
        when(auxipClient.getMetadata(eq(expectedStart), eq(expectedStop), eq(3), eq(3)))
                .thenReturn(metadata("four", "five", "six"));
        when(auxipClient.getMetadata(eq(expectedStart), eq(expectedStop), eq(3), eq(6)))
                .thenReturn(metadata("seven", "eight"));
        when(auxipClient.getMetadata(eq(expectedStart), eq(expectedStop), eq(3), eq(9)))
                .thenReturn(emptyList());

        final AuxipInboxAdapter uut = new AuxipInboxAdapter(
                inboxEntryFactory,
                auxipConfiguration,
                processConfiguration,
                auxipClient,
                URI.create("https://auxip"),
                "WILE",
                auxipRepository);


        assertThat(uut.list().collect(toList()), matches("one", "two", "three", "four", "five", "six", "seven", "eight"));

        verify(auxipClient).getMetadata(eq(expectedStart), eq(expectedStop), eq(3), eq(0));
        verify(auxipClient).getMetadata(eq(expectedStart), eq(expectedStop), eq(3), eq(3));
        verify(auxipClient).getMetadata(eq(expectedStart), eq(expectedStop), eq(3), eq(6));
    }

    @Test
    public void listInitialWithNoState() {
        final LocalDateTime start = LocalDateTime.parse("1978-08-08T14:00:00.000");

        final LocalDateTime expectedStart = start.minus(Duration.ofSeconds(24));
        final LocalDateTime expectedStop = expectedStart.plus(Duration.ofSeconds(235));

        when(processConfiguration.getHostname()).thenReturn("localhost");
        when(auxipConfiguration.getMaxPageSize()).thenReturn(3);
        when(auxipConfiguration.getOffsetFromNowSec()).thenReturn(1000);
        when(auxipConfiguration.getTimeWindowOverlapSec()).thenReturn(24);
        when(auxipConfiguration.getTimeWindowSec()).thenReturn(235);
        when(auxipConfiguration.getStart()).thenReturn("1978-08-08T14:00:00.000Z");
        when(auxipRepository.findByProcessingPodAndPripUrl("localhost", "https://auxip")).thenReturn(empty());
        when(auxipClient.getMetadata(eq(expectedStart), eq(expectedStop), eq(3), eq(0)))
                .thenReturn(metadata("one", "two", "three"));
        when(auxipClient.getMetadata(eq(expectedStart), eq(expectedStop), eq(3), eq(3)))
                .thenReturn(metadata("four", "five", "six"));
        when(auxipClient.getMetadata(eq(expectedStart), eq(expectedStop), eq(3), eq(6)))
                .thenReturn(metadata("seven", "eight"));
        when(auxipClient.getMetadata(eq(expectedStart), eq(expectedStop), eq(3), eq(9)))
                .thenReturn(emptyList());

        final AuxipInboxAdapter uut = new AuxipInboxAdapter(
                inboxEntryFactory,
                auxipConfiguration,
                processConfiguration,
                auxipClient,
                URI.create("https://auxip"),
                "WILE",
                auxipRepository);


        assertThat(uut.list().collect(toList()), matches("one", "two", "three", "four", "five", "six", "seven", "eight"));

        verify(auxipClient).getMetadata(eq(expectedStart), eq(expectedStop), eq(3), eq(0));
        verify(auxipClient).getMetadata(eq(expectedStart), eq(expectedStop), eq(3), eq(3));
        verify(auxipClient).getMetadata(eq(expectedStart), eq(expectedStop), eq(3), eq(6));
    }

    @Test
    public void listTimeWindowTooCloseToNow() {
        final LocalDateTime start = LocalDateTime.now().minus(Duration.ofSeconds(235 + 500));

        when(processConfiguration.getHostname()).thenReturn("localhost");
        when(auxipConfiguration.getMaxPageSize()).thenReturn(3);
        when(auxipConfiguration.getOffsetFromNowSec()).thenReturn(1000);
        when(auxipConfiguration.getTimeWindowOverlapSec()).thenReturn(24);
        when(auxipConfiguration.getTimeWindowSec()).thenReturn(235);
        when(auxipRepository.findByProcessingPodAndPripUrl("localhost", "https://auxip")).thenReturn(auxipState(start));

        final AuxipInboxAdapter uut = new AuxipInboxAdapter(
                inboxEntryFactory,
                auxipConfiguration,
                processConfiguration,
                auxipClient,
                URI.create("https://auxip"),
                "WILE",
                auxipRepository);


        assertThat(uut.list().count(), is(0L));

        verify(auxipClient, times(0)).getMetadata(any(), any(), any(), any());
    }

    private Optional<AuxipState> auxipState(final LocalDateTime start) {
        final long millis = ZonedDateTime.ofLocal(start, ZoneId.of("UTC"), null).toInstant().toEpochMilli();
        AuxipState auxipState = new AuxipState();
        auxipState.setNextWindowStart(new Date(millis));
        auxipState.setProcessingPod("localhost");
        auxipState.setPripUrl("https://auxip");
        return Optional.of(
                auxipState);
    }

    private Matcher<List<AbstractInboxAdapter.EntrySupplier>> matches(String... ids) {
        return new CustomMatcher<List<AbstractInboxAdapter.EntrySupplier>>("matches " + asList(ids)) {
            @Override
            public boolean matches(Object o) {
                @SuppressWarnings("unchecked")
                List<AbstractInboxAdapter.EntrySupplier> actual = (List<AbstractInboxAdapter.EntrySupplier>) o;
                List<String> actualMapped = actual.stream().map(entry -> entry.getEntry().getName()).collect(toList());

                return actualMapped.equals(asList(ids));
            }
        };
    }

    private List<AuxipProductMetadata> metadata(String... ids) {
        return stream(ids).map(id -> new AuxipProductMetadata() {
            @Override
            public String getProductName() {
                return id;
            }

            @Override
            public LocalDateTime getCreationDate() {
                return LocalDateTime.now();
            }

            @Override
            public List<String> getParsingErrors() {
                return emptyList();
            }

            @Override
            public UUID getId() {
                return UUID.randomUUID();
            }
        }).collect(toList());
    }

    @Test
    public void advanceAfterPublish() {

        final LocalDateTime start = LocalDateTime.parse("1978-08-08T14:00:00.000");
        final LocalDateTime expectedNewStart = start.plus(Duration.ofSeconds(25489584));

        when(auxipConfiguration.getTimeWindowSec()).thenReturn(25489584);
        when(processConfiguration.getHostname()).thenReturn("localhost");
        when(auxipRepository.findByProcessingPodAndPripUrl("localhost", "https://auxip")).thenReturn(auxipState(start));

        AuxipInboxAdapter uut = new AuxipInboxAdapter(
                inboxEntryFactory,
                auxipConfiguration,
                processConfiguration,
                auxipClient,
                URI.create("https://auxip"),
                "WILE",
                auxipRepository);


        uut.advanceAfterPublish();

        verify(auxipRepository).save(argThat(isAuxipStateWithDate(expectedNewStart)));
    }

    private ArgumentMatcher<AuxipState> isAuxipStateWithDate(final LocalDateTime expectedStart) {
        return auxipState -> {
            Date expected = new Date(expectedStart.toInstant(ZoneOffset.UTC).toEpochMilli());
            return auxipState.getNextWindowStart().equals(expected);
        };
    }
}