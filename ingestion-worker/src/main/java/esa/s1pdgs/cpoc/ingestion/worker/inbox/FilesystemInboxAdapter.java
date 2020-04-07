package esa.s1pdgs.cpoc.ingestion.worker.inbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.ingestion.worker.config.IngestionWorkerServiceConfigurationProperties;

public final class FilesystemInboxAdapter implements InboxAdapter {
	static final Logger LOG = LogManager.getLogger(FilesystemInboxAdapter.class); 

	private final IngestionWorkerServiceConfigurationProperties properties;
		
	public FilesystemInboxAdapter(final IngestionWorkerServiceConfigurationProperties properties) {
		this.properties = properties;
	}

	@Override
	public final List<InboxAdapterEntry> read(final URI uri, final String name) throws Exception {	
		final Path basePath = Paths.get(uri);
		
		return Files.walk(basePath, FileVisitOption.FOLLOW_LINKS)
			.map(Path::toFile)
			.filter(f -> !f.isDirectory())
			.map(f -> toInboxAdapterEntry(basePath.getParent(),f))
			.collect(Collectors.toList());
	}
	
	@Override
	public final void delete(final URI uri) throws Exception {
		final File file = Paths.get(uri)
				.toFile();
		
		if (file.exists()) {
			LOG.debug("Deleting file {}", file);
			FileUtils.deleteWithRetries(
					file, 
					properties.getMaxRetries(), 
					properties.getTempoRetryMs()
			);
		}
	}
	
	@Override
	public final String toString() {
		return "FilesystemInboxAdapter";
	}
	
	private final InboxAdapterEntry toInboxAdapterEntry(final Path parent, final File file) {
		return new InboxAdapterEntry(parent.resolve(file.toPath()).toString(), toInputStream(file), file.length());
	}

	static final InputStream toInputStream(final File file){
		try {
			return new FileInputStream(file);
		} catch (final FileNotFoundException e) {
			throw new RuntimeException(
					String.format("Could not create FileInputStream for %s: %s", file, e.getMessage()), 
					e
			);
		}
	}	
}
