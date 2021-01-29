package esa.s1pdgs.cpoc.disseminator.outbox;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.net.ftp.FTPSClient;

import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.disseminator.path.PathEvaluater;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public final class FtpsOutboxClient extends FtpOutboxClient {
	public static final class Factory implements OutboxClient.Factory {
		@Override
		public OutboxClient newClient(final ObsClient obsClient, final OutboxConfiguration config, final PathEvaluater eval) {
			return new FtpsOutboxClient(obsClient, config, eval);
		}			
	}
	
	private static final int DEFAULT_PORT_EXPLICIT = 23;
	private static final int DEFAULT_PORT_IMPLICIT = 990;

	public FtpsOutboxClient(final ObsClient obsClient, final OutboxConfiguration config, final PathEvaluater pathEvaluator) {
		super(obsClient, config, pathEvaluator);
	}

	@Override
	public String transfer(final ObsObject obsObject, final ReportingFactory reportingFactory) throws Exception {
		final FTPSClient ftpsClient = new FTPSClient("TLS", true);

		// if a keystore is configured, client authentication will be enabled. If it shall not be used, simply
		// don't configure a keystore
		if (config.getKeystoreFile() != null) {
			final KeyStore keyStore = newKeyStore(Utils.getInputStream(config.getKeystoreFile()),
					config.getKeystorePass());

			final KeyManagerFactory keyManagerFactory = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, config.getKeystorePass().toCharArray());

			final KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
			ftpsClient.setKeyManager(keyManagers[0]);
			ftpsClient.setWantClientAuth(true);
		}
		
		if (config.getTruststoreFile() != null) {
			final KeyStore trustStore = newKeyStore(Utils.getInputStream(config.getTruststoreFile()),
					config.getTruststorePass());

			final TrustManagerFactory trustManagerFactory = TrustManagerFactory
					.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(trustStore);
			
		    final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
		    ftpsClient.setTrustManager(trustManagers[0]);
		}
		
		final int port = config.getPort() > 0 ? config.getPort() : (config.isImplicitSsl() ? DEFAULT_PORT_IMPLICIT : DEFAULT_PORT_EXPLICIT);
		ftpsClient.connect(config.getHostname(), port);
	    assertPositiveCompletion(ftpsClient);
	    
	    ftpsClient.execPBSZ(0);
        assertPositiveCompletion(ftpsClient);
        
	    ftpsClient.execPROT("P");
        assertPositiveCompletion(ftpsClient);
        
        return performTransfer(obsObject, ftpsClient, reportingFactory);
	}	
	
	static final KeyStore newKeyStore(final InputStream in, final String password)
			throws Exception {
		final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(in, password.toCharArray());
		return keystore;
	}
	

}