package esa.s1pdgs.cpoc.ingestion.trigger;

import java.io.IOException;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.ingestion.trigger.config.InboxConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.BlacklistRegexRelativePathInboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.JoinedFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.WhitelistRegexRelativePathInboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.kafka.producer.KafkaSubmissionClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

@Component
public class InboxFactory {
	private final KafkaTemplate<String, IngestionJob> kafkaTemplate;
	private final InboxAdapterFactory inboxAdapterFactory;
	private final IngestionTriggerServiceTransactional ingestionTriggerServiceTransactional;

	@Autowired
	public InboxFactory(
			final KafkaTemplate<String, IngestionJob> kafkaTemplate,
			final IngestionTriggerServiceTransactional inboxPollingServiceTransactional,
			final InboxAdapterFactory inboxAdapterFactory
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.ingestionTriggerServiceTransactional = inboxPollingServiceTransactional;
		this.inboxAdapterFactory = inboxAdapterFactory;
	}

	public Inbox newInbox(final InboxConfiguration config) throws IOException {
		return new Inbox(
				inboxAdapterFactory.newInboxAdapter(config.getDirectory(), config.getProductInDirectoryLevel()), 
				new JoinedFilter(
						new BlacklistRegexRelativePathInboxFilter(Pattern.compile(config.getIgnoreRegex())),
						new WhitelistRegexRelativePathInboxFilter(Pattern.compile(config.getMatchRegex()))
				),
				ingestionTriggerServiceTransactional, 
				new KafkaSubmissionClient(kafkaTemplate, config.getTopic())
		);
	}	
}
