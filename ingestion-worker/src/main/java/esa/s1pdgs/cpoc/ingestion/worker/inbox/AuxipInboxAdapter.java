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
    public List<InboxAdapterEntry> read(URI uri, String name) {
        // https://prip.sentinel1.eo.esa.int:443/prif/odata/S1A_OPER_AUX_RESORB_OPOD_20201008T162440_V20201008T122310_20201008T154040.EOF.zip
        final AuxipClient auxipClient = clientFactory.newAuxipClient(uri);
        final InputStream in = auxipClient.read(UUID.fromString(name));
        return Collections.singletonList(new InboxAdapterEntry(name, in, -1));
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
