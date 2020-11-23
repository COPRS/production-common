package esa.s1pdgs.cpoc.auxip.client.odata;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.client.core.http.ProxyWrappingHttpClientFactory;
import org.apache.olingo.commons.api.http.HttpHeader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
		
		// TODO @MSc: anhand der config entscheiden ob basic auth oder oauth, entweder hier zentral für beide clients oder beim erstellen jedes clients selber
		
		final ODataClient odataClient = this.buildOdataClient(hostConfig);
	
		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(
			AuthScope.ANY,
		    new UsernamePasswordCredentials(hostConfig.getUser(), hostConfig.getPass())
		);
		
		// preemptive auth
		final HttpClientContext context = HttpClientContext.create();
		final AuthCache cache = new BasicAuthCache();
		
		cache.put(new HttpHost(serverUrl.getHost(),serverUrl.getPort(),"https"), new BasicScheme());
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
	
	private CloseableHttpClient newDownloadClient(final AuxipHostConfiguration hostConfig, final CredentialsProvider credentials) {		
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
	
	private ODataClient buildOdataClient(final AuxipHostConfiguration hostConfig) {
		final ODataClient odataClient = ODataClientFactory.getClient();
				
		final AuxipOdataHttpClientFactory httpClientFactory = new AuxipOdataHttpClientFactory(hostConfig);
		
		final boolean needsProxy = null != this.config.getProxy() && !this.config.getProxy().isEmpty();
		final boolean needsAuthentication = null != hostConfig.getUser() && !hostConfig.getUser().isEmpty();
		final boolean oauth = needsAuthentication && StringUtil.isNotEmpty(hostConfig.getOauthClientId())
				&& StringUtil.isNotEmpty(hostConfig.getOauthClientSecret())
				&& StringUtil.isNotEmpty(hostConfig.getOauthAuthUrl());
		
		// TODO @MSc:  initiales fehlerhandling wg. nicht vorhandener values hier oder früher
		
		// authentication
		if (oauth) {
			httpClientFactory.addHeaderSupplier(() -> this.oauthHeaderFor(hostConfig));
		} else if (needsAuthentication) {
			httpClientFactory.addHeaderSupplier(() -> basicAuthHeaderFor(hostConfig));
		}
		
		// proxy
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
	
	
	private static final Header basicAuthHeaderFor(final AuxipHostConfiguration hostConfig) {
		final String auth = hostConfig.getUser() + ":" + hostConfig.getPass();
		final byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));

		return new BasicHeader(HttpHeader.AUTHORIZATION, "Basic " + new String(encodedAuth));
	}
	
	private Header oauthHeaderFor(final AuxipHostConfiguration hostConfig) {
		final String accessToken;
		try {
			accessToken = this.retrieveOauthAccessToken(hostConfig);
		} catch (Exception e) {
			LOG.error("error retrieving oauth access token: " + StringUtil.stackTraceToString(e));
			throw e;
		}

		return new BasicHeader("OAUTH2-ACCESS-TOKEN", accessToken);
	}
	
	private String retrieveOauthAccessToken(final AuxipHostConfiguration hostConfig) {
		final String oauthAuthUrl = hostConfig.getOauthAuthUrl();
		final String oauthClientId = hostConfig.getOauthClientId();
		final String oauthClientSecret = hostConfig.getOauthClientSecret();
		final String oauthUser = hostConfig.getUser();
		final String oauthPass = hostConfig.getPass();

		return this.retrieveOauthAccessToken(URI.create(oauthAuthUrl), oauthClientId, oauthClientSecret, oauthUser,	oauthPass);
	}
	
	private String retrieveOauthAccessToken(final URI oauthAuthUrl, final String oauthClientId,
			final String oauthClientSecret, final String oauthUser, final String oauthPass) {
		final CloseableHttpClient httpClient = this.newOauthAuthorizationClient();

		final List<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("grant_type", "password"));
		data.add(new BasicNameValuePair("client_id", oauthClientId));
		data.add(new BasicNameValuePair("client_secret", oauthClientSecret));
		data.add(new BasicNameValuePair("username", oauthUser));
		data.add(new BasicNameValuePair("password", oauthPass));

		ObjectNode token = null;
		InputStream responseContent = null;
		try {
			final HttpPost post = new HttpPost(oauthAuthUrl);
			post.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
			
			final HttpResponse response = httpClient.execute(post);
			
			responseContent = response.getEntity().getContent();
					
			token = (ObjectNode) new ObjectMapper().readTree(responseContent);
			
		} catch (Exception e) {
			throw new AuxipOauthException(
					"error retrieving oauth access token from " + oauthAuthUrl + ": " + e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(responseContent);
		}
		
		if (null == token) {
			throw new AuxipOauthException("error retrieving oauth access token from " + oauthAuthUrl
					+ ": no result from parsing response to json");
		}

		final JsonNode tokenNode = token.get("access_token");
		if (null == tokenNode) {
			throw new AuxipOauthException(
					"error retrieving oauth access token from " + oauthAuthUrl + ": response contains no access_token");
		}

		final String accessToken = tokenNode.asText();
		if (StringUtil.isEmpty(accessToken)) {
			throw new AuxipOauthException("error retrieving oauth access token from " + oauthAuthUrl
					+ ": response contains no value for access_token");
		}

		return accessToken;
	}
	
	private CloseableHttpClient newOauthAuthorizationClient() {		
		try {
			final HttpClientBuilder clientBuilder = HttpClientBuilder.create();
			
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
	
}
