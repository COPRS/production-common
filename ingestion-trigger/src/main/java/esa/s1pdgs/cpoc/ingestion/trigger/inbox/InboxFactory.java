package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.ingestion.trigger.config.InboxConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.BlacklistRegexRelativePathInboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.JoinedFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.WhitelistRegexRelativePathInboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.fs.FilesystemInboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.fs.FilesystemInboxEntryFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.kafka.producer.KafkaSubmissionClient;
import esa.s1pdgs.cpoc.ingestion.trigger.service.IngestionTriggerServiceTransactional;
import esa.s1pdgs.cpoc.ingestion.trigger.xbip.XbipInboxAdapterFactory;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.xbip.client.XbipClientFactory;

@Component
public class InboxFactory {
	private final KafkaTemplate<String, IngestionJob> kafkaTemplate;
	private final IngestionTriggerServiceTransactional ingestionTriggerServiceTransactional;
	private final FilesystemInboxAdapterFactory fileSystemInboxAdapterFactory;
	private final XbipInboxAdapterFactory xbipInboxAdapterFactory;

	@Autowired
	public InboxFactory(final KafkaTemplate<String, IngestionJob> kafkaTemplate,
			final IngestionTriggerServiceTransactional inboxPollingServiceTransactional,
			final FilesystemInboxAdapterFactory fileSystemInboxAdapterFactory,
			final XbipInboxAdapterFactory xbipInboxAdapterFactory) {
		this.kafkaTemplate = kafkaTemplate;
		this.ingestionTriggerServiceTransactional = inboxPollingServiceTransactional;
		this.fileSystemInboxAdapterFactory = fileSystemInboxAdapterFactory;
		this.xbipInboxAdapterFactory = xbipInboxAdapterFactory;
	}

	public Inbox newInbox(final InboxConfiguration config) throws IOException, URISyntaxException {

		URI pollingDirectoryURI = new URI(config.getDirectory());
		InboxAdapter inboxAdapter;
		if (pollingDirectoryURI.getScheme().equalsIgnoreCase(InboxURIScheme.FILE.getScheme())) {
			inboxAdapter = fileSystemInboxAdapterFactory.newInboxAdapter(config.getDirectory(),
					config.getProductInDirectoryLevel());
		} else if (pollingDirectoryURI.getScheme().equalsIgnoreCase(InboxURIScheme.HTTPS.getScheme())) {
			inboxAdapter = xbipInboxAdapterFactory.newInboxAdapter(config.getDirectory(),
					config.getProductInDirectoryLevel());
		} else {
			throw new IllegalArgumentException(
					String.format("URI scheme not supported %s", pollingDirectoryURI.getScheme()));
		}

		return new Inbox(inboxAdapter,
				new JoinedFilter(new BlacklistRegexRelativePathInboxFilter(Pattern.compile(config.getIgnoreRegex())),
						new WhitelistRegexRelativePathInboxFilter(Pattern.compile(config.getMatchRegex()))),
				ingestionTriggerServiceTransactional, new KafkaSubmissionClient(kafkaTemplate, config.getTopic()),
				config.getFamily());
	}
}
