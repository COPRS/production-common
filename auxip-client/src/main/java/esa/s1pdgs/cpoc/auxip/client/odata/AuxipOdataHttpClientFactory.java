package esa.s1pdgs.cpoc.auxip.client.odata;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.olingo.client.api.http.HttpUriRequestFactory;
import org.apache.olingo.client.core.http.DefaultHttpClientFactory;
import org.apache.olingo.client.core.http.DefaultHttpUriRequestFactory;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpMethod;

import esa.s1pdgs.cpoc.auxip.client.config.AuxipClientConfigurationProperties.AuxipHostConfiguration;

final class AuxipOdataHttpClientFactory extends DefaultHttpClientFactory
{
	private final AuxipHostConfiguration hostConfig;
	private final List<Supplier<Header>> headers = new ArrayList<>();
	
	public AuxipOdataHttpClientFactory(
			final AuxipHostConfiguration hostConfig
	) {
		this.hostConfig = hostConfig;
	}
	
	public final AuxipOdataHttpClientFactory addHeaderSupplier(final Supplier<Header> header) 
	{
		headers.add(header);
		return this;
	}

	@Override
	public final DefaultHttpClient create(final HttpMethod method, final URI uri) {
		final DefaultHttpClient httpClient;
		
		if ("https".equalsIgnoreCase(uri.getScheme()) && !hostConfig.isSslValidation()) {
			// ssl
			final TrustStrategy acceptTrustStrategy = new TrustStrategy() {
				@Override
				public boolean isTrusted(final X509Certificate[] certificate, final String authType) {
					return true;
				}
			};

			final SchemeRegistry registry = new SchemeRegistry();
			try {
				final SSLSocketFactory ssf = new SSLSocketFactory(
						acceptTrustStrategy,
						SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				registry.register(new Scheme(uri.getScheme(), (0 < uri.getPort() ? uri.getPort() : 443), ssf));
			} catch (final Exception e) {
				throw new ODataRuntimeException("error setting up ssl socket factory for odata client", e);
			}

			httpClient = new DefaultHttpClient(new BasicClientConnectionManager(registry));
			httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);
		} else {
			httpClient = super.create(method, uri);
		}			
		return httpClient;
	}
	
	public final HttpUriRequestFactory requestFactory() 
	{
		return new DefaultHttpUriRequestFactory() {				
			@Override
			public final HttpUriRequest create(final HttpMethod method, final URI uri) {
				final HttpUriRequest result = super.create(method, uri);
				for (final Supplier<Header> headerSupplier : headers) {
					result.addHeader(headerSupplier.get());	
				}									
				return result;
			}
		};			
	}		
}