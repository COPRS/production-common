package de.werum.coprs.nativeapi.service;

import static de.werum.coprs.nativeapi.service.mapping.PripOdataEntityProperties.ContentDate;
import static de.werum.coprs.nativeapi.service.mapping.PripOdataEntityProperties.End;
import static de.werum.coprs.nativeapi.service.mapping.PripOdataEntityProperties.Start;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import de.werum.coprs.nativeapi.service.exception.NativeApiBadRequestException;
import de.werum.coprs.nativeapi.service.exception.NativeApiException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.StringUtil;

@Service
public class NativeApiStacServiceImpl implements NativeApiStacService {

	private static final Logger LOG = LogManager.getLogger(NativeApiStacServiceImpl.class);

	public static final Pattern RFC3339_DATE_PATTERN = Pattern
			.compile("^(\\d\\d\\d\\d)\\-(\\d\\d)\\-(\\d\\d)(T|t)(\\d\\d):(\\d\\d):(\\d\\d)([.]\\d+)?(Z|z|([-+])(\\d\\d):(\\d\\d))$");

	private final NativeApiProperties apiProperties;
	private final RestTemplate restTemplate;
	private final URL pripUrl;

	@Autowired
	public NativeApiStacServiceImpl(final NativeApiProperties apiProperties, final RestTemplate restTemplate) {
		this.apiProperties = apiProperties;
		this.restTemplate = restTemplate;
		this.pripUrl = buildPripUrl(apiProperties);
	}

	@Override
	public List<String> find(final String datetime) {
		// TODO: change response type using GeoJSON, see staccato-commons com.planet.staccato.model.GeoJson and com.planet.staccato.model.ItemCollection

		final String odataQueryUrl = buildPripQueryUrl(this.pripUrl, datetime);
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		final HttpEntity<String> requestEntity = new HttpEntity<>(null, httpHeaders);
		final ResponseEntity<String> responseEntity = this.restTemplate.exchange(odataQueryUrl, HttpMethod.GET, requestEntity, String.class);

		return mapResponse(responseEntity);
	}

	static List<String> mapResponse(final ResponseEntity<String> responseEntity) {
		// TODO: check for status 200 and map, else return error
		if (null != responseEntity) {

			final String responseBody = responseEntity.getBody();
			if (null != responseBody) {
				LOG.debug(String.format("PRIP response body: %s", responseBody.length() > 256 ? responseBody.substring(0, 252) + "..." : responseBody));
				return Collections.singletonList(responseBody); // TODO: map result to GeoJson and return it
			}
		}

		return Collections.singletonList("{}");
	}

	static String buildPripQueryUrl(final URL pripUrl, final String datetime) {
		return String.format("%s%s%s", pripUrl, "/odata/v1/Products?", createPripFilterTerm(datetime));
	}

	static String createPripFilterTerm(final String datetime) {
		final String odataDatetimeFilterTerm = createDatetimeOdataFilterTerm(datetime);

		if (StringUtil.isNotBlank(odataDatetimeFilterTerm)) {
			return String.format("$filter=%s", odataDatetimeFilterTerm);
		}

		return "";
	}

	static String createDatetimeOdataFilterTerm(final String datetimeStr) {
		if (StringUtil.isNotBlank(datetimeStr)) {
			final String[] datetimeStrings = datetimeStr.toUpperCase().split("/");

			if (datetimeStrings.length == 1) {
				final String singleDatetimeStr = datetimeStrings[0];

				if ("".equals(singleDatetimeStr) || "..".equals(singleDatetimeStr)) {
					throw new NativeApiBadRequestException(String.format("invalid datetime format, single value must be a datetime value: %s", datetimeStr));
				}

				final String singleOdataDatetime = convertDatetimeToOdataFormat(singleDatetimeStr);
				// ContentDate/Start lte 2021-12-21T12:21:12.000Z and ContentDate/End gte 2021-12-21T12:21:12.000Z
				return String.format("%s/%s lte %s and %s/%s gte %s", ContentDate, Start, singleOdataDatetime, ContentDate, End, singleOdataDatetime);

			} else if (datetimeStrings.length == 2) {
				final String datetimeLowerBoundaryStr = datetimeStrings[0];
				final String datetimeUpperBoundaryStr = datetimeStrings[1];
				final boolean openStart = "".equals(datetimeLowerBoundaryStr) || "..".equals(datetimeLowerBoundaryStr);
				final boolean openEnd = "".equals(datetimeUpperBoundaryStr) || "..".equals(datetimeUpperBoundaryStr);

				if (openStart && openEnd) {
					throw new NativeApiBadRequestException(
							String.format("invalid datetime value, at least one boundary (lower or upper) required: %s", datetimeStr));
				}

				if (openStart) {
					final String odataDatetimeUpperBoundary = convertDatetimeToOdataFormat(datetimeUpperBoundaryStr);
					// ContentDate/Start lte 2021-12-21T12:21:12.000Z
					return String.format("%s/%s lte %s", ContentDate, Start, odataDatetimeUpperBoundary);

				} else if (openEnd) {
					final String odataDatetimeLowerBoundary = convertDatetimeToOdataFormat(datetimeLowerBoundaryStr);
					// ContentDate/End gte 2021-12-21T00:00:00.000Z
					return String.format("%s/%s gte %s", ContentDate, End, odataDatetimeLowerBoundary);

				} else {
					final LocalDateTime datetimeLowerBoundary = convertDatetime(datetimeLowerBoundaryStr);
					final LocalDateTime datetimeUpperBoundary = convertDatetime(datetimeUpperBoundaryStr);

					if (datetimeUpperBoundary.isBefore(datetimeLowerBoundary)) {
						throw new NativeApiBadRequestException(String.format("lower boundary %s must not be smaller than upper boundary %s",
								datetimeLowerBoundaryStr, datetimeUpperBoundaryStr));
					}

					final String odataDatetimeLowerBoundary = formatToOdataFormat(datetimeLowerBoundary);
					final String odataDatetimeUpperBoundary = formatToOdataFormat(datetimeUpperBoundary);

					// ContentDate/Start lte 2021-12-21T12:21:12.000Z and ContentDate/End gte 2021-12-21T00:00:00.000Z
					return String.format("%s/%s lte %s and %s/%s gte %s", ContentDate, Start, odataDatetimeUpperBoundary, ContentDate, End,	odataDatetimeLowerBoundary);
				}
			} else {
				throw new NativeApiBadRequestException(String.format("invalid datetime format: %s", datetimeStr));
			}
		}

		return "";
	}

	static LocalDateTime convertDatetime(final String rfc3339DatetimeStr) {
		final Matcher matcher = RFC3339_DATE_PATTERN.matcher(rfc3339DatetimeStr);
		if (!matcher.matches()) {
			throw new NativeApiBadRequestException(String.format("invalid datetime format: %s", rfc3339DatetimeStr));
		}

		try {
			return DateUtils.parse(rfc3339DatetimeStr);
		} catch (final RuntimeException e) {
			throw new NativeApiBadRequestException(String.format("invalid datetime format: %s", rfc3339DatetimeStr));
		}
	}

	static String formatToOdataFormat(final LocalDateTime datetime) {
		try {
			return DateUtils.formatToOdataDateTimeFormat(datetime);
		} catch (final RuntimeException e) {
			throw new NativeApiException(String.format("error formatting datetime to OData format: %s", e.getMessage()), e, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	static String convertDatetimeToOdataFormat(final String rfc3339DatetimeStr) {
		return formatToOdataFormat(convertDatetime(rfc3339DatetimeStr));
	}

	static URL buildPripUrl(final NativeApiProperties apiProperties) {
		try {
			return UriComponentsBuilder
					.fromHttpUrl(String.format("%s://%s:%d",
							Objects.requireNonNull(apiProperties).getPripProtocol(),
							apiProperties.getPripHost(),
							apiProperties.getPripPort()))
					.build().toUri().toURL();
		} catch (final MalformedURLException e) {
			final String msg = String.format("could not initialize PRIP URL for internal interface; protocol (%s), host (%s) and port (%s) must be valid: %s",
					apiProperties.getPripProtocol(), apiProperties.getPripHost(), apiProperties.getPripPort(), e.getMessage());
			throw new IllegalArgumentException(msg, e);
		}
	}

}
