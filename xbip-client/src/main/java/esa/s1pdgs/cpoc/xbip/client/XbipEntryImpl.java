package esa.s1pdgs.cpoc.xbip.client;

import java.net.URI;
import java.nio.file.Path;
import java.util.Date;

public final class XbipEntryImpl implements XbipEntry {	
	private final String name;
	private final Path path;
	private final URI uri;
	private final Date lastModified;
	private final long size;

	public XbipEntryImpl(final String name, final Path path, final URI uri, final Date lastModified, final long size) {
		this.name = name;
		this.path = path;
		this.uri = uri;
		this.lastModified = lastModified;
		this.size = size;
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

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public String toString() {
		return "XbipEntryImpl [name=" + name + ", path=" + path + ", uri=" + uri + ", lastModified=" + lastModified
				+ ", size=" + size + "]";
	}
}
