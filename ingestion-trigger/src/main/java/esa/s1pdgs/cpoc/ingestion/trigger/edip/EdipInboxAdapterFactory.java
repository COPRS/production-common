package esa.s1pdgs.cpoc.ingestion.trigger.edip;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.ebip.client.EdipClientFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;

@Component
public class EdipInboxAdapterFactory implements InboxAdapterFactory {

	private final InboxEntryFactory inboxEntryFactory;
	private final EdipClientFactory edipClientFactory;

	@Autowired
	public EdipInboxAdapterFactory(final InboxEntryFactory inboxEntryFactory,
			final EdipClientFactory edipClientFactory) {
		this.inboxEntryFactory = inboxEntryFactory;
		this.edipClientFactory = edipClientFactory;
	}

	@Override
	public InboxAdapter newInboxAdapter(final URI inbox, final String stationName) {
		return new EdipInboxAdapter(inbox, edipClientFactory.newEdipClient(inbox), inboxEntryFactory, stationName);
	}

}
