package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.PositiveFileSizeFilter;

public class AbstractInboxAdapterTest {

    @Spy
    private AbstractInboxAdapter uut;

    @Mock
    private final ProcessConfiguration config = new ProcessConfiguration();

    @Before
    public void initMocks() {
        final InboxEntryFactory entryFactory = new InboxEntryFactoryImpl(config);

        uut = new AbstractInboxAdapter(entryFactory, URI.create("https://example.com/WILE"), "WILE", ProductFamily.AUXILIARY_FILE) {
            @Override
            protected Stream<EntrySupplier> list() {
                return Stream.of(
                        new EntrySupplier(Paths.get("/path/1"), () -> newInboxEntryWithSize(-1)),
                        new EntrySupplier(Paths.get("/path/1"), () -> newInboxEntryWithSize(0)),
                        new EntrySupplier(Paths.get("/path/1"), () -> newInboxEntryWithSize(1)));
            }
        };

        MockitoAnnotations.initMocks(this);
        Mockito.when(config.getHostname()).thenReturn("localhost");
    }

    private InboxEntry newInboxEntryWithSize(int size) {
        InboxEntry entry = new InboxEntry();
        entry.setSize(size);
        return entry;
    }

    @Test
    public void read() throws IOException {
        List<InboxEntry> inboxEntries = uut.read(new PositiveFileSizeFilter());
        assertThat(inboxEntries.size(), is((2)));
        inboxEntries.forEach(entry -> assertThat(entry.getSize(), greaterThan(-1L)));
    }

}