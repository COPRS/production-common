package de.werum.coprs.ddip.frontend.service.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import de.werum.coprs.ddip.frontend.config.DdipProperties;
import de.werum.coprs.ddip.frontend.util.DdipUtil;

@CrossOrigin
@RestController
@RequestMapping(value = "/odata")
public class OdataRestController {

	public static final String ATTR_COLLECTION_NAME = "Collection/Name";
	public static final Pattern RIGHT_VALUE_DELIMITERS = Pattern.compile(" |\\)|&|$");

	private static final Logger LOGGER = LoggerFactory.getLogger(OdataRestController.class);

	private final RestTemplate restTemplate;

	private final DdipProperties ddipProperties;

	private final URL dispatchPripUrl;

	private final Map<String, String> collections;

	@Autowired
	public OdataRestController(final DdipProperties ddipProperties, final RestTemplate restTemplate) {
		this.ddipProperties = ddipProperties;
		this.dispatchPripUrl = buildDispatchPripUrl(ddipProperties);
		this.restTemplate = restTemplate;
		this.collections = null != ddipProperties.getCollections() ? new HashMap<>(ddipProperties.getCollections()) : new HashMap<>();
	}

	@RequestMapping(value = "/v1/**")
	public void handleOdataRequest(HttpServletRequest request, HttpServletResponse response) {
		final String queryParams = request.getQueryString() == null ? "" : "?" + request.getQueryString();
		LOGGER.info("Received HTTP request for URL: {}{}", request.getRequestURL().toString(), queryParams);

		final String queryUrl = String.format("%s%s%s", this.dispatchPripUrl, request.getRequestURI(), this.modifyQueryParams(queryParams));
		LOGGER.info("Redirecting HTTP request to URL: {}", queryUrl);

		final HttpHeaders httpHeaders = getHeaders(request);
		final HttpEntity<String> requestEntity = new HttpEntity<>(null, httpHeaders);

		final ResponseEntity<String> responseEntity = this.restTemplate.exchange(queryUrl, HttpMethod.resolve(request.getMethod()), requestEntity,
				String.class);

		try {
			mapResponse(responseEntity, response);
		} catch (final IOException e) {
			throw new RuntimeException(String.format("error processing PRIP response: %s", e.getMessage()), e);
		}
	}

	private boolean isDdipFeature(final String queryParams) {
		return this.isCollectionFeature(queryParams);
	}

	private boolean isCollectionFeature(final String queryParams) {
		return null != queryParams && queryParams.contains(ATTR_COLLECTION_NAME);
	}

	private String modifyQueryParams(final String queryParams) {
		if (this.isDdipFeature(queryParams)) {
			return translateCollectionQueryParameters(queryParams, this.collections);
		}

		return queryParams;
	}

	static URL buildDispatchPripUrl(final DdipProperties ddipProperties) {
		try {
			return UriComponentsBuilder
					.fromHttpUrl(String.format("%s://%s:%d",
							Objects.requireNonNull(ddipProperties).getDispatchPripProtocol(),
							ddipProperties.getDispatchPripHost(),
							ddipProperties.getDispatchPripPort()))
					.build().toUri().toURL();
		} catch (final MalformedURLException e) {
			final String msg = String.format("could not initialize PRIP URL for request disptaching; protocol (%s), host (%s) and port (%s) must be valid: %s",
					ddipProperties.getDispatchPripProtocol(), ddipProperties.getDispatchPripHost(), ddipProperties.getDispatchPripPort(), e.getMessage());
			throw new IllegalArgumentException(msg, e);
		}
	}

	static HttpHeaders getHeaders(final HttpServletRequest request) {
		return Collections.list(request.getHeaderNames()).stream() //
				.collect(Collectors.toMap( //
						Function.identity(), //
						headerName -> Collections.list(request.getHeaders(headerName)), //
						(oldVal, newVal) -> newVal, //
						HttpHeaders::new //
						));
	}

	static void mapResponse(final ResponseEntity<String> responseEntity, final HttpServletResponse servletResponse) throws IOException {
		if (null != responseEntity) {
			for (final Map.Entry<String, List<String>> headers : responseEntity.getHeaders().entrySet()) {
				final String headerKey = headers.getKey();

				for (final String headerValue : headers.getValue()) {
					servletResponse.addHeader(headerKey, headerValue);
				}
			}

			servletResponse.setStatus(responseEntity.getStatusCodeValue());

			final String responseBody = responseEntity.getBody();
			if (null != responseBody) {
				LOGGER.debug(String.format("writing response body: %s", responseBody.length() > 256 ? responseBody.substring(0, 252) + "..." : responseBody));
				servletResponse.getWriter().write(responseBody);
			}
			servletResponse.flushBuffer();
		}
	}

	static String translateCollectionQueryParameters(final String queryParams, final Map<String, String> collections) {
		if(DdipUtil.isBlank(queryParams)) {
			return queryParams;
		}

		String translatedQueryParams;
		try {
			translatedQueryParams = URLDecoder.decode(queryParams, StandardCharsets.UTF_8.toString());
		} catch (final UnsupportedEncodingException e) {
			throw new DdipRestControllerException(String.format("error decoding query parameters: %s", e.getMessage()), e, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		final int collectionNameLength = ATTR_COLLECTION_NAME.length();
		int startIndex = 0;

		while (startIndex != -1) {
			startIndex = translatedQueryParams.indexOf(ATTR_COLLECTION_NAME);

			if (startIndex != -1) {
				final int startOfCollectionNameValue = startIndex + collectionNameLength + " eq ".length();
				// make sure the equals operator is used
				try {
					if (!" eq ".equals(translatedQueryParams.substring(startIndex + collectionNameLength, startOfCollectionNameValue))) {
						throw new DdipRestControllerException(String.format("expecting equals operator (eq) after '%s' but was: %s", ATTR_COLLECTION_NAME,
								translatedQueryParams.substring(startIndex + collectionNameLength, startOfCollectionNameValue)), HttpStatus.BAD_REQUEST);
					}
				} catch (final IndexOutOfBoundsException e) {
					throw new DdipRestControllerException(String.format("expecting equals operator (eq) after: %s", ATTR_COLLECTION_NAME),
							HttpStatus.BAD_REQUEST);
				}

				// extract collection name
				final int endOfCollectionNameValue = startOfCollectionNameValue + getNextEnd(translatedQueryParams.substring(startOfCollectionNameValue));
				final String collectionName = translatedQueryParams.substring(startOfCollectionNameValue, endOfCollectionNameValue);
				final String trimmedCollectionName = DdipUtil.removeTrailing(DdipUtil.removeLeading(collectionName, " ", "'", "\""), " ", "'", "\"");

				// get collection query term
				if (!collections.containsKey(trimmedCollectionName)) {
					throw new DdipRestControllerException(String.format("unknown collection: %s", trimmedCollectionName), HttpStatus.BAD_REQUEST);
				}
				final String collectionQueryTerm = DdipUtil.trimToEmpty(collections.get(trimmedCollectionName));
				if (DdipUtil.isBlank(collectionQueryTerm)) {
					throw new DdipRestControllerException(String.format("collection exists but is invalid: %s", trimmedCollectionName),
							HttpStatus.INTERNAL_SERVER_ERROR);
				}

				// replace collection term
				translatedQueryParams = translatedQueryParams.replaceAll(ATTR_COLLECTION_NAME + " eq " + collectionName, "(" + collectionQueryTerm + ")");
			}
		}

		return translatedQueryParams;
	}

	static int getNextEnd(final String inText) {
		final int nextEnd = DdipUtil.getFirstOccuranceOf(RIGHT_VALUE_DELIMITERS, inText);

		if (nextEnd == -1) {
			return inText.length();
		}

		return nextEnd;
	}

}
