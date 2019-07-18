package esa.s1pdgs.cpoc.validation.service.metadata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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

	@Autowired
	public MetadataService(@Qualifier("restMetadataTemplate") final RestTemplate restTemplate,
			@Value("${metadata.host}") final String metadataHostname,
			@Value("${metadata.rest-api_nb-retry}") final int nbretry,
			@Value("${metadata.rest-api_tempo-retry-ms}") final int temporetryms) {
		this.restTemplate = restTemplate;
	}
	
	public void query() {
		 
	}
}
