package esa.s1pdgs.cpoc.ebip.client.apacheftp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import esa.s1pdgs.cpoc.common.utils.Streams;
import esa.s1pdgs.cpoc.ebip.client.EdipClient;
import esa.s1pdgs.cpoc.ebip.client.EdipEntry;
import esa.s1pdgs.cpoc.ebip.client.EdipEntryFilter;
import esa.s1pdgs.cpoc.ebip.client.EdipEntryImpl;
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
		
		final FTPClient client = connectedClient();
		
		final List<EdipEntry> result = listRecursively(client, Paths.get(uri.getPath()), filter);		
		client.logout();
		client.disconnect();
		return result;
	}
	
	@Override
	public final InputStream read(final EdipEntry entry) {
		try {
			final FTPClient client = connectedClient();
			return new BufferedInputStream(client.retrieveFileStream(entry.getPath().toString()))
			{
				@Override
				public void close() throws IOException {
					client.completePendingCommand();
					super.close();			
					client.logout();
					client.disconnect();
					assertPositiveCompletion(client);
				}	
			};
		} catch (final IOException e) {
			// TODO add proper error handling
			throw new RuntimeException(e);
		}
	}
	
	private final FTPClient connectedClient() {
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
	
	private final FTPClient newClient() throws Exception {	
		
		if (!"ftps".equals(uri.getScheme())) {
			final FTPClient ftpClient = new FTPClient();
			
			ftpClient.addProtocolCommandListener(
					new PrintCommandListener(new LogPrintWriter(s -> LOG.debug(s)), true)
			);
			ftpClient.setConnectTimeout(config.getConnectTimeoutSec()*1000);
			connect(ftpClient);
			return ftpClient;
		}	
		else {
			// FTPS client creation			
			final FTPSClient ftpsClient = new FTPSClient(config.getSslProtocol(), !config.isExplictFtps());
			ftpsClient.setConnectTimeout(config.getConnectTimeoutSec());
			ftpsClient.addProtocolCommandListener(
					new PrintCommandListener(new LogPrintWriter(s -> LOG.debug(s)), true)
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

			connect(ftpsClient);	    
		    ftpsClient.execPBSZ(0);
	        assertPositiveCompletion(ftpsClient);
	        
		    ftpsClient.execPROT("P");
		    
		    return ftpsClient;
		}
	}

	private final KeyStore newKeyStore(final InputStream inputStream, final String keystorePass) 
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

	private final FTPClient connect(final FTPClient ftpClient) throws SocketException, IOException {
		if (uri.getPort() == -1) {
			ftpClient.connect(config.getServerName());
		}
		else {
			ftpClient.connect(config.getServerName(), uri.getPort());
		}	
		assertPositiveCompletion(ftpClient);
		return ftpClient;
	}
	
	private final List<EdipEntry> listRecursively(
			final FTPClient client, 
			final Path path, 
			final EdipEntryFilter filter
	) 
			throws IOException {
		final List<EdipEntry> result = new ArrayList<>();
		LOG.trace("Recursive listing of {}", path);
		
		for (final FTPFile ftpFile : client.listFiles(path.toString())) {
			final EdipEntry entry = toEdipEntry(path, ftpFile);
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
	
	private final EdipEntry toEdipEntry(final Path path, final FTPFile ftpFile) {
		final Path entryPath = path.resolve(ftpFile.getName());

		return new EdipEntryImpl(
				ftpFile.getName(), 
				entryPath, 
				toUri(entryPath), 
				ftpFile.getTimestamp().getTime(), 
				ftpFile.getSize()
		);
	}
	
	private final URI toUri(final Path entryPath) {		
		return uri.resolve(entryPath.toString());		
	}

	static void assertPositiveCompletion(final FTPClient client) throws IOException {
		if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
			throw new IOException("Error on command execution. Reply was: " + client.getReplyString());
		}
	}

}
