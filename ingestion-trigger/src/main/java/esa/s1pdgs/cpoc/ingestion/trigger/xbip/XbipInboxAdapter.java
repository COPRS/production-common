package esa.s1pdgs.cpoc.ingestion.trigger.xbip;

import java.io.IOException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.InboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;

public class XbipInboxAdapter implements InboxAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(XbipInboxAdapter.class);

	private final InboxEntryFactory inboxEntryFactory;
	private final String inboxDirectoryURI;
	private final int productInDirectoryLevel;

	public XbipInboxAdapter(String inboxDirectoryURI, InboxEntryFactory inboxEntryFactory,
			int productInDirectoryLevel) {

		this.inboxDirectoryURI = inboxDirectoryURI;
		this.inboxEntryFactory = inboxEntryFactory;
		this.productInDirectoryLevel = productInDirectoryLevel;
	}

	@Override
	public Collection<InboxEntry> read(InboxFilter filter) throws IOException {
		
		LOG.trace("Reading inbox XBIP directory '{}'", inboxDirectoryURI);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String description() {
		return String.format("Inbox at %s", inboxDirectoryURI);
	}

	@Override
	public String inboxPath() {
		return inboxDirectoryURI;
	}

}
