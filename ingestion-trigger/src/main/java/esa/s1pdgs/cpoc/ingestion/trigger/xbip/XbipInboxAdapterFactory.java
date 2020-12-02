package esa.s1pdgs.cpoc.ingestion.trigger.xbip;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.ingestion.trigger.config.InboxConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;
import esa.s1pdgs.cpoc.xbip.client.XbipClientFactory;

@Component
public class XbipInboxAdapterFactory implements InboxAdapterFactory {
	private final InboxEntryFactory inboxEntryFactory;
	private final XbipClientFactory xbipClientFactory;

	@Autowired
	public XbipInboxAdapterFactory(
			final InboxEntryFactory inboxEntryFactory,
			final XbipClientFactory xbipClientFactory
	) {
		this.inboxEntryFactory = inboxEntryFactory;
		this.xbipClientFactory = xbipClientFactory;
	}
	
	@Override
	public InboxAdapter newInboxAdapter(final URI inbox, final InboxConfiguration inboxConfig) {
		return new XbipInboxAdapter(
				inbox, 
				xbipClientFactory.newXbipClient(inbox), 
				inboxEntryFactory,
				inboxConfig.getStationName()
		);
	}

}
