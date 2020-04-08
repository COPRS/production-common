package esa.s1pdgs.cpoc.ingestion.worker.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.ingestion.worker.inbox.FilesystemInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapterManager;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.XbipInboxAdapter;
import esa.s1pdgs.cpoc.xbip.client.XbipClientFactory;

@Configuration
public class InboxAdapterManagerConfiguration {
	private final XbipClientFactory xbipClientFactory;
	private final IngestionWorkerServiceConfigurationProperties properties;

	@Autowired
	public InboxAdapterManagerConfiguration(
			final XbipClientFactory xbipClientFactory,
			final IngestionWorkerServiceConfigurationProperties properties
	) {
		this.xbipClientFactory = xbipClientFactory;
		this.properties = properties;
	}

	@Bean
	public InboxAdapterManager inboxAdapterManager() {
		final Map<String, InboxAdapter> inboxAdapter = new HashMap<>(2);
		inboxAdapter.put("file", new FilesystemInboxAdapter(properties));
		inboxAdapter.put("https", new XbipInboxAdapter(xbipClientFactory));		
		return new InboxAdapterManager(inboxAdapter);		
	}
}
