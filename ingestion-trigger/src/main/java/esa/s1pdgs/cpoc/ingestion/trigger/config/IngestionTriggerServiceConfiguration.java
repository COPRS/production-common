package esa.s1pdgs.cpoc.ingestion.trigger.config;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.Inbox;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.service.IngestionTriggerService;

@Configuration
public class IngestionTriggerServiceConfiguration {
	private static final Logger LOG = LoggerFactory.getLogger(IngestionTriggerServiceConfiguration.class);

	private final IngestionTriggerConfigurationProperties properties;
	private final InboxFactory inboxFactory;
	private final AppStatus status;

	@Autowired
	public IngestionTriggerServiceConfiguration(
			final IngestionTriggerConfigurationProperties properties,
			final InboxFactory inboxFactory,
			 final AppStatus status
	) {
		this.properties = properties;
		// InboxFactory is autowired here without a qualifier because there is only one
		// implementation of it in the classpath. This needs to be changed in the future
		// when there are other types of inboxes available
		this.inboxFactory = inboxFactory;
		this.status = status;
	}

	@Bean
	public IngestionTriggerService newInboxService() {
		final List<Inbox> inboxes = new ArrayList<>();

		for (final InboxConfiguration config : properties.getPolling()) {
			try {
				final Inbox newInbox = inboxFactory.newInbox(config);
				LOG.info("Adding new inbox to be polled: {}",newInbox);
				inboxes.add(newInbox);
			} catch (IllegalArgumentException | IOException | URISyntaxException e) {
				LOG.error(e.getMessage());
			}
		}
		return new IngestionTriggerService(inboxes, status);
	}	
}
