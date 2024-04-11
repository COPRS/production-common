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
        adapterMap.put(InboxAdapterManager.uriRegexFor("${cgs1.host}"), xbipAdapter);
        adapterMap.put(InboxAdapterManager.uriRegexFor("prip.sentinel1.eo.esa.int"), auxipAdapter);
        adapterMap.put(InboxAdapterManager.uriRegexFor("https://prip.sentinel1.eo.esa.int:443/prif/odata"), auxipAdapter);

        final InboxAdapterManager uut = new InboxAdapterManager(adapterMap);

        assertThat(uut.getInboxAdapterFor(URI.create("file://path/to/file")), is(fileAdapter));
        assertThat(uut.getInboxAdapterFor(URI.create("file:/data/inbox/AUX/S1A_OPER_AUX_PREORB_OPOD_20200121T194542_V20200121T174401_20200122T001901.EOF")), is(fileAdapter));
        assertThat(uut.getInboxAdapterFor(URI.create("https://esa-copernicus.ksat.no/SENTINEL1/WILE/S1A_XXXABC.EOF")), is(xbipAdapter));
        assertThat(uut.getInboxAdapterFor(URI.create("http://195.77.80.11/SENTINEL1/WILE/S1A_XXXABC.SAFE")), is(xbipAdapter));
        assertThat(uut.getInboxAdapterFor(URI.create("195.77.80.11/SENTINEL1/WILE/S1A_XXXABC.SAFE")), is(xbipAdapter));
        assertThat(uut.getInboxAdapterFor(URI.create("http://195.77.80.11:8181/SENTINEL1/WILE/S1A_XXXABC.SAFE")), is(xbipAdapter));
        assertThat(uut.getInboxAdapterFor(URI.create("https://prip.sentinel1.eo.esa.int/prif/odata/Products?$format=json")), is(auxipAdapter));
        assertThat(uut.getInboxAdapterFor(URI.create("http://prip.sentinel1.eo.esa.int/prif/odata/Products?$format=json")), is(auxipAdapter));
        assertThat(uut.getInboxAdapterFor(URI.create("http://prip.sentinel1.eo.esa.int:9090/prif/odata/Products?$format=json")), is(auxipAdapter));
        assertThat(uut.getInboxAdapterFor(URI.create("https://prip.sentinel1.eo.esa.int:443/prif/odata/S1__AUX_WND_V20201009T180000_G20201008T060301.SAFE.zip")), is(auxipAdapter));

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