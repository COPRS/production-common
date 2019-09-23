package esa.s1pdgs.cpoc.metadata.client;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

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
			return null;
		} else {
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
				new ParameterizedTypeReference<L0SliceMetadata>() {});

		if (response == null) {
			return null;
		} else {
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
				new ParameterizedTypeReference<LevelSegmentMetadata>() {});

		if (response == null) {
			return null;
		} else {
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
		
		ResponseEntity<L0AcnMetadata[]> response = query(builder.build().toUri(), new ParameterizedTypeReference<L0AcnMetadata[]>(){});

		if (response == null) {
			return null;
		} else {
			L0AcnMetadata[] objects = response.getBody();
			if (objects != null && objects.length > 0) {
				return objects[0];
			} else {
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
	@SuppressWarnings("unchecked")
	public List<SearchMetadata> search(final SearchMetadataQuery query, final String t0, final String t1,
			final String satelliteId, final int instrumentConfigurationId, final String processMode)
			throws MetadataQueryException {

		String uri = this.metadataBaseUri + MetadataCatalogRestPath.METADATA.path() + "/"
				+ query.getProductFamily().toString() + "/search";
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
				.queryParam("productType", query.getProductType()).queryParam("mode", query.getRetrievalMode())
				.queryParam("t0", t0).queryParam("t1", t1).queryParam("dt0", query.getDeltaTime0())
				.queryParam("dt1", query.getDeltaTime1()).queryParam("satellite", satelliteId);
		if (processMode != null) {
			builder.queryParam("processMode", processMode);
		}
		if (instrumentConfigurationId != -1) {
			builder.queryParam("insConfId", instrumentConfigurationId);
		}

		ResponseEntity<List<SearchMetadata>> response = query(builder.build().toUri(),
				new ParameterizedTypeReference<List<SearchMetadata>>() {});

		if (response == null) {
			return null;
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
	@SuppressWarnings("unchecked")
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
				new ParameterizedTypeReference<List<SearchMetadata>>() {});

		if (response == null) {
			return null;
		} else {
			LOGGER.debug("Metadata query for family '{}' returned {} results", family, numResults(response));
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
		int notAvailableRetries = 10;

		String uri = this.metadataBaseUri + MetadataCatalogRestPath.L0_SLICE.path() + "/" + family + "/" + productName
				+ "/seaCoverage";
		for (int retries = 0;; retries++) {
			try {
				LOGGER.debug("Call rest metadata on {}", uri);

				ResponseEntity<Integer> response = this.restTemplate.exchange(uri, HttpMethod.GET, null, Integer.class);
				while (response.getStatusCode() == HttpStatus.NOT_FOUND) {
					LOGGER.debug("Product not available yet. Waiting...");
					trySleep();
					notAvailableRetries--;
					LOGGER.debug("Call rest metadata on {}", uri);
					response = this.restTemplate.exchange(uri, HttpMethod.GET, null, Integer.class);
					if (notAvailableRetries <= 0) {
						LOGGER.trace("Max number of retries reached for {}", productName);
						break;
					}
				}

				if (response.getStatusCode() != HttpStatus.OK) {
					if (retries < this.maxRetries) {
						LOGGER.warn("Call rest api metadata failed: Attempt : {} / {}", retries, this.maxRetries);
						trySleep();
						continue;
					} else {
						throw new MetadataQueryException(
								String.format("Invalid HTTP status code %s", response.getStatusCode().name()));
					}

				} else if (response != null) {
					final Integer res = response.getBody();
					LOGGER.debug("Got coverage {}", res);

					if (res == null) {
						throw new MetadataQueryException("getSeaCoverage returned null");
					}
					return res;
				}
			} catch (RestClientException e) {
				if (retries < this.maxRetries) {
					LOGGER.warn("Call rest api metadata failed: Attempt : {} / {}", retries, this.maxRetries);
					trySleep();
					continue;
				} else {
					throw new MetadataQueryException(e.getMessage(), e);
				}
			}
		}
	}

	private <T> ResponseEntity<T> query(URI uri, ParameterizedTypeReference<T> responseType) throws MetadataQueryException {

		for (int retries = 0;; retries++) {
			try {
				LOGGER.debug("Call rest metadata on {}", uri);
				ResponseEntity<T> response = this.restTemplate.exchange(uri, HttpMethod.GET, null, responseType);

				if (response != null && response.getStatusCode() != HttpStatus.OK) {
					if (retries < this.maxRetries) {
						LOGGER.warn("Call rest api metadata failed: Attempt : {} / {}", retries, this.maxRetries);
						trySleep();
						continue;
					} else {
						throw new MetadataQueryException(
								String.format("Invalid HTTP status code %s", response.getStatusCode().name()));
					}
				} else {
					if (response != null && response.getBody() != null) {
						LOGGER.debug("Metadata query returned results");
						return response;
					} else {
						if (retries < this.maxRetries) {
							LOGGER.warn("Call rest api metadata failed: Attempt : {} / {}", retries, this.maxRetries);
							trySleep();
							continue;
						} else {
							LOGGER.warn("Metadata query returned no results");
							return null;
						}
					}
				}

			} catch (RestClientException e) {
				if (retries < this.maxRetries) {
					LOGGER.warn("Call rest api metadata failed: Attempt : {} / {}", retries, this.maxRetries);
					trySleep();
					continue;
				} else {
					throw new MetadataQueryException(e.getMessage(), e);
				}
			}
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

	private final void trySleep() throws MetadataQueryException {
		try {
			Thread.sleep(this.retryInMillis);
		} catch (InterruptedException e) {
			throw new MetadataQueryException(e.getMessage(), e);
		}
	}

}
