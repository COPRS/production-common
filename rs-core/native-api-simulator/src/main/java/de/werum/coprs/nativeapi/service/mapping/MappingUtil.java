/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.werum.coprs.nativeapi.service.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;

import de.werum.coprs.nativeapi.rest.model.Checksum;
import de.werum.coprs.nativeapi.rest.model.ContentDate;
import de.werum.coprs.nativeapi.rest.model.PripMetadataResponse;
import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.prip.model.GeoShapeLineString;
import esa.s1pdgs.cpoc.prip.model.GeoShapePolygon;
import esa.s1pdgs.cpoc.prip.model.PripGeoCoordinate;
import esa.s1pdgs.cpoc.prip.model.PripGeoShape;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;

public class MappingUtil {

	private static final Logger LOGGER = LogManager.getLogger(MappingUtil.class);

	public static PripMetadataResponse pripMetadataToResponse(final PripMetadata pripMetadata, final String missionName) {
		if (null == pripMetadata) {
			return null;
		}

		final PripMetadataResponse pripMetadataResponse = new PripMetadataResponse();

		pripMetadataResponse.setId(null != pripMetadata.getId() ? pripMetadata.getId().toString() : null);
		pripMetadataResponse.setName(pripMetadata.getName());
		pripMetadataResponse.setContentType(pripMetadata.getContentType());
		pripMetadataResponse.setContentLength(pripMetadata.getContentLength());
		pripMetadataResponse.setPublicationDate(null != pripMetadata.getCreationDate() ?
				DateUtils.formatToOdataDateTimeFormat(pripMetadata.getCreationDate()) : null);
		pripMetadataResponse.setEvictionDate(null != pripMetadata.getEvictionDate() ?
				DateUtils.formatToOdataDateTimeFormat(pripMetadata.getEvictionDate()) : null);
		pripMetadataResponse.setProductionType("systematic_production"); // not in prip index, in s1 returns always 'systematic_production'

		pripMetadataResponse.setChecksum(mapChecksums(pripMetadata));
		pripMetadataResponse.setContentDate(mapContentDate(pripMetadata));
		pripMetadataResponse.setFootprint(mapFootprint(pripMetadata));

		pripMetadataResponse.setAttributes(pripMetadata.getAttributes());

		pripMetadataResponse.setLinks(createLinks(missionName, pripMetadata));

		return pripMetadataResponse;
	}

	public static List<PripMetadataResponse> pripMetadataToResponse(final List<PripMetadata> pripMetadataList, final String missionName) {
		return CollectionUtil.nullToEmpty(pripMetadataList).stream()
				.map(product -> pripMetadataToResponse(product, missionName))
				.collect(Collectors.toList());
	}

	private static List<Checksum> mapChecksums(final PripMetadata pripMetadata) {
		final List<Checksum> result = new ArrayList<>();

		if (null != pripMetadata) {
			final List<esa.s1pdgs.cpoc.prip.model.Checksum> checksums = pripMetadata.getChecksums();

			if (CollectionUtil.isNotEmpty(checksums)) {
				for (final esa.s1pdgs.cpoc.prip.model.Checksum checksumToMap : checksums) {
					final Checksum checksum = new Checksum();

					checksum.setAlgorithm(checksumToMap.getAlgorithm());
					checksum.setDate(null != checksumToMap.getDate() ? DateUtils.formatToOdataDateTimeFormat(checksumToMap.getDate()) : null);
					checksum.setValue(checksumToMap.getValue());

					result.add(checksum);
				}
			}
		}

		return result;
	}

	private static ContentDate mapContentDate(final PripMetadata pripMetadata) {
		final ContentDate result = new ContentDate();

		if (null != pripMetadata) {
			result.setStart(null != pripMetadata.getContentDateStart() ? DateUtils.formatToOdataDateTimeFormat(pripMetadata.getContentDateStart()) : null);
			result.setEnd(null != pripMetadata.getContentDateEnd() ? DateUtils.formatToOdataDateTimeFormat(pripMetadata.getContentDateEnd()) : null);
		}

		return result;
	}

	private static GeoJsonObject mapFootprint(final PripMetadata pripMetadata) {
		GeoJsonObject result = null;

		if (null != pripMetadata && null != pripMetadata.getFootprint()) {
			final PripGeoShape footprint = pripMetadata.getFootprint();
			final List<PripGeoCoordinate> coordinates = footprint.getCoordinates();
			final LngLatAlt[] convertedCoordinates = convertCoordinates(coordinates);

			if (footprint instanceof GeoShapePolygon) {
				result = new Polygon(convertedCoordinates);
			} else if (footprint instanceof GeoShapeLineString) {
				result = new LineString(convertedCoordinates);
			} else {
				LOGGER.warn("cannot map footprint of '{}' because type '{}' is not yet implemented",
						pripMetadata.getName(), footprint.getClass().getSimpleName());
			}
		}

		return result;
	}

	private static LngLatAlt[] convertCoordinates(final List<PripGeoCoordinate> coordinates) {
		return CollectionUtil.nullToEmpty(coordinates).stream()
				.map(coordinate -> new LngLatAlt(coordinate.getLongitude(), coordinate.getLatitude()))
				.toArray(LngLatAlt[]::new);
	}

	/* creating HATEOAS-style links for actions on the data */
	private static Map<String, String> createLinks(final String missionName, PripMetadata pripMetadata) {
		final Map<String, String> links = new HashMap<>();

		if (null != pripMetadata && null != pripMetadata.getId()) {
			links.put("download", String.format("/missions/%s/products/%s/download", missionName, pripMetadata.getId().toString()));
		}

		return links;
	}

}
