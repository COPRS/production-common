package esa.s1pdgs.cpoc.dissemination.worker.outbox;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.net.ftp.FTPSClient;

import esa.s1pdgs.cpoc.dissemination.worker.config.DisseminationWorkerProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.dissemination.worker.path.PathEvaluator;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public final class FtpsOutboxClient extends FtpOutboxClient {

	private static final int DEFAULT_PORT = 990;

	// --------------------------------------------------------------------------

	public static final class Factory implements OutboxClient.Factory {
		@Override
		public OutboxClient newClient(final ObsClient obsClient, final OutboxConfiguration config, final PathEvaluator eval) {
			return new FtpsOutboxClient(obsClient, config, eval);
		}
	}

	public FtpsOutboxClient(final ObsClient obsClient, final OutboxConfiguration config, final PathEvaluator pathEvaluator) {
		super(obsClient, config, pathEvaluator);
	}

	// --------------------------------------------------------------------------

	@Override
	public List<String> transfer(final List<ObsObject> obsObjects, final ReportingFactory reportingFactory) throws Exception {
		final FTPSClient ftpsClient = new FTPSClient("TLS", true);

		// if a keystore is configured, client authentication will be enabled. If it shall not be used, simply don't configure a keystore
		if (this.config.getKeystoreFile() != null) {
			final KeyStore keyStore = newKeyStore(Utils.getInputStream(this.config.getKeystoreFile()),
					this.config.getKeystorePass());

			final KeyManagerFactory keyManagerFactory = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, this.config.getKeystorePass().toCharArray());

			final KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
			ftpsClient.setKeyManager(keyManagers[0]);
			ftpsClient.setWantClientAuth(true);
		}

		if (this.config.getTruststoreFile() != null) {
			final KeyStore trustStore = newKeyStore(Utils.getInputStream(this.config.getTruststoreFile()),
					this.config.getTruststorePass());

			final TrustManagerFactory trustManagerFactory = TrustManagerFactory
					.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(trustStore);

			final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
			ftpsClient.setTrustManager(trustManagers[0]);
		}

		final int port = (this.config.getPort() > 0) ? this.config.getPort(): DEFAULT_PORT;
		ftpsClient.connect(this.config.getHostname(), port);
		assertPositiveCompletion(ftpsClient);

		ftpsClient.execPBSZ(0);
		assertPositiveCompletion(ftpsClient);

		ftpsClient.execPROT("P");
		assertPositiveCompletion(ftpsClient);

		return this.performTransfer(obsObjects, ftpsClient, reportingFactory);
	}

	static final KeyStore newKeyStore(final InputStream in, final String password) throws Exception {
		final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(in, password.toCharArray());

		return keystore;
	}

}