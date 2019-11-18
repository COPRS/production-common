package esa.s1pdgs.cpoc.inbox.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.inbox.Inbox;
import esa.s1pdgs.cpoc.inbox.InboxFactory;
import esa.s1pdgs.cpoc.inbox.InboxPollingService;

@Configuration
public class InboxPollingServiceConfiguration {
	private static final Logger LOG = LoggerFactory.getLogger(InboxPollingServiceConfiguration.class);

	private final InboxPollingConfigurationProperties properties;
	private final InboxFactory inboxFactory;

	@Autowired
	public InboxPollingServiceConfiguration(final InboxPollingConfigurationProperties properties,
			// InboxFactory is autowired here without a qualifier because there is only one
			// implementation of it in the classpath. This needs to be changed in the future
			// when there are other types of inboxes available
			final InboxFactory inboxFactory) {
		this.properties = properties;
		this.inboxFactory = inboxFactory;
	}

	@Bean
	public InboxPollingService newInboxService() {
		final List<Inbox> inboxes = new ArrayList<>();

		for (InboxConfiguration c : properties.getPolling()) {
			try {
				inboxes.add(inboxFactory.newInbox(c));

			} catch (IllegalArgumentException | IOException e) {
				LOG.error(e.getMessage());
			}
		}
		return new InboxPollingService(inboxes);
	}
}
