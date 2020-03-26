package esa.s1pdgs.cpoc.xbip.client.sardine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;

import esa.s1pdgs.cpoc.xbip.client.XbipClient;
import esa.s1pdgs.cpoc.xbip.client.XbipEntry;
import esa.s1pdgs.cpoc.xbip.client.XbipEntryFilter;
import esa.s1pdgs.cpoc.xbip.client.XbipEntryImpl;

public class SardineXbipClient implements XbipClient {	
	private final Sardine sardine;
	private final String url;

	SardineXbipClient(final Sardine sardine, final String url) {
		this.sardine = sardine;
		this.url = url;
	}
	
	@Override
	public final List<XbipEntry> list(final XbipEntryFilter filter) throws IOException {
		return sardine.list(url).stream()
				.map(r -> toXbipEntry(r))
				.filter(e -> filter.accept(e))
				.collect(Collectors.toList());
	}

	@Override
	public final InputStream read(final XbipEntry entry) throws IOException {
		return sardine.get(entry.getUri().toString());
	}
	
	private final XbipEntry toXbipEntry(final DavResource davResource) {
		return new XbipEntryImpl(
				davResource.getName(), 
				Paths.get(davResource.getPath()), 
				davResource.getHref(), 
				davResource.getModified()
		);
	}
}
