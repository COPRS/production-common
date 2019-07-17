package esa.s1pdgs.cpoc.inbox;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.inbox.config.InboxConfiguration;
import esa.s1pdgs.cpoc.inbox.polling.InboxAdapterFactory;
import esa.s1pdgs.cpoc.inbox.polling.filter.BlacklistRegexNameInboxFilter;
import esa.s1pdgs.cpoc.inbox.polling.kafka.producer.KafkaSubmissionClient;
import esa.s1pdgs.cpoc.inbox.polling.repo.PickupContentConverter;
import esa.s1pdgs.cpoc.inbox.polling.repo.PickupContentRepository;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;

@Component
public class InboxFactory {	
	private final KafkaTemplate<String, IngestionDto> kafkaTemplate;
	private final PickupContentRepository pickupContentRepository;
	private final PickupContentConverter converter;
	private final InboxAdapterFactory inboxAdapterFactory;
		
	@Autowired
	public InboxFactory(
			final KafkaTemplate<String, IngestionDto> kafkaTemplate,
			final PickupContentRepository pickupContentRepository,
			final PickupContentConverter converter,
			final InboxAdapterFactory inboxAdapterFactory
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.pickupContentRepository = pickupContentRepository;
		this.converter = converter;
		this.inboxAdapterFactory = inboxAdapterFactory;
	}

	public Inbox newInbox(InboxConfiguration config)
	{
		return new Inbox(
				inboxAdapterFactory.newInboxAdapter(config.getDirectory()), 
				new BlacklistRegexNameInboxFilter(Pattern.compile(config.getIgnoreRegex())), 
				pickupContentRepository,
				converter,
				new KafkaSubmissionClient(kafkaTemplate, config.getTopic())
		);
	}

}
