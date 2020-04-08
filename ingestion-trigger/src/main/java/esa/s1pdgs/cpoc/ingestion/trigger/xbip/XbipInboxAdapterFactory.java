package esa.s1pdgs.cpoc.ingestion.trigger.xbip;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapterFactory;
import esa.s1pdgs.cpoc.xbip.client.XbipClientFactory;

@Component
public class XbipInboxAdapterFactory implements InboxAdapterFactory {
	private final XbipInboxEntryFactory inboxEntryFactory;
	private final XbipClientFactory xbipClientFactory;

	@Autowired
	public XbipInboxAdapterFactory(
			final XbipInboxEntryFactory inboxEntryFactory,
			final XbipClientFactory xbipClientFactory
	) {
		this.inboxEntryFactory = inboxEntryFactory;
		this.xbipClientFactory = xbipClientFactory;
	}

	@Override
	public InboxAdapter newInboxAdapter(final URI inbox, final int productInDirectoryLevel,	final String stationName) {
		return new XbipInboxAdapter(
				inbox, 
				xbipClientFactory.newXbipClient(inbox), 
				inboxEntryFactory,
				productInDirectoryLevel,
				stationName
		);
	}
}
