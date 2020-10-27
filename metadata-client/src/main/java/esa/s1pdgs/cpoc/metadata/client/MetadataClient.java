package esa.s1pdgs.cpoc.metadata.client;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.Retries;
import esa.s1pdgs.cpoc.metadata.model.AuxMetadata;
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
	 * Interface to access products needed for the
	 * {@link esa.s1pdgs.cpoc.ipf.preparation.worker.type.s3.MultipleProductCoverSearch}
	 * workflow extension (sentinel 3 mission)
	 * 
	 * @return list of matching products
	 */
	public List<S3Metadata> getProductsInRange(final String productType, final ProductFamily productFamily,
			final String satelliteId, final String t0, final String t1, final double dt0, final double dt1,
			final String timeliness) throws MetadataQueryException {
		final String uri = this.metadataBaseUri + MetadataCatalogRestPath.S3_METADATA.path() + "/" + productType
				+ "/range";

		final String rangeStart = convertDateForSearch(t0, -dt0,
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"));
		final String rangeStop = convertDateForSearch(t1, dt1,
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"));

		final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
				.queryParam("productFamily", productFamily.toString()).queryParam("satellite", satelliteId)
				.queryParam("start", rangeStart).queryParam("stop", rangeStop).queryParam("timeliness", timeliness);

		final ResponseEntity<List<S3Metadata>> response = query(builder.build().toUri(),
				new ParameterizedTypeReference<List<S3Metadata>>() {
				});
		return extractResult(productType, productFamily, response);
	}

	/**
	 * Extracts the L1Triggering information for the given productName
	 * 
	 * @param productFamily productFamily of the product
	 * @param productName   productName which L1Triggering should be extracted from
	 * @return L1Triggering information
	 * @throws MetadataQueryException on error on query execution
	 */
	public String getL1TriggeringForProductName(final ProductFamily productFamily, final String productName)
			throws MetadataQueryException {
		final String uri = this.metadataBaseUri + MetadataCatalogRestPath.S3_METADATA.path() + "/l1triggering";

		final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
				.queryParam("productFamily", productFamily.toString()).queryParam("productName", productName);

		final ResponseEntity<String> response = query(builder.build().toUri(),
				new ParameterizedTypeReference<String>() {
				});

		if (response == null || response.getBody() == null) {
			LOGGER.debug("L1Triggering metadata query for family '{}' and product name '{}' returned no results",
					productFamily, productName);
			return null;
		} else {
			LOGGER.info("L1Triggering metadata query for family '{}' and product name '{}' returned {}",
					productFamily.toString(), productName, response.getBody());
			return response.getBody();
		}
	}

	/**
	 * Extracts the S3Metadata information for the given productName
	 * 
	 * @param productFamily productFamily of the product
	 * @param productName   productName which metadata should be extracted
	 * @return S3Metadata
	 * @throws MetadataQueryException on error on query execution
	 */
	public S3Metadata getS3MetadataForProduct(final ProductFamily productFamily, final String productName)
			throws MetadataQueryException {
		final String uri = this.metadataBaseUri + MetadataCatalogRestPath.S3_METADATA.path() + "/"
				+ productFamily.toString();

		final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri).queryParam("productName",
				productName);

		final ResponseEntity<S3Metadata> response = query(builder.build().toUri(),
				new ParameterizedTypeReference<S3Metadata>() {
				});

		if (response == null || response.getBody() == null) {
			LOGGER.debug("S3Metadata query for family '{}' and product name '{}' returned no results", productFamily,
					productName);
			return null;
		} else {
			LOGGER.info("S3Metadata query for family '{}' and product name '{}' returned {}", productFamily.toString(),
					productName, response.getBody());
			return response.getBody();
		}
	}

	/**
	 * Extract the first product (based on insertionTime) of an orbit
	 * 
	 * @param productFamily productFamily of the product
	 * @param productType   productType of the product
	 * @param satelliteId   satelliteId of the product
	 * @param orbit         orbit number
	 * @return first product of the orbit or not product if no products exist for
	 *         orbit number
	 * @throws MetadataQueryException
	 */
	public S3Metadata getFirstProductForOrbit(final ProductFamily productFamily, final String productType,
			final String satelliteId, final long orbit) throws MetadataQueryException {
		final String uri = this.metadataBaseUri + MetadataCatalogRestPath.S3_METADATA.path() + "/" + productType
				+ "/orbit";

		final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
				.queryParam("productFamily", productFamily.toString()).queryParam("satellite", satelliteId)
				.queryParam("orbitNumber", orbit);

		final ResponseEntity<S3Metadata> response = query(builder.build().toUri(),
				new ParameterizedTypeReference<S3Metadata>() {
				});

		if (response == null || response.getBody() == null) {
			LOGGER.debug("First Product of Orbit query for product type '{}' and orbit '{}' returned no results",
					productType, orbit);
			return null;
		} else {
			LOGGER.info("First Product of Orbit query for product type '{}' and orbit '{}' returned {}", productType,
					orbit, response.getBody());
			return response.getBody();
		}
	}

	/**
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
	 * Queries the products inside a given time interval for the given producttype.
	 * The time interval is applied on the insertionTime
	 */
	public List<SearchMetadata> searchInterval(final ProductFamily productFamily, final String productType,
			final LocalDateTime intervalStart, final LocalDateTime intervalStop, final String satelliteId)
			throws MetadataQueryException {
		final String uri = this.metadataBaseUri + MetadataCatalogRestPath.METADATA.path() + "/"
				+ productFamily.toString() + "/searchTypeInterval";

		final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
				.queryParam("productType", productType)
				.queryParam("intervalStart", intervalStart.format(DateUtils.METADATA_DATE_FORMATTER))
				.queryParam("intervalStop", intervalStop.format(DateUtils.METADATA_DATE_FORMATTER))
				.queryParam("satelliteId", satelliteId);

		final ResponseEntity<List<SearchMetadata>> response = query(builder.build().toUri(),
				new ParameterizedTypeReference<List<SearchMetadata>>() {
				});

		if (response == null || response.getBody() == null) {
			LOGGER.debug("Metadata query for family '{}' returned no results", productFamily);
			return new ArrayList<>();
		} else {
			LOGGER.info("Metadata query for family '{}' returned {} results", productFamily, numResults(response));
			return response.getBody();
		}
	}

	/**
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

	public AuxMetadata queryAuxiliary(final String productType, final String productName)
			throws MetadataQueryException {

		final String uri = this.metadataBaseUri + MetadataCatalogRestPath.METADATA.path() + "/" + productType
				+ "/searchAuxiliary";

		final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri).queryParam("productName",
				productName);

		final ResponseEntity<AuxMetadata> response = query(builder.build().toUri(),
				new ParameterizedTypeReference<AuxMetadata>() {
				});

		if (response == null || response.getBody() == null) {
			LOGGER.error("Metadata query for type '{}' and product name {} returned no result", productType,
					productName);
			throw new MetadataQueryException(String.format(
					"Metadata query for type '%s' and product name %s returned no result", productType, productName));
		} else {
			LOGGER.info("Metadata query for type '{}' and product name {} returned 1 result", productType, productName);
			return response.getBody();
		}
	}

	/**
	 * Searches for the product with given productName and in the index =
	 * productFamily. The returned metadata contains only validity start and stop
	 * time.
	 * 
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

	/**
	 * Refresh the index determined by product family and type, to ensure new
	 * documents are searchable
	 * 
	 * @param productFamily product family to determine index
	 * @param productType   product type to determine index
	 */
	public void refreshIndex(final ProductFamily productFamily, final String productType)
			throws MetadataQueryException {
		LOGGER.debug("Refresh index for product family {} and product type {}", productFamily.toString(), productType);

		final String uri = this.metadataBaseUri + MetadataCatalogRestPath.S3_METADATA.path() + "/refreshIndex/"
				+ productFamily;

		final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri).queryParam("productType",
				productType);

		final ResponseEntity<String> response = query(builder.build().toUri(),
				new ParameterizedTypeReference<String>() {
				});

		if (!response.getStatusCode().is2xxSuccessful()) {
			throw new MetadataQueryException("Refresh of index for product family " + productFamily
					+ " and product type " + productType + " was not successful");
		}
	}

	/**
	 * Execute a command. If the result is null, refresh the index and try again.
	 * 
	 * This method should only be used, when one expects to receive always receive a
	 * result (ex. retrieving metadata for the product of the catalog event),
	 * otherwise this could impact performance.
	 * 
	 * @param command       command which should be executed
	 * @param productType   product type, used to determine the index
	 * @param productFamily product family, used to determine the index
	 * 
	 * @return result of the command
	 * @throws MetadataQueryException on errors while fetching metadata
	 */
	public <T> T performWithReindexOnNull(final Callable<T> command, final String productType,
			final ProductFamily productFamily) throws MetadataQueryException {
		try {
			T result = command.call();
			if (result == null) {
				LOGGER.info("Received result \"null\" but expected a result. Refresh index and try again.");
				this.refreshIndex(productFamily, productType);
				return command.call();
			}

			return result;
		} catch (MetadataQueryException e) {
			// Just pipe the exception through
			throw e;
		} catch (Exception e) {
			throw new MetadataQueryException(
					"Exception occured while executing metadata query with reindex on null result", e);
		}
	}

	private final List<S3Metadata> extractResult(final String productType, final ProductFamily productFamily,
			final ResponseEntity<List<S3Metadata>> response) {
		if (response == null) {
			LOGGER.debug("Metadata query for family '{}' and product type '{}' returned null", productFamily,
					productType);
			return Collections.emptyList();
		}
		final List<S3Metadata> queryResults = response.getBody();

		if (CollectionUtils.isEmpty(queryResults)) {
			LOGGER.debug("Metadata query for family '{}' and product type '{}' returned no results", productFamily,
					productType);
			return Collections.emptyList();
		}
		LOGGER.info("Metadata query for family '{}' and product type '{}' returned {} results", productFamily,
				productType, queryResults.size());
		return queryResults;
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

	/**
	 * Converts the given dateStr and delta a dateString of the given outputFormat
	 */
	private String convertDateForSearch(final String dateStr, final double delta,
			final DateTimeFormatter outFormatter) {
		final LocalDateTime time = DateUtils.parse(dateStr);
		final LocalDateTime timePlus = time.plusSeconds(Math.round(delta));
		return timePlus.format(outFormatter);
	}
}
