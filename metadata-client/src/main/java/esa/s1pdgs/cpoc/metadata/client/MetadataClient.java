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
	public EdrsSessionMetadata getEdrsSession(final String productType, final String productName)
			throws MetadataQueryException {

		String uri = this.metadataBaseUri + MetadataCatalogRestPath.EDRS_SESSION.path() + "/" + productType + "/"
				+ productName;

		ResponseEntity<EdrsSessionMetadata> response = query(UriComponentsBuilder.fromUriString(uri).build().toUri(),
				new ParameterizedTypeReference<EdrsSessionMetadata>() {
				});

		if (response == null) {
			LOGGER.debug("Edrs session not found for product type {} and product name {}", productType, productName);
			return null;
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

		String uri = this.metadataBaseUri + MetadataCatalogRestPath.L0_SLICE.path() + "/" + productName;

		ResponseEntity<L0SliceMetadata> response = query(UriComponentsBuilder.fromUriString(uri).build().toUri(),
				new ParameterizedTypeReference<L0SliceMetadata>() {
				});

		if (response == null) {
			LOGGER.debug("L0 slice not found for product name {}", productName);
			return null;
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
	public LevelSegmentMetadata getLevelSegment(final ProductFamily family, final String productName)
			throws MetadataQueryException {
		String uri = this.metadataBaseUri + MetadataCatalogRestPath.LEVEL_SEGMENT.path() + "/" + family + "/"
				+ productName;

		ResponseEntity<LevelSegmentMetadata> response = query(UriComponentsBuilder.fromUriString(uri).build().toUri(),
				new ParameterizedTypeReference<LevelSegmentMetadata>() {
				});

		if (response == null) {
			LOGGER.debug("Level segment not found for family {} and product name {}", family, productName);
			return null;
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

		String uri = this.metadataBaseUri + MetadataCatalogRestPath.L0_SLICE.path() + "/" + productName + "/acns";

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri).queryParam("mode", "ONE")
				.queryParam("processMode", processMode);

		ResponseEntity<L0AcnMetadata[]> response = query(builder.build().toUri(),
				new ParameterizedTypeReference<L0AcnMetadata[]>() {
				});

		if (response == null) {
			LOGGER.debug("First ACN not found for product name {} and process mode {}", productName, processMode);
			return null;
		} else {
			L0AcnMetadata[] objects = response.getBody();
			if (objects != null && objects.length > 0) {
				LOGGER.debug("Returning first ACN: {}", objects[0]);
				return objects[0];
			} else {
				LOGGER.debug("First ACN not found for product name {} and process mode {}", productName, processMode);
				return null;
			}
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
			final String satelliteId, final int instrumentConfigurationId, final String processMode, String polarisation)
			throws MetadataQueryException {

		String uri = this.metadataBaseUri + MetadataCatalogRestPath.METADATA.path() + "/"
				+ query.getProductFamily().toString() + "/search";
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
				.queryParam("productType", query.getProductType())
				.queryParam("mode", query.getRetrievalMode())
				.queryParam("t0", t0)
				.queryParam("t1", t1)
				.queryParam("dt0", query.getDeltaTime0())
				.queryParam("dt1", query.getDeltaTime1())
				.queryParam("satellite", satelliteId);

		
		if (processMode != null) {
			builder.queryParam("processMode", processMode);
		}
		if (instrumentConfigurationId != -1) {
			builder.queryParam("insConfId", instrumentConfigurationId);
		}
		if (polarisation != null) {
			builder.queryParam("polarisation", polarisation);
		}
		ResponseEntity<List<SearchMetadata>> response = query(builder.build().toUri(),
				new ParameterizedTypeReference<List<SearchMetadata>>() {
				});

		if (response == null) {
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
	public List<SearchMetadata> query(ProductFamily family, LocalDateTime intervalStart, LocalDateTime intervalStop)
			throws MetadataQueryException {

		String uri = this.metadataBaseUri + MetadataCatalogRestPath.METADATA.path() + "/" + family.toString()
				+ "/searchInterval";

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
				// FIXME: Maybe we not need a productType anyways!
				// .queryParam("productType", productType)
				.queryParam("intervalStart", intervalStart.format(DateUtils.METADATA_DATE_FORMATTER))
				.queryParam("intervalStop", intervalStop.format(DateUtils.METADATA_DATE_FORMATTER));

		ResponseEntity<List<SearchMetadata>> response = query(builder.build().toUri(),
				new ParameterizedTypeReference<List<SearchMetadata>>() {
				});

		if (response == null) {
			LOGGER.debug("Metadata query for family '{}' returned no results", family);
			return new ArrayList<>();
		} else {
			LOGGER.info("Metadata query for family '{}' returned {} results", family, numResults(response));
			return response.getBody();
		}
	}

	/**
	 * @param family
	 * @param productName
	 * @return
	 * @throws MetadataQueryException
	 */
	public int getSeaCoverage(ProductFamily family, String productName) throws MetadataQueryException {

		final String uri = this.metadataBaseUri + MetadataCatalogRestPath.L0_SLICE.path() + "/" 
				+ family + "/" + productName + "/seaCoverage";
		
		final String commandDescription = String.format("call rest metadata for sea coverage check on %s", uri);
		
		return performWithRetries(
				commandDescription,
				() -> {
					int notAvailableRetries = 10;					
					LOGGER.debug(commandDescription);
					ResponseEntity<Integer> response = this.restTemplate.exchange(uri, HttpMethod.GET, null, Integer.class);
					while (response == null || response.getStatusCode() == HttpStatus.NOT_FOUND) {
						LOGGER.debug("Product not available yet. Waiting...");
						try {
							Thread.sleep(this.retryInMillis);
						} catch (InterruptedException e) {
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
					final Integer res = response.getBody();
					LOGGER.debug("Got coverage {}", res);
					return res;					
				}
		);
	}

	private <T> ResponseEntity<T> query(URI uri, ParameterizedTypeReference<T> responseType) throws MetadataQueryException {
		final String commandDescription = String.format("call rest metadata on %s", uri);
		
		return performWithRetries(
				commandDescription,
				() -> {
					LOGGER.debug(commandDescription);
					final ResponseEntity<T> response = restTemplate.exchange(uri, HttpMethod.GET, null, responseType);
					handleReturnValueErrors(uri.toString(), response);
					LOGGER.debug("Rest api metadata call returned results");
					return response;					
				}
		);
	}

	private final <T> void handleReturnValueErrors(String uri, final ResponseEntity<T> response) throws MetadataQueryException {
		if (response == null) {
			throw new MetadataQueryException(String.format("Rest metadata call %s returned null", uri));
		}
		if (response.getStatusCode() != HttpStatus.OK) {
			throw new MetadataQueryException(
					String.format("Rest metadata call %s returned status code %s", uri, response.getStatusCode())
			);
		}
		if (response.getBody() == null) {
			throw new MetadataQueryException(String.format("Rest metadata call %s returned null body", uri));
		}
	}
	
	private final <T> T performWithRetries(
			final String commandDescription, 
			final Callable<T> command
	) throws MetadataQueryException {
		try {
			return Retries.performWithRetries(
					command,
					commandDescription,
					maxRetries, 
					retryInMillis
			);
		} catch (RuntimeException e) {
			// unwrap possible metadata exception
			if (e.getCause() instanceof MetadataQueryException) {
				final MetadataQueryException metadataException = (MetadataQueryException) e.getCause();
				throw metadataException;
			}
			else if (e.getCause() instanceof RestClientException) {
				final RestClientException restClientException = (RestClientException) e.getCause();				
				throw new MetadataQueryException(restClientException.getMessage(), restClientException);
			}
			// otherwise simply pass through exception
			throw e;
		} catch (InterruptedException e) {
			throw new RuntimeException(
					String.format("Interrupted on command execution of '%s'", commandDescription)
			);
		}
	}	

	private final int numResults(final ResponseEntity<List<SearchMetadata>> response) {
		if (response != null) {
			final List<SearchMetadata> res = response.getBody();
			if (res != null) {
				return res.size();
			}
		}
		return -1; // To indicate that null was returned and not an empty list
	}
}
