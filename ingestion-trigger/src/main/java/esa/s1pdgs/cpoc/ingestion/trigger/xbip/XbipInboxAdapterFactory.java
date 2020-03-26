package esa.s1pdgs.cpoc.ingestion.trigger.xbip;

import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;

public class XbipInboxAdapterFactory implements InboxAdapterFactory {
	private final InboxEntryFactory inboxEntryFactory;

	public XbipInboxAdapterFactory(InboxEntryFactory inboxEntryFactory) {
		this.inboxEntryFactory = inboxEntryFactory;
	}

	@Override
	public InboxAdapter newInboxAdapter(String inboxPath, int productInDirectoryLevel) {
		return new XbipInboxAdapter(inboxPath, inboxEntryFactory, productInDirectoryLevel);
	}

}
