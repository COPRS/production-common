package esa.s1pdgs.cpoc.outbox.cleaner.client;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPSClient;

import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.outbox.cleaner.config.FtpClientConfig;
import esa.s1pdgs.cpoc.outbox.cleaner.util.LogPrintWriter;
import esa.s1pdgs.cpoc.outbox.cleaner.util.Utils;

public class MyOceanFtpsDirectoryCleaner extends MyOceanFtpDirectoryCleaner {

	private static final int EXPLICIT_FTPS_DEFAULT_PORT = 23;
	private static final int IMPLICIT_FTPS_DEFAULT_PORT = 990;

	// --------------------------------------------------------------------------

	public MyOceanFtpsDirectoryCleaner(final FtpClientConfig config, final int retentionTimeinDays) {
		super(config, retentionTimeinDays);
	}

	// --------------------------------------------------------------------------

	@Override
	protected FTPSClient connectAndLogin() throws Exception {
		final FTPSClient ftpsClient = new SSLSessionReuseFTPSClient("TLS", this.config.isImplicitSsl());
		ftpsClient.addProtocolCommandListener(new PrintCommandListener(new LogPrintWriter(this.logger::debug), true));

		if (StringUtil.isNotBlank(this.config.getKeystoreFile())) {
			final KeyStore keyStore = newKeyStore(Utils.getInputStream(this.config.getKeystoreFile()),
					this.config.getKeystorePass());

			final KeyManagerFactory keyManagerFactory = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, this.config.getKeystorePass().toCharArray());

			final KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
			ftpsClient.setKeyManager(keyManagers[0]);
			ftpsClient.setWantClientAuth(true);
		}

		if (StringUtil.isNotBlank(this.config.getTruststoreFile())) {
			final KeyStore trustStore = newKeyStore(Utils.getInputStream(this.config.getTruststoreFile()),
					this.config.getTruststorePass());

			final TrustManagerFactory trustManagerFactory = TrustManagerFactory
					.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(trustStore);

			final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
			ftpsClient.setTrustManager(trustManagers[0]);
		}

		final int port = this.config.getPort() > 0 ? this.config.getPort()
				: (this.config.isImplicitSsl() ? IMPLICIT_FTPS_DEFAULT_PORT : EXPLICIT_FTPS_DEFAULT_PORT);

		// connect
		ftpsClient.connect(this.config.getHostname(), port);
		assertPositiveCompletion(ftpsClient);

		ftpsClient.execPBSZ(0);
		assertPositiveCompletion(ftpsClient);

		ftpsClient.execPROT("P");
		assertPositiveCompletion(ftpsClient);

		// login
		if (!ftpsClient.login(this.config.getUsername(), this.config.getPassw())) {
			throw new RuntimeException(
					"Could not authenticate user '" + this.config.getUsername() + "' with:" + this.config);
		}

		return ftpsClient;
	}

	// --------------------------------------------------------------------------

	static final KeyStore newKeyStore(final InputStream in, final String password) throws Exception {
		final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(in, password.toCharArray());

		return keystore;
	}

}
