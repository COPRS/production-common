package esa.s1pdgs.cpoc.auxip.client.odata;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.client.core.http.BasicAuthHttpClientFactory;
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
		final AuxipHostConfiguration hostConfig = this.hostConfigFor(serverUrl.getHost());
		final ODataClient odataClient = this.buildOdataClient(hostConfig);
		
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
	
	private ODataClient buildOdataClient(final AuxipHostConfiguration hostConfig) {
		final ODataClient odataClient = ODataClientFactory.getClient();

		// authentication
		BasicAuthHttpClientFactory basicAuthHttpClientFactory = null;
		if (null != hostConfig.getUser() && !hostConfig.getUser().isEmpty()) {
			basicAuthHttpClientFactory = new BasicAuthHttpClientFactory(hostConfig.getUser(), hostConfig.getPass());
		}

		// proxy
		ProxyWrappingHttpClientFactory proxyWrappingHttpClientFactory = null;
		if (null != this.config.getProxy() && !this.config.getProxy().isEmpty()) {
			URI proxyUri = null;
			try {
				proxyUri = URI.create(this.config.getProxy());
			} catch (Exception e) {
				LOG.error("could not build proxy URI from '" + this.config.getProxy() + "' and will not use proxy: "
						+ e.getMessage());
			}

			if (null != proxyUri) {
				if (null != basicAuthHttpClientFactory) {
					proxyWrappingHttpClientFactory = new ProxyWrappingHttpClientFactory(proxyUri,
							basicAuthHttpClientFactory);
				} else {
					proxyWrappingHttpClientFactory = new ProxyWrappingHttpClientFactory(proxyUri);
				}
			}
		}

		if (null != proxyWrappingHttpClientFactory) {
			odataClient.getConfiguration().setHttpClientFactory(proxyWrappingHttpClientFactory);
		} else if (null != basicAuthHttpClientFactory) {
			odataClient.getConfiguration().setHttpClientFactory(basicAuthHttpClientFactory);
		}

		return odataClient;
	}

}
