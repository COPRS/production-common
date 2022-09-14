package esa.s1pdgs.cpoc.ebip.client.apacheftp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.ebip.client.EdipClient;
import esa.s1pdgs.cpoc.ebip.client.EdipEntry;
import esa.s1pdgs.cpoc.ebip.client.EdipEntryFilter;
import esa.s1pdgs.cpoc.ebip.client.config.EdipClientConfigurationProperties.EdipHostConfiguration;

public class ApacheFtpEdipClient extends AbstractApacheFtpClient implements EdipClient {
	static final Logger LOG = LogManager.getLogger(ApacheFtpEdipClient.class);

    private final boolean directoryListing;
	
	public ApacheFtpEdipClient(final EdipHostConfiguration config, final URI uri, final boolean directoryListing) {
		super(config, uri);
		this.directoryListing = directoryListing;
	}

	@Override
	public final List<EdipEntry> list(final EdipEntryFilter filter) throws IOException {
		LOG.debug("Listing {}", uri.getPath());
		final Path uriPath = Paths.get(uri.getPath());
		
		final FTPClient client = connectedClient();
		
		List<EdipEntry> result = new ArrayList<>();
		
		/*
		 * S1OPS-582: Dirty workaround for Plan & Report QC files listing 
		 */
		if (directoryListing) {
			result = listDirectory(client, uriPath, filter);
		} else {
		
			result = getIfNotDirectory(client, uriPath)
					.orElse(listRecursively(client, uriPath, filter));
		}
		
		client.logout();
		client.disconnect();
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
	
	private Optional<List<EdipEntry>> getIfNotDirectory(final FTPClient client, final Path path) throws IOException {
		if (null == path) {
			throw new RuntimeException(String.format("Cannot list path: %s", path));
		}
		final List<FTPFile> ftpFiles = Arrays.asList(client.listFiles(path.toString()));
		final FTPFile ftpFile = ftpFiles.size() == 1 ? ftpFiles.get(0) : null;
		if (isNotDirectory(path, ftpFile)) {
			final EdipEntry edipEntry = toEdipEntry(path.getParent(), ftpFile);
			return Optional.of(Collections.singletonList(edipEntry));
		}
		return Optional.empty();
	}
	
	private List<EdipEntry> listDirectory(final FTPClient client, final Path path, final EdipEntryFilter filter)
			throws IOException {
		final List<EdipEntry> result = new ArrayList<>();

		for (final FTPFile ftpFile : client.listFiles(path.toString())) {
			final EdipEntry entry = toEdipEntry(path, ftpFile);
			// System.err.println("FOO " + entry);
			if (!filter.accept(entry)) {
				LOG.trace("{} ignored by {}", entry, filter);
				continue;
			}

			if (ftpFile.isDirectory()) {
				result.add(entry);
			}
		}
		return result;
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
			
			// dirty workaround for '..' issue:
			if (ftpFile.getName().startsWith("..")) {
				LOG.trace("Ignoring {}", entry);
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

}
