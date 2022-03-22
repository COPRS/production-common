package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import java.net.URI;

import esa.s1pdgs.cpoc.ingestion.trigger.config.InboxConfiguration;

public interface InboxAdapterFactory {

	InboxAdapter newInboxAdapter(final URI inbox, final InboxConfiguration inboxConfig);

}
