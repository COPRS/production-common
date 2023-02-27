package de.werum.coprs.nativeapi.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import de.werum.coprs.nativeapi.config.NativeApiProperties;
import de.werum.coprs.nativeapi.config.NativeApiProperties.StacCollectionProperties;
import de.werum.coprs.nativeapi.rest.model.stac.StacCatalog;
import de.werum.coprs.nativeapi.rest.model.stac.StacCollection;
import de.werum.coprs.nativeapi.rest.model.stac.StacItemCollection;
import de.werum.coprs.nativeapi.rest.model.stac.StacLink;
import de.werum.coprs.nativeapi.rest.model.stac.StacRootCatalog;

@Controller
public class NativeAPIServiceImpl {
	private static final Logger LOG = LogManager.getLogger(NativeAPIServiceImpl.class);

	private static final String PARAM_PAGE = "page";

	private static final String STACSPEC_CORE = "https://api.stacspec.org/v1.0.0-rc.1/core";

	@Autowired
	private NativeApiProperties properties;

	@Autowired
	private StatementParserServiceImpl parser;

	@Autowired
	private ODataBackendServiceImpl backend;

	private Map<String, String> flattenParameters(Map<String, String[]> parameterMap) {
		Map<String, String> results = new HashMap<>();

		parameterMap.entrySet().stream().forEach(e -> {
			if (e.getValue().length > 1) {
				LOG.warn("Parameter '{}' occurs more than one, just first occurance will be evaluated", e.getKey());
			}
			results.put(e.getKey(), e.getValue()[0]);
		});

		return results;
	}

	public StacItemCollection processSearchRequest(final HttpServletRequest request) {
		return processSearchRequest(flattenParameters(request.getParameterMap()));
	}

	public StacItemCollection processSearchRequest(final Map<String, String> parameters) {
		LOG.debug("Identified {} parameters for search request: {}", parameters.size(), parameters);

		// extract pagination parameter if existing and remove it from the parameter as
		// not intended to be processed by the filter
		int page = 0;
		String pageParam = parameters.get(PARAM_PAGE);
		if (pageParam != null) {
			parameters.remove(PARAM_PAGE);
			page = Integer.parseInt(pageParam);
		}

		// build the query for the OData backend
		String oDataQuery = parser.buildOdataQuery(parameters);
		if (oDataQuery == null || oDataQuery.isEmpty()) {
			throw new IllegalArgumentException("Unable to generate a request from the parameters and look up table");
		}

		LOG.debug("OData query generated: {}", oDataQuery);

		// send the query to the backend and convert to stac
		String queryUrl = backend.buildPripQueryUrl(oDataQuery, true, page);
		StacItemCollection result = backend.queryOData(queryUrl);
		return result;
	}

	/**
	 * Creates the static landing page for the STAC interface
	 */
	public StacRootCatalog getLandingPage() {
		StacRootCatalog rootCatalog = new StacRootCatalog();

		rootCatalog.setId(properties.getRootCatalogId());
		rootCatalog.setTitle(properties.getRootCatalogTitle());
		rootCatalog.setDescription(properties.getRootCatalogDescription());

		List<String> conformsTo = new ArrayList<>();
		conformsTo.add(STACSPEC_CORE);
		rootCatalog.setConformsTo(conformsTo);

		rootCatalog.getLinks().add(
				new StacLink("self", properties.getHostname(), "application/json", properties.getRootCatalogTitle()));
		rootCatalog.getLinks().add(
				new StacLink("root", properties.getHostname(), "application/json", properties.getRootCatalogTitle()));
		
		for (String subCatalog : extractSubCatalogs()) {
			rootCatalog.getLinks().add(new StacLink("child", properties.getHostname() + "/" + subCatalog,
					"application/json", "Catalog for " + subCatalog));
		}
		
		rootCatalog.getLinks().add(new StacLink("search", properties.getHostname() + "/search", "application/geo+json",
				"STAC search endpoint"));

		return rootCatalog;
	}

	/**
	 * Creates the static collections page for the STAC interface
	 */
	public StacCatalog getSubCatalogPage(final String catalogName) {
		if (extractSubCatalogs().contains(catalogName)) {
			StacCatalog catalog = new StacCatalog();

			catalog.setId(catalogName);
			catalog.setTitle("Catalog for " + catalogName);
			catalog.setDescription("Groups all collections for " + catalogName);

			catalog.getLinks().add(new StacLink("self", properties.getHostname() + "/" + catalogName,
					"application/json", "Catalog for " + catalogName));
			catalog.getLinks().add(new StacLink("root", properties.getHostname(), "application/json",
					properties.getRootCatalogTitle()));
			catalog.getLinks().add(new StacLink("child", properties.getHostname() + "/" + catalogName + "/collections",
					"application/json", "Collection list for " + catalogName));

			return catalog;
		}

		return null;
	}

	/**
	 * Creates the static collections page for the STAC interface
	 */
	public StacCatalog getCollectionsPage(final String catalogName) {
		if (extractSubCatalogs().contains(catalogName)) {
			StacCatalog catalog = new StacCatalog();
	
			catalog.setId("collections");
			catalog.setTitle("Collection list for " + catalogName);
			catalog.setDescription("Lists all available collections of " + catalogName);
	
			catalog.getLinks().add(new StacLink("self", properties.getHostname() + "/" + catalogName + "/collections",
					"application/json", "Collection list"));
			catalog.getLinks().add(
					new StacLink("root", properties.getHostname(), "application/json", properties.getRootCatalogTitle()));
			catalog.getLinks().add(new StacLink("search", properties.getHostname() + "/search", "application/geo+json",
					"STAC search endpoint"));
	
			for (Entry<String, StacCollectionProperties> entry : properties.getCollections().entrySet()) {
				if (entry.getValue().getCatalog().equals(catalogName)) {
					catalog.getLinks()
							.add(new StacLink("child",
									properties.getHostname() + "/" + catalogName + "/collections/" + entry.getKey(),
									"application/json", entry.getValue().getTitle()));
					catalog.getCollections().add(createCollection(catalogName, entry.getKey(), entry.getValue()));
				}
			}
	
			return catalog;
		}
		
		return null;
	}

	/**
	 * Create static page for one specific collection
	 */
	public StacCollection getCollectionPage(final String catalogName, final String collectionName) {
		if (properties.getCollections().containsKey(collectionName)) {
			return createCollection(catalogName, collectionName, properties.getCollections().get(collectionName));
		}

		return null;
	}

	/**
	 * Create a StacCollection object based on a name and defined properties
	 * 
	 * @param catalogName    name of the subcatalog
	 * @param name           name of the collection
	 * @param collProperties object containing further information for the
	 *                       collection
	 * @return new StacCollection object
	 */
	private StacCollection createCollection(String catalogName, String name, StacCollectionProperties collProperties) {
		StacCollection collection = new StacCollection();

		collection.setId(name);
		collection.setTitle(collProperties.getTitle());
		collection.setDescription(collProperties.getDescription());
		collection.setLicense(collProperties.getLicense());

		collection.getLinks()
				.add(new StacLink("self", properties.getHostname() + "/" + catalogName + "/collections/" + name,
						"application/json", collProperties.getTitle()));
		collection.getLinks().add(
				new StacLink("root", properties.getHostname(), "application/json", properties.getRootCatalogTitle()));
		collection.getLinks()
				.add(new StacLink("child",
						properties.getHostname() + "/" + catalogName + "/collections/" + name + "/items",
						"application/geo+json", "Item list of " + name));

		return collection;
	}

	/**
	 * Extract list of all defined sub catalogs of the configuration
	 */
	private Set<String> extractSubCatalogs() {
		return properties.getCollections().values().stream().map(collection -> collection.getCatalog())
				.collect(Collectors.toSet());
	}
}
