package esa.s1pdgs.cpoc.xbip.client;

import java.net.URI;
import java.nio.file.Path;
import java.util.Date;

public final class XbipEntryImpl implements XbipEntry {	
	private final String name;
	private final Path path;
	private final URI uri;
	private final Date lastModified;

	public XbipEntryImpl(
			final String name, 
			final Path path, 
			final URI uri, 
			final Date lastModified
	) {
		this.name = name;
		this.path = path;
		this.uri = uri;
		this.lastModified = lastModified;
	}

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final Path getPath() {
		return path;
	}

	@Override
	public final URI getUri() {
		return uri;
	}

	@Override
	public final Date getLastModified() {
		return lastModified;
	}

}
