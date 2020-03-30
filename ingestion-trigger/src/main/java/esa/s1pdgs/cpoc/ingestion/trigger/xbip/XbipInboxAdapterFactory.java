package esa.s1pdgs.cpoc.ingestion.trigger.xbip;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;
import esa.s1pdgs.cpoc.xbip.client.XbipClientFactory;

@Component
public class XbipInboxAdapterFactory implements InboxAdapterFactory {
	private final InboxEntryFactory inboxEntryFactory;
	private final XbipClientFactory xbipClientFactory;

	@Autowired
	public XbipInboxAdapterFactory(final InboxEntryFactory inboxEntryFactory,
			final XbipClientFactory xbipClientFactory) {
		this.inboxEntryFactory = inboxEntryFactory;
		this.xbipClientFactory = xbipClientFactory;
	}

	@Override
	public InboxAdapter newInboxAdapter(final String inboxURL, final int productInDirectoryLevel) {
		try {
			URI inbox = new URI(inboxURL);
			return new XbipInboxAdapter(inbox, this.xbipClientFactory.newXbipClient(inbox), inboxEntryFactory,
					productInDirectoryLevel);
		} catch (final URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
