package esa.s1pdgs.cpoc.inbox.config;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.inbox.Inbox;
import esa.s1pdgs.cpoc.inbox.InboxFactory;
import esa.s1pdgs.cpoc.inbox.InboxPollingService;

@Configuration
public class InboxPollingServiceConfiguration {
	private final InboxPollingConfigurationProperties properties;
	private final InboxFactory inboxFactory;

	@Autowired
	public InboxPollingServiceConfiguration(
			final InboxPollingConfigurationProperties properties,
			// InboxFactory is autowired here without a qualifier because there is only one
			// implementation of it in the classpath. This needs to be changed in the future 
			// when there are other types of inboxes available
			final InboxFactory inboxFactory
	) {
		this.properties = properties;
		this.inboxFactory = inboxFactory;
	}
	
	@Bean
	public InboxPollingService newInboxService() {
		final List<Inbox> inboxes = properties.getPolling().stream()
				.map(c -> inboxFactory.newInbox(c))
				.collect(Collectors.toList());
		
		return new InboxPollingService(inboxes);
	}
}
