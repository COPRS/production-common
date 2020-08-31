package esa.s1pdgs.cpoc.xbip.client.sardine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;

import esa.s1pdgs.cpoc.xbip.client.XbipClient;
import esa.s1pdgs.cpoc.xbip.client.XbipEntry;
import esa.s1pdgs.cpoc.xbip.client.XbipEntryFilter;
import esa.s1pdgs.cpoc.xbip.client.XbipEntryImpl;

public class SardineXbipClient implements XbipClient {		
	static final Logger LOG = LogManager.getLogger(SardineXbipClient.class);
			
	private final Sardine sardine;
	private final URI url;
	private final boolean programmaticRecursion;

	SardineXbipClient(final Sardine sardine, final URI url, final boolean programmaticRecursion) {
		this.sardine = sardine;
		this.url = url;
		this.programmaticRecursion = programmaticRecursion;
	}
	
	@Override
	public final List<XbipEntry> list(final XbipEntryFilter filter) throws IOException {
		// S1PRO-1847: special case if infinity depth is disabled on server
		// each subdirectory needs to be traversed recursively
		if (programmaticRecursion) {
			LOG.info("Performing programmatic recursion on {}", url);
			return listAllRecursively(url.toString(), filter);
		}
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
	
	// S1PRO-1847: Programmatic server tree traversal
	private final List<XbipEntry> listAllRecursively(final String url, final XbipEntryFilter filter) throws IOException {
		final List<XbipEntry> result = new ArrayList<>();
		for (final DavResource davResource : sardine.list(url.toString(), 1)) {
			// ignore hidden files (like PIC)
			if (davResource.getName().startsWith(".")) {
				LOG.trace("Ignoring hidden {}", davResource.getName());
				continue;
			}			
			final URI uri = toUri(davResource);
			
			if (davResource.isDirectory()) {
				LOG.trace("Scanning subdirectory {}", davResource.getName());
				result.addAll(listAllRecursively(uri.toString(), filter));
				continue;
			}
			
			final XbipEntry entry = toXbipEntry(davResource);
			
			if (!filter.accept(entry)) {
				LOG.trace("Ignoring filtered entry {}", davResource.getName());
				continue;
			}
			LOG.info("Found entry {}", davResource.getName());
			result.add(entry);
		}
		return result;
	}

}
