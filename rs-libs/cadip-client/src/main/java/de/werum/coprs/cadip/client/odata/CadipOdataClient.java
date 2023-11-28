package de.werum.coprs.cadip.client.odata;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntityRequest;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetIteratorRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientEntitySetIterator;
import org.apache.olingo.client.api.uri.FilterArgFactory;
import org.apache.olingo.client.api.uri.FilterFactory;
import org.apache.olingo.client.api.uri.URIBuilder;
import org.apache.olingo.client.api.uri.URIFilter;

import de.werum.coprs.cadip.client.CadipClient;
import de.werum.coprs.cadip.client.config.CadipClientConfigurationProperties.CadipHostConfiguration;
import de.werum.coprs.cadip.client.model.CadipFile;
import de.werum.coprs.cadip.client.model.CadipQualityInfo;
import de.werum.coprs.cadip.client.model.CadipSession;
import de.werum.coprs.cadip.client.odata.mapping.ResponseMapperUtil;
import de.werum.coprs.cadip.client.odata.model.CadipOdataFile;
import de.werum.coprs.cadip.client.odata.model.CadipOdataSession;
import esa.s1pdgs.cpoc.common.utils.StringUtil;

/**
 * OData implementation of the CADIP client
 */
public class CadipOdataClient implements CadipClient {
	static final Logger LOG = LogManager.getLogger(CadipOdataClient.class);

	private final ODataClient odataClient;
	private final CloseableHttpClient downloadClient;
	private final HttpClientContext context;
	private final CadipHostConfiguration hostConfig;
	private final URI rootServiceUrl;

	// --------------------------------------------------------------------------

	CadipOdataClient(final ODataClient odataClient, final CadipHostConfiguration hostConfig,
			final CloseableHttpClient downloadClient, final HttpClientContext context) {
		this.odataClient = odataClient;
		this.hostConfig = hostConfig;

		final String baseUri = this.hostConfig.getServiceRootUri();

		this.rootServiceUrl = toNormalizedUri(baseUri);

		this.downloadClient = downloadClient;
		this.context = context;
	}

	static final URI toNormalizedUri(final String configuredUri) {
		if (!configuredUri.endsWith("/")) {
			return URI.create(configuredUri + "/");
		}
		return URI.create(configuredUri);
	}

	// --------------------------------------------------------------------------

	@Override
	public void close() throws IOException {
		// try to close client download client
		if (this.downloadClient instanceof Closeable) {
			try {
				((Closeable) this.downloadClient).close();
			} catch (final IOException e) {
				LOG.warn(String.format("error closing cadip download client %s: %s", this.downloadClient,
						StringUtil.stackTraceToString(e)));
			}
		}

		// try to close client odata client
		if (this.odataClient instanceof Closeable) { // currently, ODataClient is not Closeable though
			try {
				((Closeable) this.odataClient).close();
			} catch (final IOException e) {
				LOG.warn(String.format("error closing cadip odata client %s: %s", this.odataClient,
						StringUtil.stackTraceToString(e)));
			}
		}
	}

	@Override
	public List<CadipSession> getSessions(final String satellite, final List<String> orbits,
			final LocalDateTime publishingDate) {
		// Prepare filter and URI
		final URIFilter uriFilter = buildSessionFilters(satellite, orbits, publishingDate);
		final URI queryUri = this.buildQueryUri(Collections.singletonList(uriFilter), CadipOdataSession.ENTITY_SET_NAME,
				CadipOdataSession.PUBLICATION_DATE_ATTRIBUTE, "asc");

		// Retrieve entities
		final ClientEntitySetIterator<ClientEntitySet, ClientEntity> response = this.readEntities(queryUri);
		final List<CadipSession> result = ResponseMapperUtil.mapResponseToListOfSessions(response);
		LOG.debug("getSessions returned " + result.size() + " elements");

		return result;
	}

	@Override
	public List<CadipSession> getSessionsBySessionIdAndRetransfer(String sessionId, boolean retransfer) {
		// Prepare filter and URI
		final FilterFactory filterFactory = this.odataClient.getFilterFactory();
		final URIFilter sessionIdFilter = filterFactory.eq(CadipOdataSession.SESSION_ID_ATTRIBUTE, sessionId);
		final URIFilter retransferFilter = filterFactory.eq(CadipOdataSession.RETRANSFER_ATTRIBUTE, retransfer);
		
		final URIFilter uriFilter = combineFilters(filterFactory, sessionIdFilter, retransferFilter);

		final URI queryUri = this.buildQueryUri(Collections.singletonList(uriFilter), CadipOdataSession.ENTITY_SET_NAME,
				CadipOdataSession.PUBLICATION_DATE_ATTRIBUTE, "asc");

		// Retrieve entities
		final ClientEntitySetIterator<ClientEntitySet, ClientEntity> response = this.readEntities(queryUri);
		final List<CadipSession> result = ResponseMapperUtil.mapResponseToListOfSessions(response);
		LOG.debug("getSessionsBySessionId returned " + result.size() + " elements");

		return result;
	}

	@Override
	public CadipSession getSessionById(String uuid) {
		// Prepare filter and URI
		final FilterFactory filterFactory = this.odataClient.getFilterFactory();
		final URIFilter uriFilter = filterFactory.eq(CadipOdataSession.ID_ATTRIBUTE, uuid);

		final URI queryUri = this.buildQueryUri(Collections.singletonList(uriFilter), CadipOdataSession.ENTITY_SET_NAME,
				CadipOdataSession.PUBLICATION_DATE_ATTRIBUTE, "asc");

		// Retrieve entities
		final ClientEntitySetIterator<ClientEntitySet, ClientEntity> response = this.readEntities(queryUri);
		final List<CadipSession> result = ResponseMapperUtil.mapResponseToListOfSessions(response);

		if (result.size() == 1) {
			LOG.debug("getSessionById returned an element");
			return result.get(0);
		}

		LOG.debug("getSessionById returned no element");
		return null;
	}

	@Override
	public List<CadipFile> getFiles(String sessionId, String name, boolean retransfer, LocalDateTime publishingDate) {
		// Prepare filter and URI
		final URIFilter uriFilter = buildFileFilters(sessionId, name, retransfer, publishingDate);
		final URI queryUri = this.buildQueryUri(Collections.singletonList(uriFilter), CadipOdataFile.ENTITY_SET_NAME,
				CadipOdataFile.PUBLICATION_DATE_ATTRIBUTE, "asc");

		// Retrieve entities
		final ClientEntitySetIterator<ClientEntitySet, ClientEntity> response = this.readEntities(queryUri);
		final List<CadipFile> result = ResponseMapperUtil.mapResponseToListOfFiles(response);
		LOG.debug("getFiles " + result.size() + " elements");

		return result;
	}

	@Override
	public List<CadipFile> getFilesBySessionUUID(String sessionUUID) {
		// Prepare URI
		final URIBuilder uriBuilder = this.odataClient.newURIBuilder(this.rootServiceUrl.toString())
				.appendEntitySetSegment(CadipOdataSession.ENTITY_SET_NAME)
				.appendKeySegment(UUID.fromString(sessionUUID)).expand("Files");

		URI queryUri = uriBuilder.build();

		// Retrieve entities
		final ClientEntity response = this.readEntity(queryUri);
		final List<CadipFile> result = ResponseMapperUtil.mapSessionResponseToListOfFiles(response);
		LOG.debug("getFilesBySessionUUID " + result.size() + " elements");

		return result;
	}

	@Override
	public CadipFile getFileById(String uuid) {
		// Prepare filter and URI
		final FilterFactory filterFactory = this.odataClient.getFilterFactory();
		final URIFilter uriFilter = filterFactory.eq(CadipOdataFile.ID_ATTRIBUTE, UUID.fromString(uuid));

		final URI queryUri = this.buildQueryUri(Collections.singletonList(uriFilter), CadipOdataFile.ENTITY_SET_NAME,
				CadipOdataFile.PUBLICATION_DATE_ATTRIBUTE, "asc");

		// Retrieve entities
		final ClientEntitySetIterator<ClientEntitySet, ClientEntity> response = this.readEntities(queryUri);
		final List<CadipFile> result = ResponseMapperUtil.mapResponseToListOfFiles(response);

		if (result.size() == 1) {
			LOG.debug("getFileById returned an element");
			return result.get(0);
		}

		LOG.debug("getFileById returned no element");
		return null;
	}

	@Override
	public List<CadipQualityInfo> getQualityInfo(UUID sessionId) {
		// TODO: Implement if needed.
		throw new NotImplementedException("getQualityInfo was not implemented for the CADIP client yet");
	}

	@Override
	public InputStream downloadFile(UUID fileId) {
		final URI productDownloadUrl = this.rootServiceUrl
				.resolve(CadipOdataFile.ENTITY_SET_NAME + "(" + fileId.toString() + ")/$value");

		LOG.debug("sending download request: " + productDownloadUrl);
		if (this.hostConfig.isUseHttpClientDownload()) {
			final HttpGet httpget = new HttpGet(productDownloadUrl.toString());

			// authentication
			final String authType = this.hostConfig.getAuthType();
			if ("oauth2".equalsIgnoreCase(authType)) {
				httpget.addHeader(CadipAuthenticationUtil.oauthHeaderFor(this.hostConfig));
			} else if ("basic".equalsIgnoreCase(authType)) {
				httpget.addHeader(CadipAuthenticationUtil.basicAuthHeaderFor(this.hostConfig));
			} else {
				LOG.info("download request authentication is disabled per authType "
						+ (StringUtil.isEmpty(this.hostConfig.getAuthType()) ? "<empty>"
								: "'" + this.hostConfig.getAuthType() + "'")
						+ " for " + this.rootServiceUrl);
			}

			try {
				final CloseableHttpResponse response = this.downloadClient.execute(httpget, this.context);
				final HttpEntity entity = response.getEntity();

				// check if something has been returned and no error occurred
				if ((entity == null) || (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)) {
					throw new RuntimeException(
							String.format("Error on download of %s: %s", fileId, response.getStatusLine()));
				}
				return new BufferedInputStream(entity.getContent()) {
					@Override
					public final void close() throws IOException {
						try {
							super.close();
						} finally {
							response.close();
						}
					}
				};
			} catch (final Exception e) {
				// TODO error handling
				throw new RuntimeException(e);
			}
		} else {
			final ODataRetrieveResponse<InputStream> response = this.odataClient.getRetrieveRequestFactory()
					.getMediaRequest(productDownloadUrl).execute();

			LOG.debug("download request (" + productDownloadUrl + ") response status: " + response.getStatusCode()
					+ " - " + response.getStatusMessage());

			return response.getBody();
		}
	}

	// --------------------------------------------------------------------------

	private URIFilter buildSessionFilters(final String satellite, final List<String> orbits,
			final LocalDateTime publishingDate) {
		final FilterFactory filterFactory = this.odataClient.getFilterFactory();
		URIFilter satelliteFilter = null;
		URIFilter orbitFilter = null;
		URIFilter dateFilter = null;

		if (satellite != null && !satellite.isEmpty()) {
			satelliteFilter = filterFactory.eq(CadipOdataSession.SATELLITE_ATTRIBUTE, satellite);
		}

		// TODO: Change this to in operator, when olingo finally implements it on client
		// side
		if (orbits != null && !orbits.isEmpty()) {
			for (final String orbit : orbits) {
				if (orbitFilter != null) {
					orbitFilter = filterFactory.or(orbitFilter,
							filterFactory.eq(CadipOdataSession.DOWNLINK_ORBIT_ATTRIBUTE, orbit));
				} else {
					orbitFilter = filterFactory.eq(CadipOdataSession.DOWNLINK_ORBIT_ATTRIBUTE, orbit);
				}
			}
		}

		if (publishingDate != null) {
			dateFilter = filterFactory.ge(CadipOdataSession.PUBLICATION_DATE_ATTRIBUTE,
					publishingDate.toInstant(ZoneOffset.UTC));
		}

		// Build final filter
		URIFilter finalFilter = null;
		finalFilter = combineFilters(filterFactory, finalFilter, satelliteFilter);
		finalFilter = combineFilters(filterFactory, finalFilter, orbitFilter);
		finalFilter = combineFilters(filterFactory, finalFilter, dateFilter);
		return finalFilter;
	}

	private URIFilter buildFileFilters(final String sessionId, final String name, final boolean retransfer,
			final LocalDateTime publishingDate) {
		final FilterFactory filterFactory = this.odataClient.getFilterFactory();
		final FilterArgFactory filterArgFactory = filterFactory.getArgFactory();
		URIFilter sessionFilter = null;
		URIFilter nameFilter = null;
		URIFilter dateFilter = null;

		if (sessionId != null && !sessionId.isEmpty()) {
			sessionFilter = filterFactory.eq(CadipOdataFile.SESSION_ID_ATTRIBUTE, sessionId);
		}

		if (name != null && !name.isEmpty()) {
			nameFilter = filterFactory.match(filterArgFactory.contains(
					filterArgFactory.property(CadipOdataFile.NAME_ATTRIBUTE), filterArgFactory.literal(name)));
		}
		
		URIFilter retransferFilter = filterFactory.eq(CadipOdataFile.RETRANSFER_ATTRIBUTE, retransfer);

		if (publishingDate != null) {
			dateFilter = filterFactory.ge(CadipOdataFile.PUBLICATION_DATE_ATTRIBUTE,
					publishingDate.toInstant(ZoneOffset.UTC));
		}

		// Build final filter
		URIFilter finalFilter = null;
		finalFilter = combineFilters(filterFactory, finalFilter, sessionFilter);
		finalFilter = combineFilters(filterFactory, finalFilter, nameFilter);
		finalFilter = combineFilters(filterFactory, finalFilter, retransferFilter);
		finalFilter = combineFilters(filterFactory, finalFilter, dateFilter);

		return finalFilter;
	}

	private URIFilter combineFilters(final FilterFactory factory, final URIFilter filter1, final URIFilter filter2) {
		if (filter2 == null) {
			return filter1;
		}

		return filter1 == null ? filter2 : factory.and(filter1, filter2);
	}

	private URI buildQueryUri(final List<URIFilter> filters, final String entitySetName, final String sortAttribute,
			final String sortDirection) {
		final URIBuilder uriBuilder = this.odataClient.newURIBuilder(this.rootServiceUrl.toString())
				.appendEntitySetSegment(entitySetName);

		if (null != filters && !filters.isEmpty()) {
			filters.forEach(f -> uriBuilder.filter(f));
		}

		uriBuilder.orderBy(sortAttribute + " " + sortDirection);

		return uriBuilder.build();
	}

	private ClientEntitySetIterator<ClientEntitySet, ClientEntity> readEntities(final URI absoluteUri) {
		final ODataEntitySetIteratorRequest<ClientEntitySet, ClientEntity> request = this.odataClient
				.getRetrieveRequestFactory().getEntitySetIteratorRequest(absoluteUri);
		request.setAccept("application/json");
		LOG.debug("sending request to CADIP-Server: " + absoluteUri);

		final ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> response = request
				.execute();
		LOG.debug("CADIP response status: " + response.getStatusCode() + " - " + response.getStatusMessage());

		return response.getBody();
	}

	private ClientEntity readEntity(final URI absoluteUri) {
		final ODataEntityRequest<ClientEntity> request = this.odataClient.getRetrieveRequestFactory()
				.getEntityRequest(absoluteUri);
		request.setAccept("application/json");
		LOG.debug("sending request to CADIP-Server: " + absoluteUri);

		final ODataRetrieveResponse<ClientEntity> response = request.execute();
		LOG.debug("CADIP response status: " + response.getStatusCode() + " - " + response.getStatusMessage());
		return response.getBody();
	}

}
