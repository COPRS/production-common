package esa.s1pdgs.cpoc.inbox.polling.filter;

import esa.s1pdgs.cpoc.inbox.polling.InboxEntry;

final class FakeInboxEntry implements InboxEntry {
	private final String name;
	
	FakeInboxEntry(String name) {
		this.name = name;
	}
	@Override
	public final String getName() {
		return name;
	}
	
	@Override
	public String getUrl() {
		return "test://" + name;
	}		
}