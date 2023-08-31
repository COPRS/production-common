package de.werum.coprs.cadip.client.odata;

import java.net.URI;
import java.util.Objects;

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

import de.werum.coprs.cadip.client.CadipClient;
import de.werum.coprs.cadip.client.CadipClientFactory;
import de.werum.coprs.cadip.client.config.CadipClientConfigurationProperties;
import de.werum.coprs.cadip.client.config.CadipClientConfigurationProperties.CadipHostConfiguration;
import esa.s1pdgs.cpoc.common.utils.StringUtil;

public class CadipOdataClientFactory implements CadipClientFactory {
	private static final Logger LOG = LogManager.getLogger(CadipOdataClientFactory.class);

	/** OData entity set name for the entities that will be queried */
	public static final String ENTITY_SET_NAME = "Products";

	private final CadipClientConfigurationProperties config;

	// --------------------------------------------------------------------------

	public CadipOdataClientFactory(final CadipClientConfigurationProperties config) {
		this.config = config;
	}

	// --------------------------------------------------------------------------

	@Override
	public CadipClient newCadipClient(final URI serverUrl) {
		final CadipHostConfiguration hostConfig = this.hostConfigFor(serverUrl.toString());
		final ODataClient odataClient = this.newOdataClient(hostConfig);

		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(hostConfig.getUser(), hostConfig.getPass()));

		// preemptive auth
		final HttpClientContext context = HttpClientContext.create();
		final AuthCache cache = new BasicAuthCache();

		cache.put(new HttpHost(serverUrl.getHost(), serverUrl.getPort(), "https"), new BasicScheme());
		context.setAuthCache(cache);
		context.setCredentialsProvider(credentialsProvider);

		// Check validity of configuration
		Objects.requireNonNull(odataClient, "OData client must not be null!");
		Objects.requireNonNull(hostConfig, "host configuration must not be null!");
		Objects.requireNonNull(hostConfig.getServiceRootUri(), "the root service URL must not be null!");

		return new CadipOdataClient(odataClient, hostConfig, newDownloadClient(hostConfig, credentialsProvider),
				context);
	}

	private CadipHostConfiguration hostConfigFor(final String serviceRootUri) {
		// lookup host configuration for the given URL
		for (final CadipHostConfiguration hostConfig : this.config.getHostConfigs().values()) {
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

	private CloseableHttpClient newDownloadClient(final CadipHostConfiguration hostConfig,
			final CredentialsProvider credentials) {
		try {
			final HttpClientBuilder clientBuilder = HttpClientBuilder.create();
			clientBuilder.setDefaultCredentialsProvider(credentials);

			final SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(new TrustSelfSignedStrategy());
			final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(),
					NoopHostnameVerifier.INSTANCE);
			clientBuilder.setSSLSocketFactory(sslsf);

			return clientBuilder.build();
		} catch (final Exception e) {
			// FIXME error handling
			throw new RuntimeException(e);
		}
	}

	private ODataClient newOdataClient(final CadipHostConfiguration hostConfig) {
		final ODataClient odataClient = ODataClientFactory.getClient();
		final CadipOdataHttpClientFactory httpClientFactory = new CadipOdataHttpClientFactory(hostConfig);

		// authentication
		final String authType = hostConfig.getAuthType();
		if ("oauth2".equalsIgnoreCase(authType)) {
			this.validateOauthConfig(hostConfig);
			httpClientFactory.addHeaderSupplier(() -> CadipAuthenticationUtil.oauthHeaderFor(hostConfig));
		} else if ("basic".equalsIgnoreCase(authType)) {
			this.validateBasicAuthConfig(hostConfig);
			httpClientFactory.addHeaderSupplier(() -> CadipAuthenticationUtil.basicAuthHeaderFor(hostConfig));
		} else {
			LOG.info("because of the configured authType "
					+ (StringUtil.isEmpty(authType) ? "<empty>" : "'" + authType + "'")
					+ " the cadip client will be disabled for target host " + hostConfig.getServiceRootUri());
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

	private void validateBasicAuthConfig(final CadipHostConfiguration hostConfig) {
		if (StringUtil.isBlank(hostConfig.getUser())) {
			final String msg = "error configuring odata client with basic authentication for cadip target host "
					+ hostConfig.getServiceRootUri() + ": no user configured!";
			LOG.error(msg);
			throw new CadipClientConfigurationException(msg);
		}
	}

	private void validateOauthConfig(final CadipHostConfiguration hostConfig) {
		if (StringUtil.isBlank(hostConfig.getOauthAuthUrl())) {
			final String msg = "error configuring odata client with oauth2 for cadip target host "
					+ hostConfig.getServiceRootUri() + ": no oauth authorization server url configured!";
			LOG.error(msg);
			throw new CadipClientConfigurationException(msg);
		}
		if (StringUtil.isBlank(hostConfig.getOauthClientId())) {
			final String msg = "error configuring odata client with oauth2 for cadip target host "
					+ hostConfig.getServiceRootUri() + ": no oauth client ID configured!";
			LOG.error(msg);
			throw new CadipClientConfigurationException(msg);
		}
		if (StringUtil.isBlank(hostConfig.getOauthClientSecret())) {
			final String msg = "error configuring odata client with oauth2 for cadip target host "
					+ hostConfig.getServiceRootUri() + ": no oauth client secret configured!";
			LOG.error(msg);
			throw new CadipClientConfigurationException(msg);
		}
		if (StringUtil.isBlank(hostConfig.getUser())) {
			final String msg = "error configuring odata client with oauth2 for cadip target host "
					+ hostConfig.getServiceRootUri() + ": no oauth authorization server user configured!";
			LOG.error(msg);
			throw new CadipClientConfigurationException(msg);
		}
		if (StringUtil.isBlank(hostConfig.getPass())) {
			final String msg = "error configuring odata client with oauth2 for cadip target host "
					+ hostConfig.getServiceRootUri() + ": no oauth authorization server password configured!";
			LOG.error(msg);
			throw new CadipClientConfigurationException(msg);
		}
	}

}
