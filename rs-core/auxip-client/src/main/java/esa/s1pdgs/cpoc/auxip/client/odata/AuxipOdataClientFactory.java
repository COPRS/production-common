package esa.s1pdgs.cpoc.auxip.client.odata;

import java.net.URI;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.client.core.http.ProxyWrappingHttpClientFactory;
import esa.s1pdgs.cpoc.auxip.client.AuxipClient;
import esa.s1pdgs.cpoc.auxip.client.AuxipClientFactory;
import esa.s1pdgs.cpoc.auxip.client.config.AuxipClientConfigurationProperties;
import esa.s1pdgs.cpoc.auxip.client.config.AuxipClientConfigurationProperties.AuxipHostConfiguration;
import esa.s1pdgs.cpoc.common.utils.StringUtil;

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
		final AuxipHostConfiguration hostConfig = this.hostConfigFor(serverUrl.toString());
		final ODataClient odataClient = this.newOdataClient(hostConfig);

		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(
			AuthScope.ANY,
		    new UsernamePasswordCredentials(hostConfig.getUser(), hostConfig.getPass())
		);
		
		// preemptive auth
		final HttpClientContext context = HttpClientContext.create();
		final AuthCache cache = new BasicAuthCache();

		cache.put(new HttpHost(serverUrl.getHost(), serverUrl.getPort(), "https"), new BasicScheme());
		context.setAuthCache(cache);
		context.setCredentialsProvider(credentialsProvider);

		return new AuxipOdataClient(
				odataClient, 
				hostConfig, 
				ENTITY_SET_NAME, 
				newDownloadClient(hostConfig,credentialsProvider), 
				context
		);
	}

	private AuxipHostConfiguration hostConfigFor(final String serviceRootUri) {
		// lookup host configuration for the given URL
		for (final AuxipHostConfiguration hostConfig : this.config.getHostConfigs()) {
			if (this.urisEqual(serviceRootUri, hostConfig.getServiceRootUri())) {
				LOG.trace("Found config {}", hostConfig);
				return hostConfig;
			}
		}
		throw new IllegalArgumentException(
				String.format("Could not find configuration for server '%s'", serviceRootUri));
	}

	private boolean urisEqual(final String serviceRootUri1, final String serviceRootUri2) {
		return StringUtil.removeTrailing(serviceRootUri1, "/").equals(StringUtil.removeTrailing(serviceRootUri2, "/"));
	}

	private CloseableHttpClient newDownloadClient(final AuxipHostConfiguration hostConfig,
			final CredentialsProvider credentials) {
		try {
			final HttpClientBuilder clientBuilder = HttpClientBuilder.create();
			clientBuilder.setDefaultCredentialsProvider(credentials);

			final SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(new TrustSelfSignedStrategy());
			final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					builder.build(),
			        NoopHostnameVerifier.INSTANCE
			);
			clientBuilder.setSSLSocketFactory(sslsf);

			return clientBuilder.build();
		} catch (final Exception e) {
			// FIXME error handling
			throw new RuntimeException(e);
		}
	}

	private ODataClient newOdataClient(final AuxipHostConfiguration hostConfig) {
		final ODataClient odataClient = ODataClientFactory.getClient();
		final AuxipOdataHttpClientFactory httpClientFactory = new AuxipOdataHttpClientFactory(hostConfig);

		// authentication
		final String authType = hostConfig.getAuthType();
		if ("oauth2".equalsIgnoreCase(authType)) {
			this.validateOauthConfig(hostConfig);
			httpClientFactory.addHeaderSupplier(() -> AuxipAuthenticationUtil.oauthHeaderFor(hostConfig));
		} else if ("basic".equalsIgnoreCase(authType)) {
			this.validateBasicAuthConfig(hostConfig);
			httpClientFactory.addHeaderSupplier(() -> AuxipAuthenticationUtil.basicAuthHeaderFor(hostConfig));
		} else {
			LOG.info("because of the configured authType "
					+ (StringUtil.isEmpty(authType) ? "<empty>" : "'" + authType + "'")
					+ " the auxip client will be disabled for target host " + hostConfig.getServiceRootUri());
		}

		// proxy
		final boolean needsProxy = null != this.config.getProxy() && !this.config.getProxy().isEmpty();
		ProxyWrappingHttpClientFactory proxyWrappingHttpClientFactory = null;
		if (needsProxy) {
			URI proxyUri = null;
			try {
				proxyUri = URI.create(this.config.getProxy());
			} catch (final Exception e) {
				LOG.error("could not build proxy URI from '" + this.config.getProxy() + "' and will not use proxy: "
						+ e.getMessage());
			}

			if (null != proxyUri) {
				proxyWrappingHttpClientFactory = new ProxyWrappingHttpClientFactory(proxyUri, httpClientFactory);
			}
		}

		if (null != proxyWrappingHttpClientFactory) {
			odataClient.getConfiguration().setHttpClientFactory(proxyWrappingHttpClientFactory);
		} else if (null != httpClientFactory) {
			odataClient.getConfiguration().setHttpClientFactory(httpClientFactory);
		}
		odataClient.getConfiguration().setHttpUriRequestFactory(httpClientFactory.requestFactory());

		return odataClient;
	}

	private void validateBasicAuthConfig(final AuxipHostConfiguration hostConfig) {
		if (StringUtil.isBlank(hostConfig.getUser())) {
			final String msg = "error configuring odata client with basic authentication for auxip target host "
					+ hostConfig.getServiceRootUri() + ": no user configured!";
			LOG.error(msg);
			throw new AuxipClientConfigurationException(msg);
		}
		//		if (StringUtil.isBlank(hostConfig.getPass())) {
		//			final String msg = "error configuring odata client with basic authentication for auxip target host "
		//					+ hostConfig.getServiceRootUri() + ": no password configured!";
		//			LOG.error(msg);
		//			throw new AuxipConfigurationException(msg);
		//		}
	}

	private void validateOauthConfig(final AuxipHostConfiguration hostConfig) {
		if (StringUtil.isBlank(hostConfig.getOauthAuthUrl())) {
			final String msg = "error configuring odata client with oauth2 for auxip target host "
					+ hostConfig.getServiceRootUri() + ": no oauth authorization server url configured!";
			LOG.error(msg);
			throw new AuxipClientConfigurationException(msg);
		}
		if (StringUtil.isBlank(hostConfig.getOauthClientId())) {
			final String msg = "error configuring odata client with oauth2 for auxip target host "
					+ hostConfig.getServiceRootUri() + ": no oauth client ID configured!";
			LOG.error(msg);
			throw new AuxipClientConfigurationException(msg);
		}
		if (StringUtil.isBlank(hostConfig.getOauthClientSecret())) {
			final String msg = "error configuring odata client with oauth2 for auxip target host "
					+ hostConfig.getServiceRootUri() + ": no oauth client secret configured!";
			LOG.error(msg);
			throw new AuxipClientConfigurationException(msg);
		}
		if (StringUtil.isBlank(hostConfig.getUser())) {
			final String msg = "error configuring odata client with oauth2 for auxip target host "
					+ hostConfig.getServiceRootUri() + ": no oauth authorization server user configured!";
			LOG.error(msg);
			throw new AuxipClientConfigurationException(msg);
		}
		if (StringUtil.isBlank(hostConfig.getPass())) {
			final String msg = "error configuring odata client with oauth2 for auxip target host "
					+ hostConfig.getServiceRootUri() + ": no oauth authorization server password configured!";
			LOG.error(msg);
			throw new AuxipClientConfigurationException(msg);
		}
	}

}
