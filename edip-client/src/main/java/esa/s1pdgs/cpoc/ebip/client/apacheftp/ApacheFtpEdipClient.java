package esa.s1pdgs.cpoc.ebip.client.apacheftp;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.TrustManager;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
		return listRecursively(connectedClient(), uri.getPath(), filter);
	}
	
	@Override
	public final InputStream read(final EdipEntry entry) {
		try {
			return connectedClient().retrieveFileStream(entry.getPath().toString());
		} catch (final IOException e) {
			// TODO add proper error handling
			throw new RuntimeException(e);
		}
	}
	
	private final FTPClient connectedClient() {
		try {
			final FTPClient ftpClient = newClient();

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
		} catch (final IOException e) {
			// TODO add proper error handling
			throw new RuntimeException(e);
		}
	}
	
	private final FTPClient newClient() throws IOException {	
		
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
			// FIXME make certificates and checks configurable
			final TrustManager trustManager;
			if (config.isTrustSelfSignedCertificate()) {
				trustManager = TrustManagerUtils.getAcceptAllTrustManager();
			}
			else {
				trustManager = TrustManagerUtils.getValidateServerCertificateTrustManager();
			}
		    ftpsClient.setTrustManager(trustManager);

			connect(ftpsClient);	    
		    ftpsClient.execPBSZ(0);
	        assertPositiveCompletion(ftpsClient);
	        
		    ftpsClient.execPROT("P");
		    
		    return ftpsClient;
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
	
	private List<EdipEntry> listRecursively(
			final FTPClient client, 
			final String path, 
			final EdipEntryFilter filter
	) 
			throws IOException {
		final List<EdipEntry> result = new ArrayList<>();
		System.err.println("Recursive listing of " + path);
		for (final FTPFile ftpFile : client.listFiles(path)) {
			final EdipEntry entry = toEdipEntry(ftpFile);
			if (!filter.accept(entry)) {
				System.err.println("filtered " + path);
				continue;
			}			
			if (ftpFile.isDirectory()) {
				System.err.println("Dir " + path);
				result.addAll(listRecursively(client, ftpFile.getName(), filter));
			}
			else {
				System.err.println("File " + path);
				result.add(toEdipEntry(ftpFile));
			}
		}
		return result;
	}
	
	private final EdipEntry toEdipEntry(final FTPFile ftpFile) {
		return new EdipEntryImpl(
				ftpFile.getName(), 
				Paths.get(ftpFile.getName()), 
				null, 
				ftpFile.getTimestamp().getTime(), 
				ftpFile.getSize()
		);
	}
	
	static void assertPositiveCompletion(final FTPClient client) throws IOException {
		if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
			throw new IOException("Error on command execution. Reply was: " + client.getReplyString());
		}
	}

}
