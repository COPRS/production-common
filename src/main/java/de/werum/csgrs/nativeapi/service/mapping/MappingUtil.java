package de.werum.csgrs.nativeapi.service.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;

import de.werum.csgrs.nativeapi.rest.model.Checksum;
import de.werum.csgrs.nativeapi.rest.model.ContentDate;
import de.werum.csgrs.nativeapi.rest.model.PripMetadataResponse;
import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.prip.model.GeoShapeLineString;
import esa.s1pdgs.cpoc.prip.model.GeoShapePolygon;
import esa.s1pdgs.cpoc.prip.model.PripGeoCoordinate;
import esa.s1pdgs.cpoc.prip.model.PripGeoShape;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;

public class MappingUtil {

	private static final Logger LOGGER = LogManager.getLogger(MappingUtil.class);

	public static PripMetadataResponse pripMetadataToResponse(final PripMetadata pripMetadata) {
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

		return pripMetadataResponse;
	}

	public static List<PripMetadataResponse> pripMetadataToResponse(final List<PripMetadata> pripMetadataList) {
		return CollectionUtil.nullToEmpty(pripMetadataList).stream().map(product -> pripMetadataToResponse(product)).collect(Collectors.toList());
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

}
