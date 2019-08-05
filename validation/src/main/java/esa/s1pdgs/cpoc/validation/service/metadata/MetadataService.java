package esa.s1pdgs.cpoc.validation.service.metadata;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;

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
	
	private String uriBase;
	
    /**
     * Nb max of retry for querying api rest
     */
    private final int nbretry;
    /**
     * Tempo of retry for querying api rest
     */
    private final int temporetryms;

	@Autowired
	public MetadataService(@Qualifier("restValidationTemplate") final RestTemplate restTemplate,
			@Value("${metadata.host}") final String metadataHostname,
			@Value("${metadata.rest-api_nb-retry}") final int nbretry,
			@Value("${metadata.rest-api_tempo-retry-ms}") final int temporetryms) {
		this.restTemplate = restTemplate;
		this.uriBase = "http://" + metadataHostname + "/metadata";
		this.nbretry = nbretry;
		this.temporetryms = temporetryms;
	}
	
	public List<SearchMetadata> query(ProductFamily family, LocalDateTime intervalStart, LocalDateTime intervalStop) throws MetadataQueryException {
		for (int retries = 0;; retries++) {
            try {
                String uri = this.uriBase + "/"
                        + family.toString() + "/searchInterval";
                
                UriComponentsBuilder builder = UriComponentsBuilder
                        .fromUriString(uri)
                        // FIXME: Maybe we not need a productType anyways!
                        //.queryParam("productType", productType)
                        .queryParam("intervalStart", intervalStart.format(DateUtils.METADATA_DATE_FORMATTER))
                        .queryParam("intervalStop", intervalStop.format(DateUtils.METADATA_DATE_FORMATTER));
                
                LOGGER.debug("Call rest metadata on [{}]",
                        builder.build().toUri());

                ResponseEntity<List<SearchMetadata>> response =
                        this.restTemplate.exchange(builder.build().toUri(),
                                HttpMethod.GET, null,
                                new ParameterizedTypeReference<List<SearchMetadata>>() {
                                });
                if (response.getStatusCode() != HttpStatus.OK) {
                    if (retries < this.nbretry) {
                        LOGGER.warn(
                                "Call rest api metadata failed: Attempt : {} / {}",
                                retries, this.nbretry);
                        try {
                            Thread.sleep(this.temporetryms);
                        } catch (InterruptedException e) {
                            throw new MetadataQueryException(e.getMessage(),
                                    e);
                        }
                        continue;
                    } else {
                        throw new MetadataQueryException(
                                String.format("Invalid HTTP status code %s",
                                        response.getStatusCode().name()));
                    }
                } else {
                	LOGGER.info("Metadata query for family '{}' and product type '{}' returned {} results", family, response.getBody().size());                
                    return response.getBody();
                }
            } catch (RestClientException e) {
                if (retries < this.nbretry) {
                    LOGGER.warn(
                            "Call rest api metadata failed: Attempt : {} / {}",
                            retries, this.nbretry);
                    try {
                        Thread.sleep(this.temporetryms);
                    } catch (InterruptedException e1) {
                        throw new MetadataQueryException(e1.getMessage(), e1);
                    }
                    continue;
                } else {
                    throw new MetadataQueryException(e.getMessage(), e);
                }
            }
        }
	}
}
