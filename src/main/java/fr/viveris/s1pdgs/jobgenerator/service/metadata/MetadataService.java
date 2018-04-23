package fr.viveris.s1pdgs.jobgenerator.service.metadata;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import fr.viveris.s1pdgs.jobgenerator.exception.MetadataException;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.EdrsSessionMetadata;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.L0AcnMetadata;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.L0SliceMetadata;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.SearchMetadata;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.SearchMetadataQuery;

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
	private static final Logger LOGGER = LoggerFactory.getLogger(MetadataService.class);

	private final RestTemplate restTemplate;

	private final String metadataUriEdrsSession;
	private final String metadataUriSearch;
	private final String metadataUriL0Slice;

	@Autowired
	public MetadataService(@Qualifier("restMetadataTemplate") final RestTemplate restTemplate,
			@Value("${metadata.host}") final String metadataHostname) {
		this.restTemplate = restTemplate;
		this.metadataUriEdrsSession = "http://" + metadataHostname + "/edrsSession";
		this.metadataUriSearch = "http://" + metadataHostname + "/metadata";
		this.metadataUriL0Slice = "http://" + metadataHostname + "/l0Slice";
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
	public EdrsSessionMetadata getEdrsSession(String productType, String productName) throws MetadataException {
		try {
			String uri = this.metadataUriEdrsSession + "/" + productType + "/" + productName;
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Call rest metadata on {}", uri);
			}

			ResponseEntity<EdrsSessionMetadata> response = this.restTemplate.exchange(uri, HttpMethod.GET, null,
					EdrsSessionMetadata.class);
			if (response.getStatusCode() != HttpStatus.OK) {
				throw new MetadataException(
						String.format("Invalid HTTP status code %s", response.getStatusCode().name()));
			}
			return response.getBody();
		} catch (RestClientException e) {
			throw new MetadataException(e.getMessage(), e);
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
	public L0SliceMetadata getSlice(String productType, String productName) throws MetadataException {
		try {
			String uri = this.metadataUriL0Slice + "/" + productType + "/" + productName;
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Call rest metadata on {}", uri);
			}

			ResponseEntity<L0SliceMetadata> response = this.restTemplate.exchange(uri, HttpMethod.GET, null,
					L0SliceMetadata.class);
			if (response.getStatusCode() != HttpStatus.OK) {
				throw new MetadataException(
						String.format("Invalid HTTP status code %s", response.getStatusCode().name()));
			}
			return response.getBody();
		} catch (RestClientException e) {
			throw new MetadataException(e.getMessage(), e);
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
	public L0AcnMetadata getFirstACN(String productType, String productName) throws MetadataException {
		try {
			String uri = this.metadataUriL0Slice + "/" + productType + "/" + productName + "/acns";
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Call rest metadata on {}", uri);
			}

			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri).queryParam("mode", "ONE");
			ResponseEntity<L0AcnMetadata[]> response = this.restTemplate.exchange(builder.build().toUri(),
					HttpMethod.GET, null, L0AcnMetadata[].class);
			if (response != null && response.getStatusCode() != HttpStatus.OK) {
				throw new MetadataException(
						String.format("Invalid HTTP status code %s", response.getStatusCode().name()));
			}
			if (response != null && response.getBody() != null) {
				L0AcnMetadata[] objects = response.getBody();
				if (objects != null && objects.length > 0) {
					return objects[0];
				}
			}
			
			throw new MetadataException(String.format("No retrieved ACNs for %s", productName));

		} catch (RestClientException e) {
			throw new MetadataException(e.getMessage(), e);
		}
	}

	public SearchMetadata search(SearchMetadataQuery query, Date t0, Date t1, String satelliteId,
			int instrumentConfigurationId) throws MetadataException {
		try {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			String uri = this.metadataUriSearch + "/search";
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(uri)
					.queryParam("productType", query.getProductType()).queryParam("mode", query.getRetrievalMode())
					.queryParam("t0", format.format(t0)).queryParam("t1", format.format(t1))
					.queryParam("dt0", query.getDeltaTime0()).queryParam("dt1", query.getDeltaTime1())
					.queryParam("satellite", satelliteId);
			if (instrumentConfigurationId != -1) {
				builder.queryParam("insConfId", instrumentConfigurationId);
			}
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Call rest metadata on [{}]", builder.build().toUri());
			}
			ResponseEntity<SearchMetadata> response = this.restTemplate.exchange(builder.build().toUri(),
					HttpMethod.GET, null, SearchMetadata.class);
			if (response.getStatusCode() != HttpStatus.OK) {
				throw new MetadataException(
						String.format("Invalid HTTP status code %s", response.getStatusCode().name()));
			}
			return response.getBody();
		} catch (RestClientException e) {
			throw new MetadataException(e.getMessage(), e);
		}
	}

}
