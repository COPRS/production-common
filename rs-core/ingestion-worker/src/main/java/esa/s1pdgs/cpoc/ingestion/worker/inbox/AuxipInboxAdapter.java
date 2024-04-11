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

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.UUID;

import esa.s1pdgs.cpoc.auxip.client.AuxipClient;
import esa.s1pdgs.cpoc.auxip.client.AuxipClientFactory;

public class AuxipInboxAdapter implements InboxAdapter {
    private final AuxipClientFactory clientFactory;

    public AuxipInboxAdapter(AuxipClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public InboxAdapterResponse read(final URI uri, final String name, final String relativePath, final long size) {
        final AuxipClient auxipClient = clientFactory.newAuxipClient(uri);
        final InputStream in = auxipClient.read(UUID.fromString(name));
        return new InboxAdapterResponse(Collections.singletonList(new InboxAdapterEntry(relativePath, in, size)), auxipClient);
    }

    @Override
    public void delete(URI uri) {
        //nothing to do for auxip
    }

    @Override
    public String toString() {
        return "AuxipInboxAdapter";
    }
}
