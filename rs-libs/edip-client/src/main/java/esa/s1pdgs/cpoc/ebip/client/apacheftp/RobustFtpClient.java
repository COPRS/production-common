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
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.ebip.client.EdipClient;
import esa.s1pdgs.cpoc.ebip.client.EdipEntry;
import esa.s1pdgs.cpoc.ebip.client.EdipEntryFilter;
import esa.s1pdgs.cpoc.ebip.client.config.EdipClientConfigurationProperties.EdipHostConfiguration;

public class RobustFtpClient extends AbstractApacheFtpClient implements EdipClient {

	static final Logger LOG = LogManager.getLogger(RobustFtpClient.class);

	private final boolean directoryListing;

	private final int timeoutSec;

	private final CompletionService<List<FTPFile>> completionServiceList;
	private final CompletionService<Boolean> completionServiceAbort;
	private final ExecutorService executor;

	public RobustFtpClient(final EdipHostConfiguration config, final URI uri, final boolean directoryListing) {
		super(config, uri);
		this.directoryListing = directoryListing;
		this.timeoutSec = config.getListingTimeoutSec();
		this.executor = Executors.newSingleThreadExecutor();
		this.completionServiceList = new ExecutorCompletionService<>(executor);
		this.completionServiceAbort = new ExecutorCompletionService<>(executor);
	}

	@Override
	public List<EdipEntry> list(EdipEntryFilter filter) throws IOException {
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

			result = getIfNotDirectory(client, uriPath).orElse(listRecursively(client, uriPath, filter));
		}

		shutdownExecution();
		try {
			client.logout();
		} catch (IOException e) {
			LOG.warn("Error when try to logout: {}", e.getMessage());
		}
		client.disconnect();
		return result;
	}

	@Override
	public InputStream read(EdipEntry entry) {
		try {
			final FTPClient client = connectedClient();
			final InputStream res = new BufferedInputStream(client.retrieveFileStream(entry.getPath().toString())) {
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

	private List<EdipEntry> listDirectory(final FTPClient client, final Path path, final EdipEntryFilter filter) {
		final List<EdipEntry> result = new ArrayList<>();

		for (final FTPFile ftpFile : this.listFiles(client, path)) {
			final EdipEntry entry = toEdipEntry(path, ftpFile);
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

	private Optional<List<EdipEntry>> getIfNotDirectory(final FTPClient client, final Path path) throws IOException {
		if (null == path) {
			throw new RuntimeException(String.format("Cannot list path: %s", path));
		}
		final List<FTPFile> ftpFiles = this.listFiles(client, path);
		final FTPFile ftpFile = ftpFiles.size() == 1 ? ftpFiles.get(0) : null;
		if (isNotDirectory(path, ftpFile)) {
			final EdipEntry edipEntry = toEdipEntry(path.getParent(), ftpFile);
			return Optional.of(Collections.singletonList(edipEntry));
		}
		return Optional.empty();
	}

	private List<EdipEntry> listRecursively(final FTPClient ftpClient, final Path path, final EdipEntryFilter filter) {
		final List<EdipEntry> result = new ArrayList<>();
		LOG.trace("Recursive listing of {}", path);

		for (FTPFile f : this.listFiles(ftpClient, path)) {

			final EdipEntry entry = toEdipEntry(path, f);
			if (!filter.accept(entry)) {
				LOG.trace("{} ignored by {}", entry, filter);
				continue;
			}

			// dirty workaround for '..' issue:
			if (f.getName().startsWith("..")) {
				LOG.trace("Ignoring {}", entry);
				continue;
			}
			if (f.isDirectory()) {
				LOG.trace("Found dir {}", entry);
				result.addAll(this.listRecursively(ftpClient, entry.getPath(), filter));
			} else {
				LOG.trace("Found file {}", entry);
				result.add(entry);
			}
		}
		return result;
	}

	private List<FTPFile> listFiles(final FTPClient ftpClient, final Path path) {

		List<FTPFile> collectedFiles = new ArrayList<>();

		try {
			completionServiceList.submit(listCall(ftpClient, path));
			ftpClient.setSoTimeout(timeoutSec * 1000);
			Future<List<FTPFile>> result = completionServiceList.poll(timeoutSec, TimeUnit.SECONDS);
			if (result != null) {
				if (!result.isCancelled()) {
					collectedFiles = result.get();
				}
			} else {
				LOG.warn("Timeout while listing files in path {}", path);
				completionServiceAbort.submit(abortCall(ftpClient));
				Future<Boolean> abortResult = completionServiceAbort.poll(timeoutSec, TimeUnit.SECONDS);
				if (abortResult != null) {
					LOG.warn("List command aborted");
				} else {
					LOG.warn("Timeout while aborting list command");
				}
			}
		} catch (Exception e) {
			LOG.warn("Error while listing {}: {}", path, e.getMessage());
		}
		return collectedFiles;
	}

	private void shutdownExecution() {
		executor.shutdownNow();
	}

	private Callable<List<FTPFile>> listCall(final FTPClient ftpClient, final Path path) {
		return () -> {
			return (Arrays.asList(ftpClient.listFiles(path.toString())));
		};
	}

	private Callable<Boolean> abortCall(final FTPClient ftpClient) {
		return () -> {
			return ftpClient.abort();
		};
	}

}
