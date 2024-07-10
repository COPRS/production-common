/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.ebip.client.apacheftp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import esa.s1pdgs.cpoc.common.utils.Streams;
import esa.s1pdgs.cpoc.ebip.client.EdipEntry;
import esa.s1pdgs.cpoc.ebip.client.EdipEntryImpl;
import esa.s1pdgs.cpoc.ebip.client.apacheftp.ftpsclient.SSLSessionReuseFTPSClient;
import esa.s1pdgs.cpoc.ebip.client.apacheftp.util.LogPrintWriter;
import esa.s1pdgs.cpoc.ebip.client.config.EdipClientConfigurationProperties.EdipHostConfiguration;

public abstract class AbstractApacheFtpClient {

	static final Logger LOG = LogManager.getLogger(AbstractApacheFtpClient.class);

	protected final EdipHostConfiguration config;
	protected final URI uri;

	public AbstractApacheFtpClient(final EdipHostConfiguration config, final URI uri) {
		this.config = config;
		this.uri = uri;
	}

	protected FTPClient connectedClient() {
		try {
			final FTPClient ftpClient = newClient();
			ftpClient.setRemoteVerificationEnabled(false);
			
			LOG.debug("Setting internal buffer size to {} Bytes",config.getBufferSize());
			ftpClient.setBufferSize(config.getBufferSize());

			if (!ftpClient.login(config.getUser(), config.getPass())) {
				throw new RuntimeException("Could not authenticate user " + config.getUser());
			}
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

			if (config.isPasv()) {
				ftpClient.enterLocalPassiveMode();
			} else {
				ftpClient.enterLocalActiveMode();
			}
			return ftpClient;
		} catch (final Exception e) {
			// TODO add proper error handling
			throw new RuntimeException(e);
		}
	}

	protected boolean isNotDirectory(final Path path, final FTPFile ftpFile) {

		return null != ftpFile && !ftpFile.isDirectory() && pathEqualsFtpFileName(path, ftpFile) && null != path
				&& null != path.getParent();
	}

	protected EdipEntry toEdipEntry(final Path path, final FTPFile ftpFile) {
		final Path entryPath = path.resolve(ftpFile.getName());

		return new EdipEntryImpl(ftpFile.getName(), entryPath, toUri(entryPath), ftpFile.getTimestamp().getTime(),
				ftpFile.getSize());
	}

	protected boolean pathEqualsFtpFileName(final Path path, final FTPFile ftpFile) {

		Path ftpFilePath = Paths.get(ftpFile.getName()).getFileName();
		Path uriPath = path.getFileName();

		if (ftpFilePath == null && uriPath == null) {
			return true;
		}
		if (ftpFilePath == null || uriPath == null) {
			return false;
		}

		return ftpFilePath.toString().equals(uriPath.toString());

	}

	private FTPClient newClient() throws Exception {

		if (!"ftps".equals(uri.getScheme())) {
			final FTPClient ftpClient = new FTPClient();

			ftpClient.addProtocolCommandListener(new PrintCommandListener(new LogPrintWriter(LOG::debug), true));
			ftpClient.setDefaultTimeout(config.getConnectTimeoutSec() * 1000);
			ftpClient.setConnectTimeout(config.getConnectTimeoutSec() * 1000);
			ftpClient.setDataTimeout(config.getConnectTimeoutSec() * 1000);
			connect(ftpClient);
			return ftpClient;
		} else {
			// FTPS client creation
			final FTPSClient ftpsClient = new SSLSessionReuseFTPSClient(config.getSslProtocol(),
					!config.isExplicitFtps(), config.isFtpsSslSessionReuse(), config.isUseExtendedMasterSecret());
			ftpsClient.setDefaultTimeout(config.getConnectTimeoutSec() * 1000);
			ftpsClient.setConnectTimeout(config.getConnectTimeoutSec() * 1000);
			ftpsClient.setDataTimeout(config.getConnectTimeoutSec() * 1000);
			ftpsClient.addProtocolCommandListener(new PrintCommandListener(new LogPrintWriter(LOG::debug), true));

			// handle SSL
			// if a keystore is configured, client authentication will be enabled
			if (!StringUtils.isEmpty(config.getKeyManagerKeyStore())) {
				final String keystorePass = config.getKeyManagerKeyStorePassword();

				final KeyStore keyStore = newKeyStore(Streams.getInputStream(config.getKeyManagerKeyStore()),
						keystorePass);

				final KeyManagerFactory keyManagerFactory = KeyManagerFactory
						.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				keyManagerFactory.init(keyStore, keystorePass.toCharArray());

				final KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
				final KeyManager keyManager = keyManagers[0];

				ftpsClient.setKeyManager(keyManager);
				ftpsClient.setWantClientAuth(true);
			}

			if (!StringUtils.isEmpty(config.getTrustManagerKeyStore())) {
				final String trustManagerPassword = config.getTrustManagerKeyStorePassword();

				final KeyStore trustStore = newKeyStore(Streams.getInputStream(config.getTrustManagerKeyStore()),
						trustManagerPassword);

				final TrustManagerFactory trustMgrFactory = TrustManagerFactory
						.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				trustMgrFactory.init(trustStore);
				final TrustManager[] trustManagers = trustMgrFactory.getTrustManagers();
				final TrustManager keyManager = trustManagers[0];
				ftpsClient.setTrustManager(keyManager);
			} else if (config.isTrustSelfSignedCertificate()) {
				ftpsClient.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
			} else {
				ftpsClient.setTrustManager(TrustManagerUtils.getValidateServerCertificateTrustManager());
			}

			if (config.isEnableHostnameVerification()) {
				ftpsClient.setHostnameVerifier(new DefaultHostnameVerifier());
			}

			connect(ftpsClient);
			ftpsClient.execPBSZ(0);
			assertPositiveCompletion(ftpsClient);

			if (config.isEncryptDataChannel()) {
				ftpsClient.execPROT("P");
			} else {
				ftpsClient.execPROT("C");
			}
			assertPositiveCompletion(ftpsClient);

			return ftpsClient;
		}
	}

	private KeyStore newKeyStore(final InputStream inputStream, final String keystorePass) throws Exception {
		try {
			final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(inputStream, keystorePass.toCharArray());
			return keystore;
		} finally {
			Streams.close(inputStream);
		}
	}

	private FTPClient connect(final FTPClient ftpClient) throws IOException {
		if (uri.getPort() == -1) {
			ftpClient.connect(config.getServerName());
		} else {
			ftpClient.connect(config.getServerName(), uri.getPort());
		}
		assertPositiveCompletion(ftpClient);
		return ftpClient;
	}

	private URI toUri(final Path entryPath) {
		return uri.resolve(entryPath.toString());
	}

	static void assertPositiveCompletion(final FTPClient client) throws IOException {
		if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
			throw new IOException("Error on command execution. Reply was: " + client.getReplyString());
		}
	}

}
