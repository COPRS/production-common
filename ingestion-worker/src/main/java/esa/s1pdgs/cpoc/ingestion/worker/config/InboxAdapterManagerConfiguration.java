package esa.s1pdgs.cpoc.ingestion.worker.config;

import static esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapterManager.uriRegexFor;
import static esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapterManager.uriRegexForFile;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.auxip.client.AuxipClientFactory;
import esa.s1pdgs.cpoc.auxip.client.config.AuxipClientConfigurationProperties;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.AuxipInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.FilesystemInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapterManager;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.XbipInboxAdapter;
import esa.s1pdgs.cpoc.xbip.client.XbipClientFactory;
import esa.s1pdgs.cpoc.xbip.client.config.XbipClientConfigurationProperties;

@Configuration
public class InboxAdapterManagerConfiguration {

	private final XbipClientFactory xbipClientFactory;
	private final AuxipClientFactory auxipClientFactory;
	private final IngestionWorkerServiceConfigurationProperties properties;
	private final AuxipClientConfigurationProperties auxipClientConfigurationProperties;
	private final XbipClientConfigurationProperties xbipClientConfigurationProperties;

	@Autowired
	public InboxAdapterManagerConfiguration(
			final XbipClientFactory xbipClientFactory,
			AuxipClientFactory auxipClientFactory, final IngestionWorkerServiceConfigurationProperties properties,
			AuxipClientConfigurationProperties auxipClientConfigurationProperties, XbipClientConfigurationProperties xbipClientConfigurationProperties) {
		this.xbipClientFactory = xbipClientFactory;
		this.auxipClientFactory = auxipClientFactory;
		this.properties = properties;
		this.auxipClientConfigurationProperties = auxipClientConfigurationProperties;
		this.xbipClientConfigurationProperties = xbipClientConfigurationProperties;
	}

	@Bean
	public InboxAdapterManager inboxAdapterManager() {
		final Map<String, InboxAdapter> inboxAdapter = new HashMap<>();

		final InboxAdapter xbip = new XbipInboxAdapter(xbipClientFactory);
		final InboxAdapter auxip = new AuxipInboxAdapter(auxipClientFactory);
		final InboxAdapter file = new FilesystemInboxAdapter(properties);

		//TODO replace this mechanism by adding the inbox type to IngestionJob and evaluate it here
		xbipClientConfigurationProperties.getHostConfigs()
				.forEach(xbipHost -> inboxAdapter.put(uriRegexFor(xbipHost.getServerName()), xbip));

		auxipClientConfigurationProperties.getHostConfigs()
				.forEach(auxipHost -> inboxAdapter.put(uriRegexFor(auxipHost.getServiceRootUri()), auxip));

		inboxAdapter.put(uriRegexForFile(), file);

		return new InboxAdapterManager(inboxAdapter);
	}



}
