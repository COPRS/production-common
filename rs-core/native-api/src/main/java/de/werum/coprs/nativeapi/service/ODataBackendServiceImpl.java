package de.werum.coprs.nativeapi.service;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import de.werum.coprs.nativeapi.config.NativeApiProperties;
import de.werum.coprs.nativeapi.rest.model.stac.StacItemCollection;
import de.werum.coprs.nativeapi.service.exception.NativeApiException;
import de.werum.coprs.nativeapi.service.mapping.PripToStacMapper;

@Service
public class ODataBackendServiceImpl {
	private NativeApiProperties properties;
	private RestTemplate restTemplate;
	
	private final URL internalPripUrl;
	private final URI externalPripUrl;


	@Autowired
	public ODataBackendServiceImpl(final NativeApiProperties properties, final RestTemplate restTemplate) {
		this.properties = properties;
		this.restTemplate = restTemplate;
		
		this.internalPripUrl = buildInternalPripUrl(properties);
		this.externalPripUrl = buildExternalPripUrl(properties);
	}
	
	private static final Logger LOG = LogManager.getLogger(ODataBackendServiceImpl.class);
	
	String buildPripQueryUrl(final String filterQuery, final boolean includeAdditionalAttributes, final int page) {
		return buildPripQueryUrl(internalPripUrl, filterQuery, includeAdditionalAttributes, properties.getDefaultLimit(), page);
	}
	
	public StacItemCollection queryOData(String url) {
		LOG.debug("sending PRIP request: {}", url);
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		final HttpEntity<String> requestEntity = new HttpEntity<>(null, httpHeaders);
		final ResponseEntity<String> responseEntity = this.restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

		return mapResponse(responseEntity, externalPripUrl, properties.getIncludeAdditionalAttributes());
	}
	
	static StacItemCollection mapResponse(final ResponseEntity<String> responseEntity, final URI externalPripUrl,
			final boolean includeAdditionalAttributes) {
		if (null != responseEntity) {
			if (HttpStatus.OK != responseEntity.getStatusCode()) {
				throw new NativeApiException(String.format("PRIP could not successfully be queried: %s", responseEntity.getBody()),
						responseEntity.getStatusCode());
			}

			final String responseBody = responseEntity.getBody();
			LOG.trace("Response is code:{} body:{}", responseEntity.getStatusCode(), responseBody);
			
			if (null != responseBody) {
				final JsonReader jsonReader = Json.createReader(new StringReader(responseBody));
			    final JsonObject jsonObject = jsonReader.readObject();
			    jsonReader.close();

				if (null != jsonObject && !jsonObject.containsKey("value")) {
					throw new NativeApiException("missing 'value' property in PRIP response ", HttpStatus.INTERNAL_SERVER_ERROR);
				}
				try {
					return PripToStacMapper.mapFromPripOdataJson(jsonObject, externalPripUrl, includeAdditionalAttributes);
				} catch (JsonException | URISyntaxException e) {
					throw new NativeApiException("error mapping PRIP response to STAC item collection", e, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
		}

		return null;
	}
	
	static String buildPripQueryUrl(final URL pripUrl, final String oDataQuery, final boolean includeAdditionalAttributes, final int limit, final int page) {
		String pripFilterUrl = String.format("%s%s%s", pripUrl, "/odata/v1/Products?$filter=", oDataQuery);

		if (includeAdditionalAttributes) {
			pripFilterUrl = String.format("%s%s", pripFilterUrl, pripFilterUrl.endsWith("?") ? "$expand=Attributes,Quicklooks" : "&$expand=Attributes,Quicklooks");
		}
		
		// Adding top param using the default limit
		String topUrl = String.format("$top=%s", limit);
		pripFilterUrl = String.format("%s%s", pripFilterUrl, pripFilterUrl.endsWith("?") ? topUrl : "&"+topUrl );
		
		// Adding skip param using the page. If no page is set we ignore it.
		if (page == 0) {
			String skipUrl = String.format("$skip=%s", page);
			pripFilterUrl = String.format("%s%s", pripFilterUrl, pripFilterUrl.endsWith("?") ? topUrl : "&"+skipUrl );	
		}
	
		return pripFilterUrl;// UriUtils.encodePath(pripFilterUrl, "UTF-8");
	}
	
	static URL buildInternalPripUrl(final NativeApiProperties apiProperties) {
		return buildPripUrl(apiProperties.getPripProtocol(), apiProperties.getPripHost(), apiProperties.getPripPort());
	}

	static URI buildExternalPripUrl(final NativeApiProperties apiProperties) {
		try {
			return buildPripUrl(Objects.requireNonNull(apiProperties).getExternalPripProtocol(), apiProperties.getExternalPripHost(),
					apiProperties.getExternalPripPort()).toURI();
		} catch (final Exception e) {
			final String msg = String.format(
					"could not initialize PRIP URL for external interface, metadata/download links will not be added to responses [protocol (%s), host (%s) and port (%s)]: %s",
					apiProperties.getExternalPripProtocol(), apiProperties.getExternalPripHost(), apiProperties.getExternalPripPort(), e.getMessage());
			LOG.warn(msg);
			return null;
		}
	}

	static URL buildPripUrl(final String protocol, final String host, final int port) {
		try {
			return UriComponentsBuilder
					.fromHttpUrl(String.format("%s://%s:%d",
							Objects.requireNonNull(protocol),
							Objects.requireNonNull(host),
							port))
					.build().toUri().toURL();
		} catch (final MalformedURLException e) {
			final String msg = String.format("could not initialize PRIP URL; protocol (%s), host (%s) and port (%s) must be valid: %s",
					protocol, host, port, e.getMessage());
			throw new IllegalArgumentException(msg, e);
		}
	}
}
 