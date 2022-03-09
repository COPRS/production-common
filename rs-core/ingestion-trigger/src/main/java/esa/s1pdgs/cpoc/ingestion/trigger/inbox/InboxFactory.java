package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.metadata.PathMetadataExtractor;
import esa.s1pdgs.cpoc.common.metadata.PathMetadataExtractorImpl;
import esa.s1pdgs.cpoc.ingestion.trigger.auxip.AuxipInboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.config.InboxConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.edip.EdipInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.edip.EdipInboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.BlacklistRegexRelativePathInboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.JoinedFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.MinimumModificationDateFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.WhitelistRegexRelativePathInboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.fs.FilesystemInboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.name.AuxipProductNameEvaluator;
import esa.s1pdgs.cpoc.ingestion.trigger.name.FlatProductNameEvaluator;
import esa.s1pdgs.cpoc.ingestion.trigger.name.ProductNameEvaluator;
import esa.s1pdgs.cpoc.ingestion.trigger.name.SessionProductNameEvaluator;
import esa.s1pdgs.cpoc.ingestion.trigger.service.IngestionTriggerServiceTransactional;
import esa.s1pdgs.cpoc.ingestion.trigger.xbip.XbipInboxAdapterFactory;
import esa.s1pdgs.cpoc.message.MessageProducer;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

@Component
public class InboxFactory {
	private final IngestionTriggerServiceTransactional ingestionTriggerServiceTransactional;
	private final FilesystemInboxAdapterFactory fileSystemInboxAdapterFactory;
	private final XbipInboxAdapterFactory xbipInboxAdapterFactory;
	private final AuxipInboxAdapterFactory auxipInboxAdapterFactory;
	private final EdipInboxAdapterFactory edipInboxAdapterFactory;

	@Autowired
	public InboxFactory(
			final IngestionTriggerServiceTransactional inboxPollingServiceTransactional,
			final FilesystemInboxAdapterFactory fileSystemInboxAdapterFactory,
			final XbipInboxAdapterFactory xbipInboxAdapterFactory,
			final AuxipInboxAdapterFactory auxipInboxAdapterFactory,
			final EdipInboxAdapterFactory edipInboxAdapterFactory
	) {
		this.ingestionTriggerServiceTransactional = inboxPollingServiceTransactional;
		this.fileSystemInboxAdapterFactory = fileSystemInboxAdapterFactory;
		this.xbipInboxAdapterFactory = xbipInboxAdapterFactory;
		this.auxipInboxAdapterFactory = auxipInboxAdapterFactory;
		this.edipInboxAdapterFactory = edipInboxAdapterFactory;
	}
	
	static final Date ignoreFilesBeforeDateFor(final InboxConfiguration config, final Date now) {
		final Date ignoreFilesBeforeDate = config.getIgnoreFilesBeforeDate();
		
		// S1PRO-2098: If the configured date is in the future, take the current date as the
		// reference value
		if (ignoreFilesBeforeDate.after(now)) {
			return now;
		}
		return ignoreFilesBeforeDate;				
	}
	
	public Inbox newInbox(final InboxConfiguration config) throws URISyntaxException {
		return new Inbox(
				newInboxAdapter(config),
				new JoinedFilter(
						new BlacklistRegexRelativePathInboxFilter(Pattern.compile(config.getIgnoreRegex())),
						new WhitelistRegexRelativePathInboxFilter(Pattern.compile(config.getMatchRegex())),
						new MinimumModificationDateFilter(ignoreFilesBeforeDateFor(config, new Date()))
				),
				ingestionTriggerServiceTransactional, 
				config.getFamily(),
				config.getMissionId(),
				config.getStationName(),
				config.getStationRetentionTime(),
				config.getMode(),
				config.getTimeliness(),
				newProductNameEvaluatorFor(config),
				newPathMetadataExtractor(config)
		);
	}
	
	// TODO / FIXME
	private final PathMetadataExtractor newPathMetadataExtractor(final InboxConfiguration config) {
		if (config.getPathPattern() == null) {
			return PathMetadataExtractor.NULL;
		}
		return new PathMetadataExtractorImpl(Pattern.compile(config.getPathPattern(), Pattern.CASE_INSENSITIVE),
				config.getPathMetadataElements());
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
	
	
	private final InboxAdapterFactory newInboxAdapterFactory(final String type, final String url) {
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
				config
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
