package esa.s1pdgs.cpoc.ingestion.trigger.xbip;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
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
		final URI inboxUri = ensureEndsWithSlash(inbox);		
		return new XbipInboxAdapter(
				inbox, 
				xbipClientFactory.newXbipClient(inboxUri), 
				inboxEntryFactory,
				inboxConfig.getStationName(),
				inboxConfig.getMissionId(),
				inboxConfig.getFamily()
		);
	}
	
	
	// apparently, webdav requires a trailing slash
	private final URI ensureEndsWithSlash(final URI serverUrl) {
		if (!serverUrl.getPath().endsWith("/")) {
			try {
				return new URIBuilder(serverUrl)
						.setPath(serverUrl.getPath() + "/")
						.build();
			} catch (final URISyntaxException e) {
				throw new RuntimeException(
						"Error handling URI " + serverUrl, 
						e
				);
			}
		}
		return serverUrl;		
	}

}
