package esa.s1pdgs.cpoc.xbip.client.sardine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.client.utils.URIBuilder;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;

import esa.s1pdgs.cpoc.xbip.client.XbipClient;
import esa.s1pdgs.cpoc.xbip.client.XbipEntry;
import esa.s1pdgs.cpoc.xbip.client.XbipEntryFilter;
import esa.s1pdgs.cpoc.xbip.client.XbipEntryImpl;

public class SardineXbipClient implements XbipClient {	
	private final Sardine sardine;
	private final URI url;

	SardineXbipClient(final Sardine sardine, final URI url) {
		this.sardine = sardine;
		this.url = url;
	}
	
	@Override
	public final List<XbipEntry> list(final XbipEntryFilter filter) throws IOException {
		return sardine.list(url.toString(), -1).stream()
				.filter(r -> !r.isDirectory())
				.map(r -> toXbipEntry(r))
				.filter(e -> filter.accept(e))
				.collect(Collectors.toList());
	}

	@Override
	public final InputStream read(final XbipEntry entry) {
		try {
			return sardine.get(entry.getUri().toString());
		} catch (final IOException e) {
			throw new RuntimeException(
					String.format("Error on retrieving input stream for %s: %s", entry, e.getMessage()),
					e
			);
		}
	}
	
	private final XbipEntry toXbipEntry(final DavResource davResource) {			
		return new XbipEntryImpl(
				davResource.getName(), 
				Paths.get(davResource.getPath()), 
				toUri(davResource), 
				davResource.getModified(),
				davResource.getContentLength()
		);
	}
	
	private final URI toUri(final DavResource davResource) {
		// ok, some servers (like our test server) only return the absolute path as an URI.
		// So this is a workaround to fix such conditions
		if (!davResource.getHref().toString().startsWith(url.toString())) {
			try {
				return new URIBuilder(url)
						.setPath(davResource.getHref().toString())
						.build();
			} catch (final URISyntaxException e) {
				throw new IllegalArgumentException(
						String.format("Could not create URI for %s: %s", davResource.getHref(), e.getMessage()), 
						e
				);
			}
		}
		return davResource.getHref();
	}
}
