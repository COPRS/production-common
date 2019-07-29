package esa.s1pdgs.cpoc.inbox;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.inbox.config.InboxConfiguration;
import esa.s1pdgs.cpoc.inbox.filter.BlacklistRegexNameInboxFilter;
import esa.s1pdgs.cpoc.inbox.kafka.producer.KafkaSubmissionClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;

@Component
public class InboxFactory {	
	private final KafkaTemplate<String, IngestionDto> kafkaTemplate;
	private final InboxAdapterFactory inboxAdapterFactory;
	private final InboxPollingServiceTransactional inboxPollingServiceTransactional;
		
	@Autowired
	public InboxFactory(
			final KafkaTemplate<String, IngestionDto> kafkaTemplate,
			final InboxPollingServiceTransactional inboxPollingServiceTransactional,
			final InboxAdapterFactory inboxAdapterFactory
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.inboxPollingServiceTransactional = inboxPollingServiceTransactional;
		this.inboxAdapterFactory = inboxAdapterFactory;
	}

	public Inbox newInbox(InboxConfiguration config)
	{
		return new Inbox(
				inboxAdapterFactory.newInboxAdapter(config.getDirectory()), 
				new BlacklistRegexNameInboxFilter(Pattern.compile(config.getIgnoreRegex())), 
				inboxPollingServiceTransactional,
				new KafkaSubmissionClient(kafkaTemplate, config.getTopic())
		);
	}

}
