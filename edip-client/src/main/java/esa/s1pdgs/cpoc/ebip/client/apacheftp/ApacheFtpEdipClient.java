package esa.s1pdgs.cpoc.ebip.client.apacheftp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.URI;
import java.nio.file.Path;
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
