package esa.s1pdgs.cpoc.auxip.client.odata;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetIteratorRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientEntitySetIterator;
import org.apache.olingo.client.api.uri.FilterArg;
import org.apache.olingo.client.api.uri.FilterArgFactory;
import org.apache.olingo.client.api.uri.FilterFactory;
import org.apache.olingo.client.api.uri.URIBuilder;
import org.apache.olingo.client.api.uri.URIFilter;
import org.springframework.lang.NonNull;

import esa.s1pdgs.cpoc.auxip.client.AuxipClient;
import esa.s1pdgs.cpoc.auxip.client.AuxipProductMetadata;
import esa.s1pdgs.cpoc.auxip.client.config.AuxipClientConfigurationProperties.AuxipHostConfiguration;
import esa.s1pdgs.cpoc.common.utils.DateUtils;

/**
 * OData implementation of the AUXIP client
 */
public class AuxipOdataClient implements AuxipClient {		
	static final Logger LOG = LogManager.getLogger(AuxipOdataClient.class);
			
	private final ODataClient odataClient;
	private final AuxipHostConfiguration hostConfig;
	private final String entitySetName;
	private final String creationDateAttrName;
	private final String productNameAttrName;
	private final String idAttrName;
	
	// --------------------------------------------------------------------------

	AuxipOdataClient(final ODataClient odataClient, final AuxipHostConfiguration hostConfig, final String entitySetName) {
		this.odataClient = Objects.requireNonNull(odataClient, "OData client must not be null!");
		this.hostConfig = Objects.requireNonNull(hostConfig, "host configuration must not be null!");
		this.entitySetName = Objects.requireNonNull(entitySetName, "entity set name must not be null!");
		this.creationDateAttrName = Objects.requireNonNull(hostConfig.getCreationDateAttributeName(),
				"creation date attribute name must not be null!");
		this.productNameAttrName = Objects.requireNonNull(hostConfig.getProductNameAttrName(),
				"product name attribute name must not be null!");
		this.idAttrName = Objects.requireNonNull(hostConfig.getIdAttrName(), "id attribute name must not be null!");
	}

	// --------------------------------------------------------------------------
	
//	@Override
//	public List<AuxipProductMetadata> getMetadata(@NonNull LocalDateTime from, @NonNull LocalDateTime to, Integer top,
//			Integer skip, Collection<String> productNameContains) {
//		final List<URIFilter> filters = this.buildFilters(from, to);
//		final URI queryUri = this.buildQueryUri(filters, top, skip);
//		final ClientEntitySetIterator<ClientEntitySet, ClientEntity> response = this.readEntities(queryUri);
//		// TODO @MSc: filter (AuxipProductFilters, java filtering) and map response to
//		// AuxipProductMetadata
//		return null;
//	}
	
	@Override
	public List<AuxipProductMetadata> getMetadata(@NonNull LocalDateTime from, @NonNull LocalDateTime to,
			Integer pageSize, Integer offset) {
		return this.getMetadata(from, to, pageSize, offset, null);
	}
	
	@Override
	public List<AuxipProductMetadata> getMetadata(@NonNull LocalDateTime from, @NonNull LocalDateTime to,
			Integer pageSize, Integer offset, String productNameContains) {
		final URIFilter filters = this.buildFilters(from, to, productNameContains);
		
		final URI queryUri = this.buildQueryUri(Collections.singletonList(filters), pageSize, offset);
		final ClientEntitySetIterator<ClientEntitySet, ClientEntity> response = this.readEntities(queryUri);
		// TODO @MSc: map response to AuxipProductMetadata
		
		return null;
	}
	
	// --------------------------------------------------------------------------
	
	private URIFilter buildFilters(LocalDateTime from, LocalDateTime to, String productNameContains) {
		final FilterFactory filterFactory = this.odataClient.getFilterFactory();

		// timeframe filter
		final URIFilter lowerBoundFilter = filterFactory.ge(this.creationDateAttrName,
				DateUtils.formatToOdataDateTimeFormat(from));
		final URIFilter upperBoundFilter = filterFactory.lt(this.creationDateAttrName,
				DateUtils.formatToOdataDateTimeFormat(to));
		final URIFilter timeframeFilter = filterFactory.and(lowerBoundFilter, upperBoundFilter);

		// product name filter
		URIFilter productNameFilter = null;
		if (null != productNameContains && !productNameContains.isEmpty()) {
			final FilterArgFactory argFactory = filterFactory.getArgFactory();
			productNameFilter = filterFactory.match(argFactory.contains(argFactory.property(this.productNameAttrName),
					argFactory.literal(productNameContains)));
		}

		if (null != productNameFilter) {
			return filterFactory.and(timeframeFilter, productNameFilter);
		} else {
			return timeframeFilter;
		}
	}
	
	private URI buildQueryUri(final List<URIFilter> filters, final Integer pageSize, final Integer offset) {
		final URIBuilder uriBuilder = this.odataClient.newURIBuilder(this.hostConfig.getServiceRootUri())
				.appendEntitySetSegment(this.entitySetName);

		if (null != filters && !filters.isEmpty()) {
			filters.forEach(f -> uriBuilder.filter(f));
		}

		if (null != pageSize) {
			uriBuilder.top(pageSize);
		}

		if (null != offset) {
			uriBuilder.skip(offset);
		}
		
		uriBuilder.orderBy(this.creationDateAttrName + " asc");

		return uriBuilder.build();
	}
	
	private ClientEntitySetIterator<ClientEntitySet, ClientEntity> readEntitiesWithFilter(final List<URIFilter> filters,
			final Integer top, final Integer skip) {
		return this.readEntities(this.buildQueryUri(filters, top, skip));
	}
	
	private ClientEntitySetIterator<ClientEntitySet, ClientEntity> readEntities(final URI absoluteUri) {
		final ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> request = this.odataClient
				.getRetrieveRequestFactory().getEntitySetIteratorRequest(absoluteUri);
		request.setAccept("application/json");
		final ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> response = request
				.execute();

		return response.getBody();
	}
	
//	@Override
//	public final List<XbipEntry> list(final XbipEntryFilter filter) throws IOException {
//		// S1PRO-1847: special case if infinity depth is disabled on server
//		// each subdirectory needs to be traversed recursively
//		if (programmaticRecursion) {
//			LOG.debug("Performing programmatic recursion on {}", url);
//			return listAllRecursively(url.toString(), filter);
//		}
//		return sardine.list(url.toString(), -1).stream()
//				.filter(r -> !r.isDirectory())
//				.map(r -> toXbipEntry(r))
//				.filter(e -> filter.accept(e))
//				.collect(Collectors.toList());
//	}

//	private final XbipEntry toXbipEntry(final DavResource davResource) {			
//		return new XbipEntryImpl(
//				davResource.getName(), 
//				Paths.get(davResource.getPath()), 
//				toUri(davResource), 
//				davResource.getModified(),
//				davResource.getContentLength()
//		);
//	}
	
//	private final URI toUri(final DavResource davResource) {
//		// ok, some servers (like our test server) only return the absolute path as an URI.
//		// So this is a workaround to fix such conditions
//		if (!davResource.getHref().toString().startsWith(url.toString())) {
//			try {
//				return new URIBuilder(url)
//						.setPath(davResource.getHref().toString())
//						.build();
//			} catch (final URISyntaxException e) {
//				throw new IllegalArgumentException(
//						String.format("Could not create URI for %s: %s", davResource.getHref(), e.getMessage()), 
//						e
//				);
//			}
//		}
//		return davResource.getHref();
//	}
	
	// S1PRO-1847: Programmatic server tree traversal
//	private final List<XbipEntry> listAllRecursively(final String url, final XbipEntryFilter filter) throws IOException {
//		final List<XbipEntry> result = new ArrayList<>();
//		for (final DavResource davResource : sardine.list(url.toString(), 1)) {
//			// ignore hidden files (like PIC)
//			if (davResource.getName().startsWith(".")) {
//				LOG.trace("Ignoring hidden {}", davResource.getName());
//				continue;
//			}			
//			final URI uri = toUri(davResource);
//			
//			// ignore own URL (like PIC) on recursion
//			if (davResource.isDirectory() && !uri.toString().equals(url)) {
//				LOG.trace("Scanning subdirectory {}", davResource.getName());
//				result.addAll(listAllRecursively(uri.toString(), filter));
//				continue;
//			}
//			
//			final XbipEntry entry = toXbipEntry(davResource);
//			
//			if (!filter.accept(entry)) {
//				LOG.trace("Ignoring filtered entry {}", davResource.getName());
//				continue;
//			}
//			LOG.debug("Found entry {}", davResource.getName());
//			result.add(entry);
//		}
//		return result;
//	}

}
