package esa.s1pdgs.cpoc.disseminator.outbox;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;

public final class FtpsOutboxClient extends AbstractOutboxClient {
	public static final class Factory implements OutboxClient.Factory {
		@Override
		public OutboxClient newClient(ObsClient obsClient, OutboxConfiguration config) {
			return new FtpsOutboxClient(obsClient, config);
		}			
	}
	
	private static final Logger LOG = LogManager.getLogger(FtpsOutboxClient.class);
	
	private static final int DEFAULT_PORT = 990;
	
	FtpsOutboxClient(ObsClient obsClient, OutboxConfiguration config) {
		super(obsClient, config);
	}

	@Override
	public void transfer(final ObsObject obsObject) throws Exception {
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
		
		final int port = (config.getPort() > 0) ? config.getPort(): DEFAULT_PORT;
		ftpsClient.connect(config.getHostname(), port);
	    assertPositiveCompletion(ftpsClient);
	    
	    ftpsClient.execPBSZ(0);
        assertPositiveCompletion(ftpsClient);
        
	    ftpsClient.execPROT("P");
        assertPositiveCompletion(ftpsClient);
        
        if (!ftpsClient.login(config.getUsername(), config.getPassword())) {
        	throw new RuntimeException("Could not authenticate user " + config.getUsername());
        }
        
    	try {
	        ftpsClient.setFileType(FTP.BINARY_FILE_TYPE);
	        ftpsClient.enterLocalPassiveMode();
	        assertPositiveCompletion(ftpsClient);
	        
	        final Path remoteDir = Paths.get(config.getPath());
			final Map<String, InputStream> elements = obsClient.getAllAsInputStream(obsObject.getFamily(), obsObject.getKey());
    		
    		for (final Map.Entry<String, InputStream> entry : elements.entrySet()) {
    			final String path = entry.getKey();		    		
    			Utils.assertValidPath(path);
    			
    			final Path dest = remoteDir.resolve(path);	
    			String currentPath = "";
    			
    			final Path parentPath = dest.getParent();    			
    			if (parentPath == null) {
    				throw new RuntimeException("Invalid destination " + dest);
    			}    				    			
    			// create parent directories if required
    			for (final Path pathElement : parentPath) {
    				currentPath = currentPath + "/" + pathElement;
    	 	    	 			
	 				LOG.debug("current path is {}", currentPath);
	 				
	 				boolean directoryExists = ftpsClient.changeWorkingDirectory(currentPath);
	 				if (directoryExists) {
	 					continue;
	 				}
	 				LOG.debug("creating directory {}", currentPath);
	 				ftpsClient.makeDirectory(currentPath);
	 				assertPositiveCompletion(ftpsClient);	    	 
    			}		    
    			
    			try (final InputStream in = entry.getValue()) {
    				LOG.info("Uploading {} to {}", path, dest);
    				ftpsClient.storeFile(dest.toString(), in);
    				assertPositiveCompletion(ftpsClient);	    				
    			}
    		}
    	}
    	finally { 
    		try {
    			ftpsClient.logout();
	            assertPositiveCompletion(ftpsClient);
    		}
    		finally {
    			ftpsClient.disconnect();
    			assertPositiveCompletion(ftpsClient);
    		}
    	}
	}	
	
	static final KeyStore newKeyStore(final InputStream in, final String password)
			throws Exception {
		final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(in, password.toCharArray());
		return keystore;
	}
	
	static final void assertPositiveCompletion(final FTPSClient client) throws IOException {
		if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
			throw new IOException("Error on command execution. Reply was: " + client.getReplyString());
		}
	}
}