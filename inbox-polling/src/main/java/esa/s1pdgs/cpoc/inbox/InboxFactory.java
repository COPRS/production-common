package esa.s1pdgs.cpoc.inbox;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.inbox.config.InboxConfiguration;
import esa.s1pdgs.cpoc.inbox.entity.InboxEntryRepository;
import esa.s1pdgs.cpoc.inbox.filter.BlacklistRegexNameInboxFilter;
import esa.s1pdgs.cpoc.inbox.kafka.producer.KafkaSubmissionClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;

@Component
public class InboxFactory {	
	private final KafkaTemplate<String, IngestionDto> kafkaTemplate;
	private final InboxEntryRepository pickupContentRepository;
	private final InboxAdapterFactory inboxAdapterFactory;
		
	@Autowired
	public InboxFactory(
			final KafkaTemplate<String, IngestionDto> kafkaTemplate,
			final InboxEntryRepository pickupContentRepository,
			final InboxAdapterFactory inboxAdapterFactory
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.pickupContentRepository = pickupContentRepository;
		this.inboxAdapterFactory = inboxAdapterFactory;
	}

	public Inbox newInbox(InboxConfiguration config)
	{
		return new Inbox(
				inboxAdapterFactory.newInboxAdapter(config.getDirectory()), 
				new BlacklistRegexNameInboxFilter(Pattern.compile(config.getIgnoreRegex())), 
				pickupContentRepository,
				new KafkaSubmissionClient(kafkaTemplate, config.getTopic())
		);
	}

}
