package esa.s1pdgs.cpoc.inbox.polling;

import esa.s1pdgs.cpoc.inbox.polling.filter.InboxFilter;
import esa.s1pdgs.cpoc.inbox.polling.kafka.producer.SubmissionClient;

public class InboxPoller {	
	
	private final Inbox inbox;
	private final InboxFilter ignoreFilter;
	private final SubmissionClient client;
	
	public InboxPoller(Inbox inbox, InboxFilter ignoreFilter, SubmissionClient client) {
		super();
		this.inbox = inbox;
		this.ignoreFilter = ignoreFilter;
		this.client = client;
	}
}
