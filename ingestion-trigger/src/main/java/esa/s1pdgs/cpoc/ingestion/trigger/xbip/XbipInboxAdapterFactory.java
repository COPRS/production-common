package esa.s1pdgs.cpoc.ingestion.trigger.xbip;

import static esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxURIScheme.HTTPS;

import java.io.File;
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
	public XbipInboxAdapterFactory(
			final XbipInboxEntryFactory inboxEntryFactory,
			final XbipClientFactory xbipClientFactory
	) {
		this.inboxEntryFactory = inboxEntryFactory;
		this.xbipClientFactory = xbipClientFactory;
	}

	@Override
	public InboxAdapter newInboxAdapter(final String inboxPath, final int productInDirectoryLevel) {
		try {
			return new XbipInboxAdapter(
					new File(inboxPath.replace(HTTPS.getSchemeWithSlashes(), "")).toPath(),
					this.xbipClientFactory.newXbipClient(new URI(inboxPath)), 
					inboxEntryFactory,
					productInDirectoryLevel
			);
		} catch (final URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
