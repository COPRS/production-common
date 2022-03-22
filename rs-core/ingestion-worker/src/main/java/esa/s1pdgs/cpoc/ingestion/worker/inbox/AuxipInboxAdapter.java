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
