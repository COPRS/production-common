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
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import esa.s1pdgs.cpoc.auxip.client.AuxipClient;
import esa.s1pdgs.cpoc.auxip.client.AuxipProductMetadata;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ingestion.trigger.config.AuxipConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.AbstractInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;

public class AuxipInboxAdapter extends AbstractInboxAdapter {

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

        return Stream.generate(
                new AuxipMetadataSupplier(timeWindow, configuration.getMaxPageSize(), auxipClient))
                .map(this::toEntrySupplier);
    }

    private EntrySupplier toEntrySupplier(AuxipProductMetadata metaData) {
        return new EntrySupplier(
                Paths.get(metaData.getProductName()),
                () -> toInboxEntry(metaData)
        );
    }

    private TimeWindow timeWindowFrom(AuxipState state) {
        LocalDateTime start = LocalDateTime.from(state.getNextWindowStart().toInstant())
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
    }

    private static class AuxipMetadataSupplier implements Supplier<AuxipProductMetadata> {

        private final TimeWindow window;
        private final int pageSize;
        private final AuxipClient client;

        private int offset = 0;
        private final Deque<AuxipProductMetadata> collected = new ArrayDeque<>();

        public AuxipMetadataSupplier(TimeWindow window, int pageSize, AuxipClient client) {
            this.window = window;
            this.pageSize = pageSize;
            this.client = client;
        }

        @Override
        public AuxipProductMetadata get() {
            if (collected.isEmpty()) {
                collect();
            }

            if (collected.isEmpty()) {
                return null;
            }

            return collected.poll();
        }

        private void collect() {
            collected.addAll(client.getMetadata(window.start, window.stop, pageSize, offset));
            offset += pageSize;
        }
    }


}
