package de.werum.coprs.cadip.client.odata;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.olingo.client.api.http.HttpUriRequestFactory;
import org.apache.olingo.client.core.http.DefaultHttpClientFactory;
import org.apache.olingo.client.core.http.DefaultHttpUriRequestFactory;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpMethod;

import de.werum.coprs.cadip.client.config.CadipClientConfigurationProperties.CadipHostConfiguration;
import esa.s1pdgs.cpoc.common.utils.StringUtil;

final class CadipOdataHttpClientFactory extends DefaultHttpClientFactory
{
	private static final Logger LOG = LogManager.getLogger(CadipOdataHttpClientFactory.class);
	
	private final CadipHostConfiguration hostConfig;
	private final List<Supplier<Header>> headers = new ArrayList<>();
	
	public CadipOdataHttpClientFactory(
			final CadipHostConfiguration hostConfig
	) {
		this.hostConfig = hostConfig;
	}
	
	public final CadipOdataHttpClientFactory addHeaderSupplier(final Supplier<Header> header) 
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
					final Header header;
					try {
						header = headerSupplier.get();
					} catch (Exception e) {
						LOG.error("error creating authentication header for cadip odata client: "
								+ StringUtil.stackTraceToString(e));
						throw e;
					}

					result.addHeader(header);
				}
				return result;
			}
		};			
	}		
}