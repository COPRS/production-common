package esa.s1pdgs.cpoc.inbox.polling.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.inbox.polling.InboxAdapterFactory;
import esa.s1pdgs.cpoc.inbox.polling.InboxEntry;

@Component
public class PickupContentConverter {		
	private final InboxAdapterFactory inboxAdapterFactory;
	
	@Autowired
	public PickupContentConverter(InboxAdapterFactory inboxAdapterFactory) {
		this.inboxAdapterFactory = inboxAdapterFactory;
	}

	public final InboxEntry toInboxEntry(final PickupContent content) {
		return inboxAdapterFactory.newInboxEntry(content.getUrl());
	}
	
	public final PickupContent toPickupContent(final InboxEntry inboxEntry) {
		return new PickupContent(inboxEntry.getUrl());
	}
}
