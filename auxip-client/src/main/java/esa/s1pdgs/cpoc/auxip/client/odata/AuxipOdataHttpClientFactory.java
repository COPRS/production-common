package esa.s1pdgs.cpoc.auxip.client.odata;

import java.net.URI;
import java.security.cert.X509Certificate;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.olingo.client.core.http.DefaultHttpClientFactory;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpMethod;

public class AuxipOdataHttpClientFactory extends DefaultHttpClientFactory {

	private String username;
	private String password;
	private boolean sslValidation = true;

	// --------------------------------------------------------------------------

	public AuxipOdataHttpClientFactory() {
	}
	
	public AuxipOdataHttpClientFactory(boolean sslValidation) {
		this.sslValidation = sslValidation;
	}

	public AuxipOdataHttpClientFactory(String user, String pass) {
		this.username = user;
		this.password = pass;
	}
	
	public AuxipOdataHttpClientFactory(String user, String pass, boolean sslValidation) {
		this(user, pass);

		this.sslValidation = sslValidation;
	}

	// --------------------------------------------------------------------------

	@Override
	public DefaultHttpClient create(HttpMethod method, URI uri) {
		final DefaultHttpClient httpClient;
		
		if ("https".equalsIgnoreCase(uri.getScheme()) && !this.sslValidation) {
			// ssl
			final TrustStrategy acceptTrustStrategy = new TrustStrategy() {
				@Override
				public boolean isTrusted(final X509Certificate[] certificate, final String authType) {
					return true;
				}
			};

			final SchemeRegistry registry = new SchemeRegistry();
			try {
				final SSLSocketFactory ssf = new SSLSocketFactory(acceptTrustStrategy,
						SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				registry.register(new Scheme(uri.getScheme(), (0 < uri.getPort() ? uri.getPort() : 443), ssf));
			} catch (Exception e) {
				throw new ODataRuntimeException("error setting up ssl socket factory for odata client", e);
			}

			httpClient = new DefaultHttpClient(new BasicClientConnectionManager(registry));
			httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);
		} else {
			httpClient = super.create(method, uri);
		}

		// authentication
		if (null != this.username && !this.username.isEmpty()) {
			httpClient.getCredentialsProvider().setCredentials(new AuthScope(uri.getHost(), uri.getPort()),
					new UsernamePasswordCredentials(this.username, this.password));
		}

		return httpClient;
	}

}
