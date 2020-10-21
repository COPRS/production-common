package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.trigger.auxip.AuxipInboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.config.InboxConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.edip.EdipInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.edip.EdipInboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.BlacklistRegexRelativePathInboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.JoinedFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.MinimumModificationDateFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.WhitelistRegexRelativePathInboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.fs.FilesystemInboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.kafka.producer.KafkaSubmissionClient;
import esa.s1pdgs.cpoc.ingestion.trigger.name.AuxipProductNameEvaluator;
import esa.s1pdgs.cpoc.ingestion.trigger.name.FlatProductNameEvaluator;
import esa.s1pdgs.cpoc.ingestion.trigger.name.ProductNameEvaluator;
import esa.s1pdgs.cpoc.ingestion.trigger.name.SessionProductNameEvaluator;
import esa.s1pdgs.cpoc.ingestion.trigger.service.IngestionTriggerServiceTransactional;
import esa.s1pdgs.cpoc.ingestion.trigger.xbip.XbipInboxAdapterFactory;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

@Component
public class InboxFactory {
	private final KafkaTemplate<String, IngestionJob> kafkaTemplate;
	private final IngestionTriggerServiceTransactional ingestionTriggerServiceTransactional;
	private final FilesystemInboxAdapterFactory fileSystemInboxAdapterFactory;
	private final XbipInboxAdapterFactory xbipInboxAdapterFactory;
	private final AuxipInboxAdapterFactory auxipInboxAdapterFactory;
	private final EdipInboxAdapterFactory edipInboxAdapterFactory;

	@Autowired
	public InboxFactory(
			final KafkaTemplate<String, IngestionJob> kafkaTemplate,
			final IngestionTriggerServiceTransactional inboxPollingServiceTransactional,
			final FilesystemInboxAdapterFactory fileSystemInboxAdapterFactory,
			final XbipInboxAdapterFactory xbipInboxAdapterFactory,
			final AuxipInboxAdapterFactory auxipInboxAdapterFactory,
			final EdipInboxAdapterFactory edipInboxAdapterFactory
			) {
		this.kafkaTemplate = kafkaTemplate;
		this.ingestionTriggerServiceTransactional = inboxPollingServiceTransactional;
		this.fileSystemInboxAdapterFactory = fileSystemInboxAdapterFactory;
		this.xbipInboxAdapterFactory = xbipInboxAdapterFactory;
		this.auxipInboxAdapterFactory = auxipInboxAdapterFactory;
		this.edipInboxAdapterFactory = edipInboxAdapterFactory;
	}
	

	
	public Inbox newInbox(final InboxConfiguration config) throws IOException, URISyntaxException {		
		return new Inbox(
				newInboxAdapter(config),
				new JoinedFilter(
						new BlacklistRegexRelativePathInboxFilter(Pattern.compile(config.getIgnoreRegex())),
						new WhitelistRegexRelativePathInboxFilter(Pattern.compile(config.getMatchRegex())),
						new MinimumModificationDateFilter(config.getIgnoreFilesBeforeDate())
				),
				ingestionTriggerServiceTransactional, 
				new KafkaSubmissionClient(kafkaTemplate, config.getTopic()),
				config.getFamily(),
				config.getStationName(),
				config.getMode(),
				config.getTimeliness(),
				newProductNameEvaluatorFor(config)				
		);
	}
	
	private final String normalizeInputUrl(final String configuredUrl) {
		String result = configuredUrl;
		
		if (configuredUrl.startsWith("/")) {
			result = "file://" + configuredUrl;
		}		
		if (configuredUrl.endsWith("/")) {
			result = configuredUrl.substring(0, configuredUrl.length()-1);
		}
		return result;		
	}
	
	
	private final InboxAdapterFactory newInboxAdapterFactory(final String type, final String url) throws URISyntaxException {
		if("prip".equals(type)) {
			return auxipInboxAdapterFactory;
		}
		
		if (EdipInboxAdapter.INBOX_TYPE.equals(type)) {
			return edipInboxAdapterFactory;
		}

		if (url.startsWith("https://")) {
			return xbipInboxAdapterFactory;			
		}
		else if (url.startsWith("file://")) {
			return fileSystemInboxAdapterFactory; 
		}
		throw new IllegalArgumentException(
				String.format("URI scheme not supported for URI %s", url)
		);
	}
	
	private final InboxAdapter newInboxAdapter(final InboxConfiguration config) throws URISyntaxException {		
		final String sanitizedUrl = normalizeInputUrl(config.getDirectory());
		final InboxAdapterFactory inboxAdapterFactory = newInboxAdapterFactory(config.getType(), sanitizedUrl);
		
		return inboxAdapterFactory.newInboxAdapter(
				new URI(sanitizedUrl), 
				config.getStationName()
		);
	}
	
	final ProductNameEvaluator newProductNameEvaluatorFor(final InboxConfiguration config) {
		if("prip".equals(config.getType())) {
			return new AuxipProductNameEvaluator();
		}


		if (config.getFamily() == ProductFamily.EDRS_SESSION 
				|| config.getFamily() == ProductFamily.SESSION_RETRANSFER) {
			return new SessionProductNameEvaluator(
					Pattern.compile(config.getSessionNamePattern(), Pattern.CASE_INSENSITIVE), 
					config.getSessionNameGroupIndex()
			);			
		}
		return new FlatProductNameEvaluator();
	}
}
