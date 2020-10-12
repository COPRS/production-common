package esa.s1pdgs.cpoc.ingestion.trigger.auxip;

import static java.time.Duration.ofSeconds;

import java.net.URI;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.auxip.client.AuxipClient;
import esa.s1pdgs.cpoc.auxip.client.AuxipProductMetadata;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ingestion.trigger.config.AuxipConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.AbstractInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;

public class AuxipInboxAdapter extends AbstractInboxAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(AuxipInboxAdapter.class);

    private final AuxipConfiguration configuration;
    private final ProcessConfiguration processConfiguration;
    private final AuxipStateRepository repository;
    private final AuxipClient auxipClient;

    public AuxipInboxAdapter(final InboxEntryFactory inboxEntryFactory,
                             final AuxipConfiguration configuration,
                             final ProcessConfiguration processConfiguration,
                             final AuxipClient auxipClient,
                             final URI inboxURL,
                             final String stationName,
                             final AuxipStateRepository repository) {
        super(inboxEntryFactory, inboxURL, stationName);
        this.configuration = configuration;
        this.repository = repository;
        this.processConfiguration = processConfiguration;
        this.auxipClient = auxipClient;
    }

    @Override
    protected Stream<EntrySupplier> list() {
        final AuxipState state = retrieveState();

        TimeWindow timeWindow = timeWindowFrom(state);

        if(timeWindow.toCloseTo(LocalDateTime.now().minus(Duration.ofSeconds(configuration.getOffsetFromNowSec())))) {
            LOG.info("time window is to close to now, skipping");
            return Stream.empty();
        }

        Stream<AuxipProductMetadata> stream = StreamSupport.stream(
                new AuxipMetadataSpliterator(timeWindow, auxipClient, configuration.getMaxPageSize(), inboxURL),
                false);

        //TODO filter

        return stream.map(this::toEntrySupplier);
    }

    private EntrySupplier toEntrySupplier(AuxipProductMetadata metaData) {
        return new EntrySupplier(
                Paths.get(metaData.getProductName()),
                () -> toInboxEntry(metaData)
        );
    }

    private TimeWindow timeWindowFrom(AuxipState state) {
        LocalDateTime start = LocalDateTime.ofInstant(state.getNextWindowStart().toInstant(), ZoneId.of("UTC"))
                .minus(ofSeconds(configuration.getTimeWindowOverlapSec()));
        LocalDateTime stop = start.plus(Duration.ofSeconds(configuration.getTimeWindowSec()));

        return new TimeWindow(start, stop);

    }

    private InboxEntry toInboxEntry(final AuxipProductMetadata auxipMetadata) {
        return new InboxEntry(
                auxipMetadata.getProductName(),
                auxipMetadata.getProductName(),
                auxipMetadata.getProductName(),
                new Date(0),
                -1,
                processConfiguration.getHostname());
    }

    @Override
    public void advanceAfterPublish() {
        AuxipState auxipState = retrieveState();
        Instant nextStart = auxipState.getNextWindowStart().toInstant().plus(ofSeconds(configuration.getTimeWindowSec()));
        auxipState.setNextWindowStart(new Date(nextStart.toEpochMilli()));
        repository.save(auxipState);
    }

    private AuxipState retrieveState() {
        Optional<AuxipState> state = repository.findByProcessingPodAndPripUrl(processConfiguration.getHostname(), inboxURL());

        if (state.isPresent()) {
            return state.get();
        }

        AuxipState newState = new AuxipState();
        newState.setNextWindowStart(
                new Date(Instant.from(
                        ZonedDateTime.ofLocal(DateUtils.parse(configuration.getStart()), ZoneId.of("UTC"), ZoneOffset.UTC)).toEpochMilli()));
        newState.setPripUrl(inboxURL());
        newState.setProcessingPod(processConfiguration.getHostname());
        repository.save(newState);
        return newState;
    }

    private static final class TimeWindow {
        private final LocalDateTime start;
        private final LocalDateTime stop;

        public TimeWindow(LocalDateTime start, LocalDateTime stop) {
            this.start = start;
            this.stop = stop;
        }

        public boolean toCloseTo(LocalDateTime barrier) {
            return stop.isAfter(barrier);
        }

        @Override
        public String toString() {
            return "start " + start + ", stop " + stop;
        }
    }

    private static class AuxipMetadataSpliterator implements Spliterator<AuxipProductMetadata> {

        private final TimeWindow window;
        private final AuxipClient client;
        private final int pageSize;
        private final URI inboxUrl;

        private int offset;
        private final Deque<AuxipProductMetadata> collected = new ArrayDeque<>();

        public AuxipMetadataSpliterator(TimeWindow window, AuxipClient client, int pageSize, URI inboxUrl) {
            this.window = window;
            this.client = client;
            this.pageSize = pageSize;
            this.inboxUrl = inboxUrl;
        }

        @Override
        public boolean tryAdvance(Consumer<? super AuxipProductMetadata> action) {

            //query first to fill queue
            if (collected.isEmpty()) {
                collected.addAll(collect());
            }

            //if it`s still empty there will be no more entries
            if (collected.isEmpty()) {
                return false;
            }

            // otherwise provide an element
            action.accept(collected.poll());

            // if it was the last element, make a new query
            if (collected.isEmpty()) {
                collected.addAll(collect());
            }

            return !collected.isEmpty();
        }

        private List<AuxipProductMetadata> collect() {
            LOG.info("query auxip {} with window {} pageSize {} offset {}", inboxUrl, window, pageSize, offset);

            List<AuxipProductMetadata> metadata = client.getMetadata(window.start, window.stop, pageSize, offset);
            offset += pageSize;
            return metadata;
        }

        @Override
        public Spliterator<AuxipProductMetadata> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.ORDERED;
        }
    }


}
