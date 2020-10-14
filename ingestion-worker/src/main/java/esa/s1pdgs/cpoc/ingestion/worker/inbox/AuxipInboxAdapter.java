package esa.s1pdgs.cpoc.ingestion.worker.inbox;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import esa.s1pdgs.cpoc.auxip.client.AuxipClient;
import esa.s1pdgs.cpoc.auxip.client.AuxipClientFactory;

public class AuxipInboxAdapter implements InboxAdapter {
    private final AuxipClientFactory clientFactory;

    public AuxipInboxAdapter(AuxipClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public List<InboxAdapterEntry> read(final URI uri, final String name, final long size) {
        final AuxipClient auxipClient = clientFactory.newAuxipClient(uri);
        final InputStream in = auxipClient.read(UUID.fromString(name));
        return Collections.singletonList(new InboxAdapterEntry(name, in, size));
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
