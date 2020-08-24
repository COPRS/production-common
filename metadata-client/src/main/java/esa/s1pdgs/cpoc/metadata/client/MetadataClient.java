package esa.s1pdgs.cpoc.metadata.client;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.Retries;
import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0AcnMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0SliceMetadata;
import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;
import esa.s1pdgs.cpoc.metadata.model.S3Metadata;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;

public class MetadataClient {

	private static final Logger LOGGER = LogManager.getLogger(MetadataClient.class);

	private final RestTemplate restTemplate;
	private final String metadataBaseUri;
	private final int maxRetries;
	private final int retryInMillis;

	public MetadataClient(final RestTemplate restTemplate, final String metadataHostname, final int maxRetries,
			final int retryInMillis) {
		this.restTemplate = restTemplate;
		this.metadataBaseUri = "http://" + metadataHostname + "/";
		this.maxRetries = maxRetries;
		this.retryInMillis = retryInMillis;
	}

	/**
	 * If productType = blank, the metadata catalog will extract the product type
	 * from the product name
	 * 
	 * @param productType
	 * @param productName
	 * @return
	 * @throws MetadataQueryException
	 */
	public List<EdrsSessionMetadata> getEdrsSessionFor(final String sessionId) throws MetadataQueryException {

		final String uri = this.metadataBaseUri + MetadataCatalogRestPath.EDRS_SESSION.path() + "/sessionId/"
				+ sessionId;

		final ResponseEntity<List<EdrsSessionMetadata>> response = query(
				UriComponentsBuilder.fromUriString(uri).build().toUri(),
				new ParameterizedTypeReference<List<EdrsSessionMetadata>>() {
				});

		if (response == null || response.getBody() == null) {
			throw new MetadataQueryException("Edrs session not found for sessionId " + sessionId);
		} else {
			LOGGER.debug("Returning Edrs session: {}", response.getBody());
			return response.getBody();
		}
	}

	/**
	 * 
	 * @param productName
	 * @return
	 * @throws MetadataQueryException
	 */
	public L0SliceMetadata getL0Slice(final String productName) throws MetadataQueryException {

		final String uri = this.metadataBaseUri + MetadataCatalogRestPath.L0_SLICE.path() + "/" + productName;

		final ResponseEntity<L0SliceMetadata> response = query(UriComponentsBuilder.fromUriString(uri).build().toUri(),
				new ParameterizedTypeReference<L0SliceMetadata>() {
				});

		if (response == null || response.getBody() == null) {
			throw new MetadataQueryException("L0 slice not found for product name {}" + productName);
		} else {
			LOGGER.debug("Returning L0 slice: {}", response.getBody());
			return response.getBody();
		}

	}

	/**
	 * @param family
	 * @param productName
	 * @return
	 * @throws MetadataQueryException
	 */
	public List<LevelSegmentMetadata> getLevelSegments(final String dataTakeId) throws MetadataQueryException {
		final String uri = this.metadataBaseUri + MetadataCatalogRestPath.LEVEL_SEGMENT.path() + "/" + dataTakeId;

		final ResponseEntity<List<LevelSegmentMetadata>> response = query(
				UriComponentsBuilder.fromUriString(uri).build().toUri(),
				new ParameterizedTypeReference<List<LevelSegmentMetadata>>() {
				});

		if (response == null || response.getBody() == null) {
			throw new MetadataQueryException("Level segment not found for dataTakeId {}" + dataTakeId);
		} else {
			LOGGER.debug("Returning level segment: {}", response.getBody());
			return response.getBody();
		}
	}

	/**
	 * @param productName
	 * @param processMode
	 * @return
	 * @throws MetadataQueryException
	 */
	public L0AcnMetadata getFirstACN(final String productName, final String processMode) throws MetadataQueryException {

		final String uri = this.metadataBaseUri + MetadataCatalogRestPath.L0_SLICE.path() + "/" + productName + "/acns";

		final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri).queryParam("mode", "ONE")
				.queryParam("processMode", processMode);

		final ResponseEntity<L0AcnMetadata[]> response = query(builder.build().toUri(),
				new ParameterizedTypeReference<L0AcnMetadata[]>() {
				});

		if (response == null) {
			throw new MetadataQueryException(String
					.format("First ACN not found for product name %s and process mode %s", productName, processMode));
		} else {
			final L0AcnMetadata[] objects = response.getBody();
			if (objects != null && objects.length > 0) {
				LOGGER.debug("Returning first ACN: {}", objects[0]);
				return objects[0];
			} else {
				throw new MetadataQueryException(String.format(
						"First ACN not found for product name %s and process mode %s", productName, processMode));
			}
		}
	}

	/**
	 * Interface to access products needed for the marginTT workflow extension
	 * (sentinel 3 mission)
	 * 
	 * @return list of matching products
	 */
	public List<S3Metadata> getProductsForMarginWFX(final String productType, final ProductFamily productFamily,
			final String satelliteId, final String t0, final String t1, final double dt0, final double dt1,
			final String timeliness) throws MetadataQueryException {
		final String uri = this.metadataBaseUri + MetadataCatalogRestPath.S3_METADATA.path() + "/" + productType
				+ "/marginTT";
		final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
				.queryParam("productFamily", productFamily.toString()).queryParam("satellite", satelliteId)
				.queryParam("t0", t0).queryParam("t1", t1).queryParam("dt0", dt0).queryParam("dt1", dt1)
				.queryParam("timeliness", timeliness);

		final ResponseEntity<List<S3Metadata>> response = query(builder.build().toUri(),
				new ParameterizedTypeReference<List<S3Metadata>>() {
				});

		if (response == null || response.getBody() == null) {
			LOGGER.debug("MarginTT metadata query for family '{}' and product type '{}' returned no results",
					productFamily, productType);
			return new ArrayList<>();
		} else {
			LOGGER.info("MarginTT metadata query for family '{}' and product type '{}' returned {} results",
					productFamily, productType, response.getBody().size());
			return response.getBody();
		}
	}

	/**
	 * @param query
	 * @param t0
	 * @param t1
	 * @param satelliteId
	 * @param instrumentConfigurationId
	 * @param processMode
	 * @return
	 * @throws MetadataQueryException
	 */
	public List<SearchMetadata> search(final SearchMetadataQuery query, final String t0, final String t1,
			final String satelliteId, final int instrumentConfigurationId, final String processMode,
			final String polarisation) throws MetadataQueryException {

		final String uri = this.metadataBaseUri + MetadataCatalogRestPath.METADATA.path() + "/"
				+ query.getProductFamily().toString() + "/search";
		final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
				.queryParam("productType", query.getProductType()).queryParam("mode", query.getRetrievalMode())
				.queryParam("t0", t0).queryParam("t1", t1).queryParam("dt0", query.getDeltaTime0())
				.queryParam("dt1", query.getDeltaTime1()).queryParam("satellite", satelliteId);

		if (processMode != null) {
			builder.queryParam("processMode", processMode);
		}
		if (instrumentConfigurationId != -1) {
			builder.queryParam("insConfId", instrumentConfigurationId);
		}
		if (polarisation != null) {
			builder.queryParam("polarisation", polarisation);
		}
		final ResponseEntity<List<SearchMetadata>> response = query(builder.build().toUri(),
				new ParameterizedTypeReference<List<SearchMetadata>>() {
				});

		if (response == null || response.getBody() == null) {
			LOGGER.debug("Metadata query for family '{}' and product type '{}' returned no results",
					query.getProductFamily(), query.getProductType());
			return new ArrayList<>();
		} else {
			LOGGER.info("Metadata query for family '{}' and product type '{}' returned {} results",
					query.getProductFamily(), query.getProductType(), numResults(response));
			return response.getBody();
		}
	}

	/**
	 * @param family
	 * @param intervalStart
	 * @param intervalStop
	 * @return
	 * @throws MetadataQueryException
	 */
	public List<SearchMetadata> query(final ProductFamily family, final LocalDateTime intervalStart,
			final LocalDateTime intervalStop) throws MetadataQueryException {

		final String uri = this.metadataBaseUri + MetadataCatalogRestPath.METADATA.path() + "/" + family.toString()
				+ "/searchInterval";

		final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
				// FIXME: Maybe we not need a productType anyways!
				// .queryParam("productType", productType)
				.queryParam("intervalStart", intervalStart.format(DateUtils.METADATA_DATE_FORMATTER))
				.queryParam("intervalStop", intervalStop.format(DateUtils.METADATA_DATE_FORMATTER));

		final ResponseEntity<List<SearchMetadata>> response = query(builder.build().toUri(),
				new ParameterizedTypeReference<List<SearchMetadata>>() {
				});

		if (response == null || response.getBody() == null) {
			LOGGER.debug("Metadata query for family '{}' returned no results", family);
			return new ArrayList<>();
		} else {
			LOGGER.info("Metadata query for family '{}' returned {} results", family, numResults(response));
			return response.getBody();
		}
	}

	/**
	 * Searches for the product with given productName and in the index =
	 * productFamily. The returned metadata contains only validity start and stop
	 * time.
	 * 
	 * @param family
	 * @param productName
	 * @return
	 * @throws MetadataQueryException
	 */
	public SearchMetadata queryByFamilyAndProductName(final String family, final String productName)
			throws MetadataQueryException {

		final String uri = this.metadataBaseUri + MetadataCatalogRestPath.METADATA.path() + "/" + family
				+ "/searchProductName";

		final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri).queryParam("productName",
				productName);

		final ResponseEntity<SearchMetadata> response = query(builder.build().toUri(),
				new ParameterizedTypeReference<SearchMetadata>() {
				});

		if (response == null || response.getBody() == null) {
			LOGGER.error("Metadata query for family '{}' and product name {} returned no result", family, productName);
			throw new MetadataQueryException(String.format(
					"Metadata query for family '%s' and product name %s returned no result", family, productName));
		} else {
			LOGGER.info("Metadata query for family '{}' and product name {} returned 1 result", family, productName);
			return response.getBody();
		}
	}

	/**
	 * @param family
	 * @param productName
	 * @return
	 * @throws MetadataQueryException
	 */
	public int getSeaCoverage(final ProductFamily family, final String productName) throws MetadataQueryException {

		final String uri = this.metadataBaseUri + MetadataCatalogRestPath.L0_SLICE.path() + "/" + family + "/"
				+ productName + "/seaCoverage";

		final String commandDescription = String.format("call rest metadata for sea coverage check on %s", uri);

		final ResponseEntity<Integer> result = performWithRetries(commandDescription, () -> {
			int notAvailableRetries = 10;
			LOGGER.debug(commandDescription);
			ResponseEntity<Integer> response = this.restTemplate.exchange(uri, HttpMethod.GET, null, Integer.class);
			while (response.getStatusCode() == HttpStatus.NO_CONTENT) {
				LOGGER.debug("Product not available yet. Waiting...");
				try {
					Thread.sleep(this.retryInMillis);
				} catch (final InterruptedException e) {
					throw new MetadataQueryException(e.getMessage(), e);
				}
				notAvailableRetries--;
				LOGGER.debug("Retrying call rest metadata for sea coverage check on  {}", uri);
				response = this.restTemplate.exchange(uri, HttpMethod.GET, null, Integer.class);
				if (notAvailableRetries <= 0) {
					LOGGER.trace("Max number of retries reached for {}", productName);
					break;
				}
			}
			handleReturnValueErrors(uri, response);
			return response;
		});

		if (result == null) {
			throw new MetadataQueryException("Query for seacoverage returns no result for " + productName);
		}
		final Integer coverage = result.getBody();
		if (coverage == null) {
			throw new MetadataQueryException("Query for seacoverage returns no result body for " + productName);
		}
		LOGGER.debug("Got coverage {}", coverage);
		return coverage;

	}

	private <T> ResponseEntity<T> query(final URI uri, final ParameterizedTypeReference<T> responseType)
			throws MetadataQueryException {
		final String commandDescription = String.format("call rest metadata on %s", uri);

		return performWithRetries(commandDescription, () -> {
			LOGGER.debug(commandDescription);
			final ResponseEntity<T> response = restTemplate.exchange(uri, HttpMethod.GET, null, responseType);
			handleReturnValueErrors(uri.toString(), response);
			LOGGER.debug("Rest api metadata call returned result. URI: {}, RESPONSE: {},", uri, response.getBody());
			return response;
		});
	}

	private <T> void handleReturnValueErrors(final String uri, final ResponseEntity<T> response)
			throws MetadataQueryException {
		if (response == null) {
			throw new MetadataQueryException(String.format("Rest metadata call %s returned null", uri));
		}
		if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
			return;
		}
		if (response.getStatusCode() != HttpStatus.OK) {
			throw new MetadataQueryException(
					String.format("Rest metadata call %s returned status code %s", uri, response.getStatusCode()));
		}
	}

	private <T> T performWithRetries(final String commandDescription, final Callable<T> command)
			throws MetadataQueryException {
		try {
			return Retries.performWithRetries(command, commandDescription, maxRetries, retryInMillis);
		} catch (final RuntimeException e) {
			// unwrap possible metadata exception
			if (e.getCause() instanceof MetadataQueryException) {
				throw (MetadataQueryException) e.getCause();
			} else if (e.getCause() instanceof RestClientException) {
				final RestClientException restClientException = (RestClientException) e.getCause();
				throw new MetadataQueryException(restClientException.getMessage(), restClientException);
			}
			// otherwise simply pass through exception
			throw e;
		} catch (final InterruptedException e) {
			throw new RuntimeException(String.format("Interrupted on command execution of '%s'", commandDescription));
		}
	}

	private int numResults(final ResponseEntity<List<SearchMetadata>> response) {
		if (response != null) {
			final List<SearchMetadata> res = response.getBody();
			if (res != null) {
				return res.size();
			}
		}
		return -1; // To indicate that null was returned and not an empty list
	}
}
