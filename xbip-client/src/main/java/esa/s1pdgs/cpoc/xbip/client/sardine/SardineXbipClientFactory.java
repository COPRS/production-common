package esa.s1pdgs.cpoc.xbip.client.sardine;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.impl.SardineImpl;

import esa.s1pdgs.cpoc.xbip.client.XbipClient;
import esa.s1pdgs.cpoc.xbip.client.XbipClientFactory;
import esa.s1pdgs.cpoc.xbip.client.config.XbipClientConfigurationProperties;
import esa.s1pdgs.cpoc.xbip.client.config.XbipClientConfigurationProperties.XbipHostConfiguration;

public class SardineXbipClientFactory implements XbipClientFactory {
	private static final Logger LOG = LogManager.getLogger(SardineXbipClientFactory.class);
	
	private final XbipClientConfigurationProperties config;

	public SardineXbipClientFactory(final XbipClientConfigurationProperties config) {
		this.config = config;
	}

	@Override
	public XbipClient newXbipClient(final URI serverUrl) {	
		return new SardineXbipClient(				
				newSardineFor(hostConfigFor(serverUrl.getHost())), 
				serverUrl
		);
	}
	
	private final XbipHostConfiguration hostConfigFor(final String server) {
		// lookup host configuration for the given URL	
		for (final XbipHostConfiguration hostConfig : config.getHostConfigs()) {
			if (server.equals(hostConfig.getServerName())) {
				LOG.trace("Found config {}" + hostConfig);
				return hostConfig;
			}
		}
		throw new IllegalArgumentException(
				String.format("Could not find configuration for server '%s'", server)
		);
	}
	
	private final ProxySelector proxy() {
		if (config.getProxyHost() != null) {
			LOG.debug("Using Proxy {}:{}",config.getProxyHost(), config.getProxyPort());			
			return new ProxySelector() {			
				@Override
				public final List<Proxy> select(final URI uri) {
					try {
						return Collections.singletonList(new Proxy(
								Type.HTTP,
								new InetSocketAddress(
										InetAddress.getByName(config.getProxyHost()), 
										config.getProxyPort()
								)
						));
						// FIXME proper error handling
					} catch (final UnknownHostException e) {
						LOG.error(e);
					}
					return Collections.emptyList();
				}
				
				@Override
				public final void connectFailed(final URI uri, final SocketAddress sa, final IOException ioe) {
					LOG.error(ioe);
				}
			};
		}
		return null;
	}
	
		
	private final Sardine newSardineFor(final XbipHostConfiguration hostConfig) {			
		if (hostConfig.isTrustSelfSignedCertificate()) {
			LOG.debug("Trusting SSL for server {}", hostConfig.getServerName());		
			try {
				final SSLContextBuilder builder = new SSLContextBuilder();
				builder.loadTrustMaterial(new TrustSelfSignedStrategy());
				
				final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
						builder.build(),
			            NoopHostnameVerifier.INSTANCE
			    );
				return new SardineImpl(hostConfig.getUser(), hostConfig.getPass(), proxy()) {
					@Override
					protected ConnectionSocketFactory createDefaultSecureSocketFactory() {
						return sslsf;
					}
				};
			} catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
				throw new IllegalArgumentException(
						String.format("Error creating SSL context for %s: %s", hostConfig, e.getMessage()),
						e
				);
			}
		}
		return SardineFactory.begin(hostConfig.getUser(), hostConfig.getPass(), proxy());
	}

}
