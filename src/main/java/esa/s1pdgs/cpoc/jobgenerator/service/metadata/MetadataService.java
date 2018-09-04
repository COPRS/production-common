package esa.s1pdgs.cpoc.jobgenerator.service.metadata;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import esa.s1pdgs.cpoc.common.errors.processing.JobGenMetadataException;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.L0AcnMetadata;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.L0SliceMetadata;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.SearchMetadata;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.SearchMetadataQuery;

/**
 * Metadata service
 * 
 * @author Cyrielle Gailliard
 *
 */
@Service
public class MetadataService {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(MetadataService.class);

	/**
	 * Client to request REST apis
	 */
	private final RestTemplate restTemplate;

	/**
	 * URI for querying EDRS session metadata
	 */
	private final String uriEdrsSession;

	/**
	 * URI for searching inputs in metadata
	 */
	private final String uriSearch;

	/**
	 * URI for querying L0 slices
	 */
	private final String uriL0Slice;
	
	/**
	 * Nb max of retry for querying api rest 
	 */
	private final int nbretry;
	/**
	 * Tempo of retry for querying api rest 
	 */
	private final int temporetryms;

	/**
	 * Constructor
	 * 
	 * @param restTemplate
	 * @param metadataHostname
	 */
	@Autowired
	public MetadataService(@Qualifier("restMetadataTemplate") final RestTemplate restTemplate,
			@Value("${metadata.host}") final String metadataHostname,
			@Value("${metadata.rest-api_nb-retry}") final int nbretry,
			@Value("${metadata.rest-api_tempo-retry-ms}") final int temporetryms) {
		this.restTemplate = restTemplate;
		this.uriEdrsSession = "http://" + metadataHostname + "/edrsSession";
		this.uriSearch = "http://" + metadataHostname + "/metadata";
		this.uriL0Slice = "http://" + metadataHostname + "/l0Slice";
		this.nbretry = nbretry;
		this.temporetryms = temporetryms;
	}

	/**
	 * If productType = blank, the metadata catalog will extract the product type
	 * from the product name
	 * 
	 * @param productType
	 * @param productName
	 * @return
	 * @throws MetadataException
	 */
	public EdrsSessionMetadata getEdrsSession(final String productType, final String productName)
			throws JobGenMetadataException {
		for (int retries = 0;; retries++) {
			try {
				String uri = this.uriEdrsSession + "/" + productType + "/" + productName;
				LOGGER.debug("Call rest metadata on {}", uri);
	
				ResponseEntity<EdrsSessionMetadata> response = this.restTemplate.exchange(uri, HttpMethod.GET, null,
						EdrsSessionMetadata.class);
				if (response.getStatusCode() != HttpStatus.OK) {
					if (retries < this.nbretry)
					{
						LOGGER.warn("Call rest api metadata failed: Attempt : {} / {}", retries, this.nbretry);
						try {
							Thread.sleep(this.temporetryms);
						} catch (InterruptedException e) {
							throw new JobGenMetadataException(e.getMessage(), e);
						}
						continue;
					}
					else {
						throw new JobGenMetadataException(
							String.format("Invalid HTTP status code %s", response.getStatusCode().name()));
					}
				}
				else {
					return response.getBody();
				}
			} catch (RestClientException e) {
				if (retries < this.nbretry)
				{
					LOGGER.warn("Call rest api metadata failed: Attempt : {} / {}", retries, this.nbretry);
					try {
						Thread.sleep(this.temporetryms);
					} catch (InterruptedException e1) {
						throw new JobGenMetadataException(e1.getMessage(), e1);
					}
					continue;
				}
				else {
					throw new JobGenMetadataException(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * If productType = blank, the metadata catalog will extract the product type
	 * from the product name
	 * 
	 * @param productType
	 * @param productName
	 * @return
	 * @throws MetadataException
	 */
	public L0SliceMetadata getSlice(final String productType, final String productName) throws JobGenMetadataException {
		for (int retries = 0;; retries++) {
			try {
				String uri = this.uriL0Slice + "/" + productType + "/" + productName;
				LOGGER.debug("Call rest metadata on {}", uri);
	
				ResponseEntity<L0SliceMetadata> response = this.restTemplate.exchange(uri, HttpMethod.GET, null,
						L0SliceMetadata.class);
				if (response.getStatusCode() != HttpStatus.OK) {
					if (retries < this.nbretry)
					{
						LOGGER.warn("Call rest api metadata failed: Attempt : {} / {}", retries, this.nbretry);
						try {
							Thread.sleep(this.temporetryms);
						} catch (InterruptedException e) {
							throw new JobGenMetadataException(e.getMessage(), e);
						}
						continue;
					}
					else {
					throw new JobGenMetadataException(
							String.format("Invalid HTTP status code %s", response.getStatusCode().name()));
					}
				}
				else {
					return response.getBody();
				}
			} catch (RestClientException e) {
				if (retries < this.nbretry)
				{
					LOGGER.warn("Call rest api metadata failed: Attempt : {} / {}", retries, this.nbretry);
					try {
						Thread.sleep(this.temporetryms);
					} catch (InterruptedException e1) {
						throw new JobGenMetadataException(e1.getMessage(), e1);
					}
					continue;
				}
				else {
					throw new JobGenMetadataException(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * If productType = blank, the metadata catalog will extract the product type
	 * from the product name
	 * 
	 * @param productType
	 * @param productName
	 * @return
	 * @throws MetadataException
	 */
	public L0AcnMetadata getFirstACN(final String productType, final String productName) throws JobGenMetadataException {
		for (int retries = 0;; retries++) {
			try {
				String uri = this.uriL0Slice + "/" + productType + "/" + productName + "/acns";
				LOGGER.debug("Call rest metadata on {}", uri);
	
				UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri).queryParam("mode", "ONE");
				ResponseEntity<L0AcnMetadata[]> response = this.restTemplate.exchange(builder.build().toUri(),
						HttpMethod.GET, null, L0AcnMetadata[].class);
				if (response != null && response.getStatusCode() != HttpStatus.OK) {
					if (retries < this.nbretry)
					{
						LOGGER.warn("Call rest api metadata failed: Attempt : {} / {}", retries, this.nbretry);
						try {
							Thread.sleep(this.temporetryms);
						} catch (InterruptedException e) {
							throw new JobGenMetadataException(e.getMessage(), e);
						}
						continue;
					}
					else {
						throw new JobGenMetadataException(
							String.format("Invalid HTTP status code %s", response.getStatusCode().name()));
					}
				}
				else {
					if (response != null && response.getBody() != null) {
						L0AcnMetadata[] objects = response.getBody();
						if (objects != null && objects.length > 0) {
							return objects[0];
						}
					}
					if (retries < this.nbretry)
					{
						LOGGER.warn("Call rest api metadata failed: Attempt : {} / {}", retries, this.nbretry);
						try {
							Thread.sleep(this.temporetryms);
						} catch (InterruptedException e) {
							throw new JobGenMetadataException(e.getMessage(), e);
						}
						continue;
					}
					else {
						throw new JobGenMetadataException(String.format("No retrieved ACNs for %s", productName));
					}
				}
	
			} catch (RestClientException e) {
				if (retries < this.nbretry)
				{
					LOGGER.warn("Call rest api metadata failed: Attempt : {} / {}", retries, this.nbretry);
					try {
						Thread.sleep(this.temporetryms);
					} catch (InterruptedException e1) {
						throw new JobGenMetadataException(e1.getMessage(), e1);
					}
					continue;
				}
				else {
					throw new JobGenMetadataException(e.getMessage(), e);
				}
			}
		}
	}

	public SearchMetadata search(final SearchMetadataQuery query, final Date t0, final Date t1,
			final String satelliteId, final int instrumentConfigurationId) throws JobGenMetadataException {
		for (int retries = 0;; retries++) {
			try {
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				String uri = this.uriSearch + "/search";
				UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
						.queryParam("productType", query.getProductType()).queryParam("mode", query.getRetrievalMode())
						.queryParam("t0", format.format(t0)).queryParam("t1", format.format(t1))
						.queryParam("dt0", query.getDeltaTime0()).queryParam("dt1", query.getDeltaTime1())
						.queryParam("satellite", satelliteId);
				if (instrumentConfigurationId != -1) {
					builder.queryParam("insConfId", instrumentConfigurationId);
				}
				LOGGER.debug("Call rest metadata on [{}]", builder.build().toUri());
				
				ResponseEntity<SearchMetadata> response = this.restTemplate.exchange(builder.build().toUri(),
						HttpMethod.GET, null, SearchMetadata.class);
				if (response.getStatusCode() != HttpStatus.OK) {
					if (retries < this.nbretry)
					{
						LOGGER.warn("Call rest api metadata failed: Attempt : {} / {}", retries, this.nbretry);
						try {
							Thread.sleep(this.temporetryms);
						} catch (InterruptedException e) {
							throw new JobGenMetadataException(e.getMessage(), e);
						}
						continue;
					}
					else {
						throw new JobGenMetadataException(
							String.format("Invalid HTTP status code %s", response.getStatusCode().name()));
					}
				}
				else {
					return response.getBody();
				}
			} catch (RestClientException e) {
				if (retries < this.nbretry)
				{
					LOGGER.warn("Call rest api metadata failed: Attempt : {} / {}", retries, this.nbretry);
					try {
						Thread.sleep(this.temporetryms);
					} catch (InterruptedException e1) {
						throw new JobGenMetadataException(e1.getMessage(), e1);
					}
					continue;
				}
				else {
					throw new JobGenMetadataException(e.getMessage(), e);
				}
			}
		}
	}

}
