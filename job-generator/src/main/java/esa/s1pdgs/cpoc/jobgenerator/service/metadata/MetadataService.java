package esa.s1pdgs.cpoc.jobgenerator.service.metadata;

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
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMetadataException;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.SearchMetadataQuery;
import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0AcnMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0SliceMetadata;
import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;


/**
 * Metadata service
 * 
 * @author Cyrielle Gailliard
 */
@Service
public class MetadataService {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(MetadataService.class);

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
     * URI for querying level segments
     */
    private final String uriLevelSegment;

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
    public MetadataService(
            @Qualifier("restMetadataTemplate") final RestTemplate restTemplate,
            @Value("${metadata.host}") final String metadataHostname,
            @Value("${metadata.rest-api_nb-retry}") final int nbretry,
            @Value("${metadata.rest-api_tempo-retry-ms}") final int temporetryms) {
        this.restTemplate = restTemplate;
        this.uriEdrsSession = "http://" + metadataHostname + "/edrsSession";
        this.uriSearch = "http://" + metadataHostname + "/metadata";
        this.uriL0Slice = "http://" + metadataHostname + "/l0Slice";
        this.uriLevelSegment = "http://" + metadataHostname + "/level_segment";
        this.nbretry = nbretry;
        this.temporetryms = temporetryms;
    }

    /**
     * If productType = blank, the metadata catalog will extract the product
     * type from the product name
     * 
     * @param productType
     * @param productName
     * @return
     * @throws MetadataException
     */
    public EdrsSessionMetadata getEdrsSession(final String productType,
            final String productName) throws JobGenMetadataException {
        for (int retries = 0;; retries++) {
            try {
                String uri = this.uriEdrsSession + "/" + productType + "/"
                        + productName;
                LOGGER.debug("Call rest metadata on {}", uri);

                ResponseEntity<EdrsSessionMetadata> response =
                        this.restTemplate.exchange(uri, HttpMethod.GET, null,
                                EdrsSessionMetadata.class);
                if (response.getStatusCode() != HttpStatus.OK) {
                    if (retries < this.nbretry) {
                        LOGGER.warn(
                                "Call rest api metadata failed: Attempt : {} / {}",
                                retries, this.nbretry);
                        try {
                            Thread.sleep(this.temporetryms);
                        } catch (InterruptedException e) {
                            throw new JobGenMetadataException(e.getMessage(),
                                    e);
                        }
                        continue;
                    } else {
                        throw new JobGenMetadataException(
                                String.format("Invalid HTTP status code %s",
                                        response.getStatusCode().name()));
                    }
                } else {
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
                        throw new JobGenMetadataException(e1.getMessage(), e1);
                    }
                    continue;
                } else {
                    throw new JobGenMetadataException(e.getMessage(), e);
                }
            }
        }
    }
    
	public int getSeaCoverage(String productName) throws JobGenMetadataException {
        for (int retries = 0;; retries++) {
            try {
                String uri = this.uriLevelSegment + "/" + productName + "/seaCoverage";
                LOGGER.debug("Call rest metadata on {}", uri);

                ResponseEntity<Integer> response =
                        this.restTemplate.exchange(uri, HttpMethod.GET, null,
                        		Integer.class);
                if (response.getStatusCode() != HttpStatus.OK) {
                    if (retries < this.nbretry) {
                        LOGGER.warn(
                                "Call rest api metadata failed: Attempt : {} / {}",
                                retries, this.nbretry);
                        try {
                            Thread.sleep(this.temporetryms);
                        } catch (InterruptedException e) {
                            throw new JobGenMetadataException(e.getMessage(),
                                    e);
                        }
                        continue;
                    } else {
                        throw new JobGenMetadataException(
                                String.format("Invalid HTTP status code %s",
                                        response.getStatusCode().name()));
                    }
                } else if (response != null) {
                    final Integer res = response.getBody();
                    
                    if (res == null) {
                    	throw new JobGenMetadataException("getSeaCoverage returned null");
                    }                    
                    return res;
                }                 
            } catch (RestClientException e) {
                if (retries < this.nbretry) {
                    LOGGER.warn(
                            "Call rest api metadata failed: Attempt : {} / {}",
                            retries, this.nbretry);
                    try {
                        Thread.sleep(this.temporetryms);
                    } catch (InterruptedException e1) {
                        throw new JobGenMetadataException(e1.getMessage(), e1);
                    }
                    continue;
                } else {
                    throw new JobGenMetadataException(e.getMessage(), e);
                }
            }
        }
	}

    /**
     * If productType = blank, the metadata catalog will extract the product
     * type from the product name
     * 
     * @param productType
     * @param productName
     * @return
     * @throws MetadataException
     */
    public L0SliceMetadata getL0Slice(final String productName)
            throws JobGenMetadataException {
        for (int retries = 0;; retries++) {
            try {
                String uri = this.uriL0Slice + "/" + productName;
                LOGGER.debug("Call rest metadata on {}", uri);

                ResponseEntity<L0SliceMetadata> response =
                        this.restTemplate.exchange(uri, HttpMethod.GET, null,
                                L0SliceMetadata.class);
                if (response.getStatusCode() != HttpStatus.OK) {
                    if (retries < this.nbretry) {
                        LOGGER.warn(
                                "Call rest api metadata failed: Attempt : {} / {}",
                                retries, this.nbretry);
                        try {
                            Thread.sleep(this.temporetryms);
                        } catch (InterruptedException e) {
                            throw new JobGenMetadataException(e.getMessage(),
                                    e);
                        }
                        continue;
                    } else {
                        throw new JobGenMetadataException(
                                String.format("Invalid HTTP status code %s",
                                        response.getStatusCode().name()));
                    }
                } else {
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
                        throw new JobGenMetadataException(e1.getMessage(), e1);
                    }
                    continue;
                } else {
                    throw new JobGenMetadataException(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * If productType = blank, the metadata catalog will extract the product
     * type from the product name
     * 
     * @param productType
     * @param productName
     * @return
     * @throws MetadataException
     */
    public LevelSegmentMetadata getLevelSegment(final ProductFamily family,
            final String productName) throws JobGenMetadataException {
        for (int retries = 0;; retries++) {
            try {
                String uri =
                        this.uriLevelSegment + "/" + family + "/" + productName;
                LOGGER.debug("Call rest metadata on {}", uri);

                ResponseEntity<LevelSegmentMetadata> response =
                        this.restTemplate.exchange(uri, HttpMethod.GET, null,
                                LevelSegmentMetadata.class);
                if (response.getStatusCode() != HttpStatus.OK) {
                    if (retries < this.nbretry) {
                        LOGGER.warn(
                                "Call rest api metadata failed: Attempt : {} / {}",
                                retries, this.nbretry);
                        try {
                            Thread.sleep(this.temporetryms);
                        } catch (InterruptedException e) {
                            throw new JobGenMetadataException(e.getMessage(),
                                    e);
                        }
                        continue;
                    } else {
                        throw new JobGenMetadataException(
                                String.format("Invalid HTTP status code %s",
                                        response.getStatusCode().name()));
                    }
                } else {
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
                        throw new JobGenMetadataException(e1.getMessage(), e1);
                    }
                    continue;
                } else {
                    throw new JobGenMetadataException(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * If productType = blank, the metadata catalog will extract the product
     * type from the product name
     * 
     * @param productType
     * @param productName
     * @return
     * @throws MetadataException
     */
    public L0AcnMetadata getFirstACN(final String productName,
            final String processMode) throws JobGenMetadataException {
        for (int retries = 0;; retries++) {
            try {
                String uri = this.uriL0Slice + "/" + productName + "/acns";
                LOGGER.debug("Call rest metadata on {}", uri);

                UriComponentsBuilder builder = UriComponentsBuilder
                        .fromUriString(uri).queryParam("mode", "ONE")
                        .queryParam("processMode", processMode);
                ResponseEntity<L0AcnMetadata[]> response =
                        this.restTemplate.exchange(builder.build().toUri(),
                                HttpMethod.GET, null, L0AcnMetadata[].class);
                if (response != null
                        && response.getStatusCode() != HttpStatus.OK) {
                    if (retries < this.nbretry) {
                        LOGGER.warn(
                                "Call rest api metadata failed: Attempt : {} / {}",
                                retries, this.nbretry);
                        try {
                            Thread.sleep(this.temporetryms);
                        } catch (InterruptedException e) {
                            throw new JobGenMetadataException(e.getMessage(),
                                    e);
                        }
                        continue;
                    } else {
                        throw new JobGenMetadataException(
                                String.format("Invalid HTTP status code %s",
                                        response.getStatusCode().name()));
                    }
                } else {
                    if (response != null && response.getBody() != null) {
                        L0AcnMetadata[] objects = response.getBody();
                        if (objects != null && objects.length > 0) {
                            return objects[0];
                        }
                    }
                    if (retries < this.nbretry) {
                        LOGGER.warn(
                                "Call rest api metadata failed: Attempt : {} / {}",
                                retries, this.nbretry);
                        try {
                            Thread.sleep(this.temporetryms);
                        } catch (InterruptedException e) {
                            throw new JobGenMetadataException(e.getMessage(),
                                    e);
                        }
                        continue;
                    } else {
                        throw new JobGenMetadataException(String.format(
                                "No retrieved ACNs for %s", productName));
                    }
                }

            } catch (RestClientException e) {
                if (retries < this.nbretry) {
                    LOGGER.warn(
                            "Call rest api metadata failed: Attempt : {} / {}",
                            retries, this.nbretry);
                    try {
                        Thread.sleep(this.temporetryms);
                    } catch (InterruptedException e1) {
                        throw new JobGenMetadataException(e1.getMessage(), e1);
                    }
                    continue;
                } else {
                    throw new JobGenMetadataException(e.getMessage(), e);
                }
            }
        }
    }

    public List<SearchMetadata> search(final SearchMetadataQuery query,
            final String t0, final String t1, final String satelliteId,
            final int instrumentConfigurationId, final String processMode)
            throws JobGenMetadataException {
        for (int retries = 0;; retries++) {
            try {
                String uri = this.uriSearch + "/"
                        + query.getProductFamily().toString() + "/search";
                UriComponentsBuilder builder = UriComponentsBuilder
                        .fromUriString(uri)
                        .queryParam("productType", query.getProductType())
                        .queryParam("mode", query.getRetrievalMode())
                        .queryParam("t0", t0).queryParam("t1", t1)
                        .queryParam("dt0", query.getDeltaTime0())
                        .queryParam("dt1", query.getDeltaTime1())
                        .queryParam("satellite", satelliteId);
                if (processMode != null) {
                    builder.queryParam("processMode", processMode);
                }
                if (instrumentConfigurationId != -1) {
                    builder.queryParam("insConfId", instrumentConfigurationId);
                }
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
                            throw new JobGenMetadataException(e.getMessage(),
                                    e);
                        }
                        continue;
                    } else {
                        throw new JobGenMetadataException(
                                String.format("Invalid HTTP status code %s",
                                        response.getStatusCode().name()));
                    }
                } else {
                	LOGGER.info("Metadata query for family '{}' and product type '{}' returned {} results", 
                			query.getProductFamily(), query.getProductType(), numResults(response));
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
                        throw new JobGenMetadataException(e1.getMessage(), e1);
                    }
                    continue;
                } else {
                    throw new JobGenMetadataException(e.getMessage(), e);
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



}
