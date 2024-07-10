/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

        uut = new AbstractInboxAdapter(entryFactory, URI.create("https://example.com/WILE"), "WILE", null, ProductFamily.AUXILIARY_FILE) {
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