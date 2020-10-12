package esa.s1pdgs.cpoc.ingestion.trigger.auxip;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.auxip.client.AuxipClientFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.config.AuxipConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;

@Component
public class AuxipInboxAdapterFactory implements InboxAdapterFactory {

    private final AuxipConfiguration configuration;
    private final ProcessConfiguration processConfiguration;
    private final InboxEntryFactory inboxEntryFactory;
    private final AuxipStateRepository repository;
    private final AuxipClientFactory clientFactory;

    @Autowired
    public AuxipInboxAdapterFactory(final AuxipConfiguration configuration,
                                    final ProcessConfiguration processConfiguration,
                                    final InboxEntryFactory inboxEntryFactory,
                                    final AuxipStateRepository repository, AuxipClientFactory clientFactory) {
        this.configuration = configuration;
        this.inboxEntryFactory = inboxEntryFactory;
        this.repository = repository;
        this.processConfiguration = processConfiguration;
        this.clientFactory = clientFactory;
    }

    @Override
    public InboxAdapter newInboxAdapter(URI inbox, String stationName) {
        return new AuxipInboxAdapter(
                inboxEntryFactory,
                configuration,
                processConfiguration,
                clientFactory.newAuxipClient(inbox),
                inbox,
                stationName,
                repository);
    }
}
