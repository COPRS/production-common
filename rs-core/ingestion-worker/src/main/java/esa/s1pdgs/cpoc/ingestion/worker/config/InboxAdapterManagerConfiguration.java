package esa.s1pdgs.cpoc.ingestion.worker.config;

import static esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapterManager.uriRegexFor;
import static esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapterManager.uriRegexForFile;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.werum.coprs.cadip.client.CadipClientFactory;
import de.werum.coprs.cadip.client.config.CadipClientConfigurationProperties;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.auxip.client.AuxipClientFactory;
import esa.s1pdgs.cpoc.auxip.client.config.AuxipClientConfigurationProperties;
import esa.s1pdgs.cpoc.ebip.client.EdipClientFactory;
import esa.s1pdgs.cpoc.ebip.client.config.EdipClientConfigurationProperties;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.AuxipInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.CadipInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.EdipInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.FilesystemInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapter;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapterManager;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.XbipInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.worker.product.ProductService;
import esa.s1pdgs.cpoc.ingestion.worker.product.ProductServiceImpl;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.xbip.client.XbipClientFactory;
import esa.s1pdgs.cpoc.xbip.client.config.XbipClientConfigurationProperties;

@Configuration
public class InboxAdapterManagerConfiguration {

	private final XbipClientFactory xbipClientFactory;
	private final AuxipClientFactory auxipClientFactory;
	private final EdipClientFactory edipClientFactory;
	private final CadipClientFactory cadipClientFactory;
	private final IngestionWorkerServiceConfigurationProperties properties;
	private final AuxipClientConfigurationProperties auxipClientConfigurationProperties;
	private final XbipClientConfigurationProperties xbipClientConfigurationProperties;
	private final EdipClientConfigurationProperties edipClientConfigurationProperties;
	private final CadipClientConfigurationProperties cadipClientConfigurationProperties;
	
	@Autowired
	public InboxAdapterManagerConfiguration(
			final XbipClientFactory xbipClientFactory,
			final AuxipClientFactory auxipClientFactory,
			final EdipClientFactory edipClientFactory,
			final CadipClientFactory cadipClientFactory,
			final IngestionWorkerServiceConfigurationProperties properties,
			final AuxipClientConfigurationProperties auxipClientConfigurationProperties,
			final XbipClientConfigurationProperties xbipClientConfigurationProperties,
			final EdipClientConfigurationProperties edipClientConfigurationProperties,
			final CadipClientConfigurationProperties cadipClientConfigurationProperties) {
		this.xbipClientFactory = xbipClientFactory;
		this.auxipClientFactory = auxipClientFactory;
		this.edipClientFactory = edipClientFactory;
		this.cadipClientFactory = cadipClientFactory;
		this.properties = properties;
		this.auxipClientConfigurationProperties = auxipClientConfigurationProperties;
		this.xbipClientConfigurationProperties = xbipClientConfigurationProperties;
		this.edipClientConfigurationProperties = edipClientConfigurationProperties;
		this.cadipClientConfigurationProperties = cadipClientConfigurationProperties;
	}

	@Bean
	public InboxAdapterManager inboxAdapterManager() {
		final Map<String, InboxAdapter> inboxAdapter = new HashMap<>();

		final InboxAdapter xbip = new XbipInboxAdapter(xbipClientFactory);
		final InboxAdapter auxip = new AuxipInboxAdapter(auxipClientFactory);
		final InboxAdapter edip = new EdipInboxAdapter(edipClientFactory);
		final InboxAdapter file = new FilesystemInboxAdapter(properties);
		final InboxAdapter cadip = new CadipInboxAdapter(cadipClientFactory);

		//TODO replace this mechanism by adding the inbox type to IngestionJob and evaluate it here
		xbipClientConfigurationProperties.getHostConfigs().values()
				.forEach(xbipHost -> inboxAdapter.put(uriRegexFor(xbipHost.getServerName()), xbip));

		auxipClientConfigurationProperties.getHostConfigs().values()
				.forEach(auxipHost -> inboxAdapter.put(uriRegexFor(auxipHost.getServiceRootUri()), auxip));
		
		edipClientConfigurationProperties.getHostConfigs().values()
			.forEach(edipHost -> inboxAdapter.put(uriRegexFor(edipHost.getServerName()), edip));

		cadipClientConfigurationProperties.getHostConfigs().values()
			.forEach(cadipHost -> inboxAdapter.put(uriRegexFor(cadipHost.getServiceRootUri()), cadip));
		
		inboxAdapter.put(uriRegexForFile(), file);

		return new InboxAdapterManager(inboxAdapter);
	}
	
	@Bean
	@Autowired
	public ProductService productService(
			final ObsClient obsClient,
			final AppStatus appStatus) {
		return new ProductServiceImpl(obsClient, properties.isBufferInputs(), appStatus);
	}
}
