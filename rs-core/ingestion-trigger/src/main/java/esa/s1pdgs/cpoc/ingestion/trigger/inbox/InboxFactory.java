/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.metadata.PathMetadataExtractor;
import esa.s1pdgs.cpoc.common.metadata.PathMetadataExtractorImpl;
import esa.s1pdgs.cpoc.ingestion.trigger.auxip.AuxipInboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.cadip.CadipInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.cadip.CadipInboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.config.InboxConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.edip.EdipInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.edip.EdipInboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.BlacklistRegexRelativePathInboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.JoinedFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.MinimumModificationDateFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.WhitelistRegexRelativePathInboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.fs.FilesystemInboxAdapterFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.name.AuxipProductNameEvaluator;
import esa.s1pdgs.cpoc.ingestion.trigger.name.DirectoryProductNameEvaluator;
import esa.s1pdgs.cpoc.ingestion.trigger.name.FlatProductNameEvaluator;
import esa.s1pdgs.cpoc.ingestion.trigger.name.ProductNameEvaluator;
import esa.s1pdgs.cpoc.ingestion.trigger.name.SessionProductNameEvaluator;
import esa.s1pdgs.cpoc.ingestion.trigger.service.IngestionTriggerServiceTransactional;
import esa.s1pdgs.cpoc.ingestion.trigger.xbip.XbipInboxAdapterFactory;

@Component
public class InboxFactory {
	private static final Logger logger = LoggerFactory.getLogger(InboxFactory.class);
	
	private final IngestionTriggerServiceTransactional ingestionTriggerServiceTransactional;
	private final FilesystemInboxAdapterFactory fileSystemInboxAdapterFactory;
	private final XbipInboxAdapterFactory xbipInboxAdapterFactory;
	private final AuxipInboxAdapterFactory auxipInboxAdapterFactory;
	private final EdipInboxAdapterFactory edipInboxAdapterFactory;
	private final CadipInboxAdapterFactory cadipInboxAdapterFactory;
	private final CommonConfigurationProperties commonProperties;

	@Autowired
	public InboxFactory(
			final IngestionTriggerServiceTransactional inboxPollingServiceTransactional,
			final FilesystemInboxAdapterFactory fileSystemInboxAdapterFactory,
			final XbipInboxAdapterFactory xbipInboxAdapterFactory,
			final AuxipInboxAdapterFactory auxipInboxAdapterFactory,
			final EdipInboxAdapterFactory edipInboxAdapterFactory,
			final CadipInboxAdapterFactory cadipInboxAdapterFactory,
			final CommonConfigurationProperties commonProperties
	) {
		this.ingestionTriggerServiceTransactional = inboxPollingServiceTransactional;
		this.fileSystemInboxAdapterFactory = fileSystemInboxAdapterFactory;
		this.xbipInboxAdapterFactory = xbipInboxAdapterFactory;
		this.auxipInboxAdapterFactory = auxipInboxAdapterFactory;
		this.edipInboxAdapterFactory = edipInboxAdapterFactory;
		this.cadipInboxAdapterFactory = cadipInboxAdapterFactory;
		this.commonProperties = commonProperties;
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
				newPathMetadataExtractor(config),
				commonProperties
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
			logger.info("InboxAdapterFactory returning a auxip inbox");
			return auxipInboxAdapterFactory;
		}
		
		if (EdipInboxAdapter.INBOX_TYPE.equals(type)) {
			logger.info("InboxAdapterFactory returning a edip inbox");
			return edipInboxAdapterFactory;
		}
		
		if (CadipInboxAdapter.INBOX_TYPE.equals(type)) {
			logger.info("InboxAdapterFactory returning a cadip inbox");
			return cadipInboxAdapterFactory;
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
		if ("directory".equals(config.getType())) {
			return new DirectoryProductNameEvaluator();
		}
		if ("prip".equals(config.getType())) {
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
