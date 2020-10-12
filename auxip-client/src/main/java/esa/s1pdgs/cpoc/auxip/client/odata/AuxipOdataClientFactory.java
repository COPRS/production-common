package esa.s1pdgs.cpoc.auxip.client.odata;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.client.core.http.ProxyWrappingHttpClientFactory;

import esa.s1pdgs.cpoc.auxip.client.AuxipClient;
import esa.s1pdgs.cpoc.auxip.client.AuxipClientFactory;
import esa.s1pdgs.cpoc.auxip.client.config.AuxipClientConfigurationProperties;
import esa.s1pdgs.cpoc.auxip.client.config.AuxipClientConfigurationProperties.AuxipHostConfiguration;

public class AuxipOdataClientFactory implements AuxipClientFactory {
	private static final Logger LOG = LogManager.getLogger(AuxipOdataClientFactory.class);
	
	/** OData entity set name for the entities that will be queried */
	public static final String ENTITY_SET_NAME = "Products";

	private final AuxipClientConfigurationProperties config;

	// --------------------------------------------------------------------------

	public AuxipOdataClientFactory(final AuxipClientConfigurationProperties config) {
		this.config = config;
	}

	// --------------------------------------------------------------------------

	@Override
	public AuxipClient newAuxipClient(final URI serverUrl) {
		// TODO @MSc: impl
		final AuxipHostConfiguration hostConfig = this.hostConfigFor(serverUrl.getHost());
		final ODataClient odataClient = ODataClientFactory.getClient();
		
		// TODO @MSc: add proxy configuration and when configured use it here for the client
//		if(proxy) {
//			final ProxyWrappingHttpClientFactory clientFactory = new ProxyWrappingHttpClientFactory(proxy, proxyUsername, proxyPassword, wrapped);
//			odataClient.getConfiguration().setHttpClientFactory(clientFactory);
//		}
		
		return new AuxipOdataClient(odataClient, hostConfig, ENTITY_SET_NAME);
	}
	
	private AuxipHostConfiguration hostConfigFor(final String serviceRootUri) {
		// lookup host configuration for the given URL
		for (final AuxipHostConfiguration hostConfig : this.config.getHostConfigs()) {
			if (serviceRootUri.equals(hostConfig.getServiceRootUri())) {
				LOG.trace("Found config {}", hostConfig);
				return hostConfig;
			}
		}
		throw new IllegalArgumentException(String.format("Could not find configuration for server '%s'", serviceRootUri));
	}

//	private ProxySelector proxy() {
//		if (null != this.config.getProxyHost()) {
//			LOG.debug("Using Proxy {}:{}", config.getProxyHost(), this.config.getProxyPort());
//			return new ProxySelector() {
//				@Override
//				public final List<Proxy> select(final URI uri) {
//					try {
//						return Collections.singletonList(new Proxy(Type.HTTP,
//								new InetSocketAddress(
//										InetAddress.getByName(AuxipOdataClientFactory.this.config.getProxyHost()),
//										config.getProxyPort())));
//						// FIXME proper error handling
//					} catch (final UnknownHostException e) {
//						LOG.error(e);
//					}
//					return Collections.emptyList();
//				}
//
//				@Override
//				public final void connectFailed(final URI uri, final SocketAddress sa, final IOException ioe) {
//					LOG.error(ioe);
//				}
//			};
//		}
//
//		return null;
//	}

}
