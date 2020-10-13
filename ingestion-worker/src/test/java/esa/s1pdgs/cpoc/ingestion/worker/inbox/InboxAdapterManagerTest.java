package esa.s1pdgs.cpoc.ingestion.worker.inbox;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class InboxAdapterManagerTest {

    @Mock
    InboxAdapter xbipAdapter;

    @Mock
    InboxAdapter auxipAdapter;

    @Mock
    InboxAdapter fileAdapter;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getInboxAdapterFor() {

        Map<String, InboxAdapter> adapterMap = new HashMap<>();

        adapterMap.put(InboxAdapterManager.uriRegexForFile(), fileAdapter);
        adapterMap.put(InboxAdapterManager.uriRegexFor("esa-copernicus.ksat.no"), xbipAdapter);
        adapterMap.put(InboxAdapterManager.uriRegexFor("195.77.80.11"), xbipAdapter);
        adapterMap.put(InboxAdapterManager.uriRegexFor("prip.sentinel1.eo.esa.int"), auxipAdapter);

        final InboxAdapterManager uut = new InboxAdapterManager(adapterMap);

        assertThat(uut.getInboxAdapterFor(URI.create("file://path/to/file")), is(fileAdapter));
        assertThat(uut.getInboxAdapterFor(URI.create("https://esa-copernicus.ksat.no/SENTINEL1/WILE/S1A_XXXABC.EOF")), is(xbipAdapter));
        assertThat(uut.getInboxAdapterFor(URI.create("http://195.77.80.11/SENTINEL1/WILE/S1A_XXXABC.SAFE")), is(xbipAdapter));
        assertThat(uut.getInboxAdapterFor(URI.create("195.77.80.11/SENTINEL1/WILE/S1A_XXXABC.SAFE")), is(xbipAdapter));
        assertThat(uut.getInboxAdapterFor(URI.create("http://195.77.80.11:8181/SENTINEL1/WILE/S1A_XXXABC.SAFE")), is(xbipAdapter));
        assertThat(uut.getInboxAdapterFor(URI.create("https://prip.sentinel1.eo.esa.int/prif/odata/Products?$format=json")), is(auxipAdapter));
        assertThat(uut.getInboxAdapterFor(URI.create("http://prip.sentinel1.eo.esa.int/prif/odata/Products?$format=json")), is(auxipAdapter));
        assertThat(uut.getInboxAdapterFor(URI.create("http://prip.sentinel1.eo.esa.int:9090/prif/odata/Products?$format=json")), is(auxipAdapter));
    }

    @Test
    public void getInboxAdapterForWithUnknownUri() {

        Map<String, InboxAdapter> adapterMap = new HashMap<>();

        adapterMap.put(InboxAdapterManager.uriRegexForFile(), fileAdapter);
        adapterMap.put(InboxAdapterManager.uriRegexFor("esa-copernicus.ksat.no"), xbipAdapter);
        adapterMap.put(InboxAdapterManager.uriRegexFor("195.77.80.11"), xbipAdapter);
        adapterMap.put(InboxAdapterManager.uriRegexFor("prip.sentinel1.eo.esa.int"), auxipAdapter);

        final InboxAdapterManager uut = new InboxAdapterManager(adapterMap);

        assertThrows(IllegalArgumentException.class, () -> uut.getInboxAdapterFor(URI.create("195.45.80.11/SENTINEL1/WILE/S1A_XXXABC.SAFE")));
    }
}