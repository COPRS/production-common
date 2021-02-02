package esa.s1pdgs.cpoc.dissemination.worker.outbox;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.net.ftp.FTPSClient;

import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.dissemination.worker.config.DisseminationWorkerProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.dissemination.worker.outbox.ftpsclient.SSLSessionReuseFTPSClient;
import esa.s1pdgs.cpoc.dissemination.worker.path.PathEvaluator;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public final class FtpsOutboxClient extends FtpOutboxClient {

	private static final int EXPLICIT_FTPS_DEFAULT_PORT = 23;
	private static final int IMPLICIT_FTPS_DEFAULT_PORT = 990;

	public static final class Factory implements OutboxClient.Factory {
		@Override
		public OutboxClient newClient(final ObsClient obsClient, final OutboxConfiguration config, final PathEvaluator eval) {
			return new FtpsOutboxClient(obsClient, config, eval);
		}
	}

	public FtpsOutboxClient(final ObsClient obsClient, final OutboxConfiguration config, final PathEvaluator pathEvaluator) {
		super(obsClient, config, pathEvaluator);
	}

	@Override
	public String transfer(final ObsObject mainFile, final List<ObsObject> obsObjects,
		final ReportingFactory reportingFactory) throws Exception {
		final FTPSClient ftpsClient = new SSLSessionReuseFTPSClient("TLS", config.isImplicitSsl());

		// if a keystore is configured, client authentication will be enabled. If it shall not be used, simply don't configure a keystore
		if (StringUtil.isNotBlank(config.getKeystoreFile())) {
			final KeyStore keyStore = newKeyStore(Utils.getInputStream(config.getKeystoreFile()),
					config.getKeystorePass());

			final KeyManagerFactory keyManagerFactory = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, config.getKeystorePass().toCharArray());

			final KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
			ftpsClient.setKeyManager(keyManagers[0]);
			ftpsClient.setWantClientAuth(true);
		}

		if (StringUtil.isNotBlank(config.getTruststoreFile())) {
			final KeyStore trustStore = newKeyStore(Utils.getInputStream(config.getTruststoreFile()),
					config.getTruststorePass());

			final TrustManagerFactory trustManagerFactory = TrustManagerFactory
					.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(trustStore);

			final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
			ftpsClient.setTrustManager(trustManagers[0]);
		}

		final int port = config.getPort() > 0 ? config.getPort() : (config.isImplicitSsl() ? IMPLICIT_FTPS_DEFAULT_PORT : EXPLICIT_FTPS_DEFAULT_PORT);
		ftpsClient.connect(config.getHostname(), port);
		assertPositiveCompletion(ftpsClient);

		ftpsClient.execPBSZ(0);
		assertPositiveCompletion(ftpsClient);

		ftpsClient.execPROT("P");
		assertPositiveCompletion(ftpsClient);

		return performTransfer(mainFile, obsObjects, ftpsClient, reportingFactory);
	}

	static final KeyStore newKeyStore(final InputStream in, final String password) throws Exception {
		final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(in, password.toCharArray());
		return keystore;
	}

}