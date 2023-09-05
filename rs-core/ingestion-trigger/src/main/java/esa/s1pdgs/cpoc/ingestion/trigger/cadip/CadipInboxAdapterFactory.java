package esa.s1pdgs.cpoc.ingestion.trigger.cadip;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;

import de.werum.coprs.cadip.client.CadipClientFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.auxip.AuxipInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.config.InboxConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;

public class CadipInboxAdapterFactory implements InboxAdapterFactory {

	private final ProcessConfiguration processConfiguration;
	private final InboxEntryFactory inboxEntryFactory;
	private final CadipStateRepository cadipStateRepository;
	private final CadipSessionStateRepository cadipSessionStateRepository;
	private final CadipClientFactory clientFactory;

	@Autowired
	public CadipInboxAdapterFactory(final ProcessConfiguration processConfiguration,
			final InboxEntryFactory inboxEntryFactory, final CadipStateRepository cadipStateRepository,
			final CadipSessionStateRepository cadipSessionStateRepository, final CadipClientFactory clientFactory) {
		this.processConfiguration = processConfiguration;
		this.inboxEntryFactory = inboxEntryFactory;
		this.cadipStateRepository = cadipStateRepository;
		this.cadipSessionStateRepository = cadipSessionStateRepository;
		this.clientFactory = clientFactory;
	}

	@Override
	public InboxAdapter newInboxAdapter(URI inbox, InboxConfiguration inboxConfig) {
		return new CadipInboxAdapter(inboxEntryFactory, processConfiguration, cadipStateRepository,
				cadipSessionStateRepository, clientFactory.newCadipClient(inbox), inbox, inboxConfig.getStationName(),
				inboxConfig.getMissionId(), inboxConfig.getFamily());
	}

}
