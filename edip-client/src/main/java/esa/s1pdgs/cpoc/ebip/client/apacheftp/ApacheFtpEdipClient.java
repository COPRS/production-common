package esa.s1pdgs.cpoc.ebip.client.apacheftp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
import esa.s1pdgs.cpoc.ebip.client.EdipClient;
import esa.s1pdgs.cpoc.ebip.client.EdipEntry;
import esa.s1pdgs.cpoc.ebip.client.EdipEntryFilter;
import esa.s1pdgs.cpoc.ebip.client.EdipEntryImpl;
import esa.s1pdgs.cpoc.ebip.client.apacheftp.ftpsclient.SSLSessionReuseFTPSClient;
import esa.s1pdgs.cpoc.ebip.client.apacheftp.util.LogPrintWriter;
import esa.s1pdgs.cpoc.ebip.client.config.EdipClientConfigurationProperties.EdipHostConfiguration;

public class ApacheFtpEdipClient implements EdipClient {
	static final Logger LOG = LogManager.getLogger(ApacheFtpEdipClient.class);

	private final EdipHostConfiguration config;
	private final URI uri;
	
	public ApacheFtpEdipClient(final EdipHostConfiguration config, final URI uri) {
		this.config = config;
		this.uri = uri;
	}

	@Override
	public final List<EdipEntry> list(final EdipEntryFilter filter) throws IOException {
		LOG.debug("Listing {}", uri.getPath());
		final Path uriPath = Paths.get(uri.getPath());
		
		final FTPClient client = connectedClient();
		
		final List<EdipEntry> result = getIfNotDirectory(client, uriPath)
				.orElse(listRecursively(client, uriPath, filter));	
		
		client.logout();
		client.disconnect();
		LOG.debug("Client returns result: {}", result);
		return result;
	}
	
	@Override
	public final InputStream read(final EdipEntry entry) {
		try {
			final FTPClient client = connectedClient();
			final InputStream res = new BufferedInputStream(client.retrieveFileStream(entry.getPath().toString()))
			{
				@Override
				public void close() throws IOException {	
					if (client.isConnected()) {
						super.close();
						client.completePendingCommand();
						client.logout();
						client.disconnect();
						assertPositiveCompletion(client);
					}
				}	
			};
			// possibly a fix for the NPE issue described above
			final int replyCode = client.getReplyCode();
	        if (!FTPReply.isPositiveIntermediate(replyCode) && !FTPReply.isPositivePreliminary(replyCode)) {
	        	res.close();
	      		throw new IOException("Error on command execution. Reply was: " + client.getReplyString());
	        }
			return res;			
		} catch (final IOException e) {
			// TODO add proper error handling
			throw new RuntimeException(e);
		}
	}
	
	private FTPClient connectedClient() {
		try {
			final FTPClient ftpClient = newClient();
			ftpClient.setRemoteVerificationEnabled(false);

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
	
	private FTPClient newClient() throws Exception {
		
		if (!"ftps".equals(uri.getScheme())) {
			final FTPClient ftpClient = new FTPClient();
			
			ftpClient.addProtocolCommandListener(
					new PrintCommandListener(new LogPrintWriter(LOG::debug), true)
			);
			ftpClient.setDefaultTimeout(config.getConnectTimeoutSec() * 1000);
			ftpClient.setConnectTimeout(config.getConnectTimeoutSec() * 1000);
			ftpClient.setDataTimeout(config.getConnectTimeoutSec() * 1000);
			connect(ftpClient);
			return ftpClient;
		}	
		else {
			// FTPS client creation			
			final FTPSClient ftpsClient = new SSLSessionReuseFTPSClient(config.getSslProtocol(),
					!config.isExplicitFtps(), config.isFtpsSslSessionReuse(), config.isUseExtendedMasterSecret());
			ftpsClient.setDefaultTimeout(config.getConnectTimeoutSec() * 1000);
			ftpsClient.setConnectTimeout(config.getConnectTimeoutSec() * 1000);
			ftpsClient.setDataTimeout(config.getConnectTimeoutSec() * 1000);
			ftpsClient.addProtocolCommandListener(
					new PrintCommandListener(new LogPrintWriter(LOG::debug), true)
			);
			
			// handle SSL
		    // if a keystore is configured, client authentication will be enabled
		    if (!StringUtils.isEmpty(config.getKeyManagerKeyStore()))
		    {
		      final String keystorePass = config.getKeyManagerKeyStorePassword();

		      final KeyStore keyStore = newKeyStore(
		          Streams.getInputStream(config.getKeyManagerKeyStore()),
		          keystorePass
		      );

		      final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
		          KeyManagerFactory.getDefaultAlgorithm()
		      );
		      keyManagerFactory.init(keyStore, keystorePass.toCharArray());

		      final KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
		      final KeyManager keyManager = keyManagers[0];

		      ftpsClient.setKeyManager(keyManager);
		      ftpsClient.setWantClientAuth(true);
		    }

		    if (!StringUtils.isEmpty(config.getTrustManagerKeyStore()))
		    {
		      final String trustManagerPassword = config.getTrustManagerKeyStorePassword();

		      final KeyStore trustStore = newKeyStore(
		          Streams.getInputStream(config.getTrustManagerKeyStore()),
		          trustManagerPassword
		      );

		      final TrustManagerFactory trustMgrFactory = TrustManagerFactory.getInstance(
		          TrustManagerFactory.getDefaultAlgorithm()
		      );
		      trustMgrFactory.init(trustStore);
		      final TrustManager[] trustManagers = trustMgrFactory.getTrustManagers();
		      final TrustManager keyManager = trustManagers[0];
		      ftpsClient.setTrustManager(keyManager);
		    }
		    else if (config.isTrustSelfSignedCertificate()) {
		      ftpsClient.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
		    }
		    else {
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

	private KeyStore newKeyStore(final InputStream inputStream, final String keystorePass)
			throws Exception {
	    try
	    {
	      final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
	      keystore.load(inputStream, keystorePass.toCharArray());
	      return keystore;
	    }
	    finally
	    {
	      Streams.close(inputStream);
	    }
	}

	private FTPClient connect(final FTPClient ftpClient) throws IOException {
		if (uri.getPort() == -1) {
			ftpClient.connect(config.getServerName());
		}
		else {
			ftpClient.connect(config.getServerName(), uri.getPort());
		}	
		assertPositiveCompletion(ftpClient);
		return ftpClient;
	}
	
	private Optional<List<EdipEntry>> getIfNotDirectory(final FTPClient client, final Path path) throws IOException {
		if (null == path) {
			throw new RuntimeException(String.format("Cannot list path: %s", path));
		}
		final List<FTPFile> ftpFiles = Arrays.asList(client.listFiles(path.toString()));
		FTPFile ftpFile = ftpFiles.size() == 1 ? ftpFiles.get(0) : null;
		if (null != ftpFile && !ftpFile.isDirectory() && null != path && null != path.getParent()) {
			EdipEntry edipEntry = toEdipEntry(path, ftpFile);
			return Optional.of(Collections.singletonList(edipEntry));
		}
		return Optional.empty();
	}
	
	private List<EdipEntry> listRecursively(
			final FTPClient client, 
			final Path path, 
			final EdipEntryFilter filter
	) 
			throws IOException {
		final List<EdipEntry> result = new ArrayList<>();	
		LOG.trace("Recursive listing of {}", path);
		
		for (final FTPFile ftpFile : client.listFiles(path.toString())) {
			final EdipEntry entry = toEdipEntry(path, ftpFile);
			//System.err.println("FOO " + entry);
			if (!filter.accept(entry)) {
				LOG.trace("{} ignored by {}", entry, filter);
				continue;
			}	
			
			if (ftpFile.isDirectory()) {
				LOG.trace("Found dir {}", entry);
				result.addAll(listRecursively(client, entry.getPath(), filter));
			}
			else {
				LOG.trace("Found file {}", entry);
				result.add(entry);
			}
		}
		return result;
	}
	
	private EdipEntry toEdipEntry(final Path path, final FTPFile ftpFile) {
		final Path entryPath = path.resolve(ftpFile.getName());

		return new EdipEntryImpl(
				ftpFile.getName(), 
				entryPath, 
				toUri(entryPath), 
				ftpFile.getTimestamp().getTime(), 
				ftpFile.getSize()
		);
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
