package esa.s1pdgs.cpoc.xbip.client.sardine;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

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
		// lookup host configuration for the given URL
		final XbipHostConfiguration hostConfig = config.getHostConfigs().get(serverUrl.getHost());
		LOG.debug("Got {} for host {}", hostConfig, serverUrl.getHost());				
		return new SardineXbipClient(
				newSardineFor(hostConfig), 
				serverUrl.toString()
		);
	}
	
	private final Sardine newSardineFor(final XbipHostConfiguration hostConfig) {				
		if (hostConfig.isTrustSelfSignedCertificate()) {
			try {
				final SSLContextBuilder builder = new SSLContextBuilder();
				builder.loadTrustMaterial(new TrustSelfSignedStrategy());
				
				final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
						builder.build(),
			            NoopHostnameVerifier.INSTANCE
			    );
				return new SardineImpl(hostConfig.getUser(), hostConfig.getPass()) {
					@Override
					protected ConnectionSocketFactory createDefaultSecureSocketFactory() {
						return sslsf;
					}
				};
			} catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
				LOG.error(e);
			}
		}
		return SardineFactory.begin(hostConfig.getUser(), hostConfig.getPass());
	}

}
