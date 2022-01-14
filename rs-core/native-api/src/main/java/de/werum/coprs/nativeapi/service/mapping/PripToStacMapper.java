package de.werum.coprs.nativeapi.service.mapping;

import static de.werum.coprs.nativeapi.service.mapping.PripOdataEntityProperties.Footprint;
import static de.werum.coprs.nativeapi.service.mapping.PripOdataEntityProperties.Id;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.geojson.Crs;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;
import org.geojson.jackson.CrsType;
import org.json.JSONArray;
import org.json.JSONObject;

import de.werum.coprs.nativeapi.rest.model.Checksum;
import de.werum.coprs.nativeapi.rest.model.ContentDate;
import de.werum.coprs.nativeapi.rest.model.stac.GeoJsonBase.GeoJsonType;
import de.werum.coprs.nativeapi.rest.model.stac.StacItem;
import de.werum.coprs.nativeapi.rest.model.stac.StacItemCollection;
import esa.s1pdgs.cpoc.common.utils.CollectionUtil;

/**
 * Mapping to STAC
 */
public class PripToStacMapper {

	public static StacItemCollection mapFromPripOdataJson(final JSONObject pripOdataJson) {
		if (null != pripOdataJson) {
			final JSONArray pripOdataJsonProducts = pripOdataJson.getJSONArray("value");

			final List<StacItem> items = new ArrayList<>();
			for (int i = 0; i < pripOdataJsonProducts.length(); i++) {
				items.add(mapFromPripOdataJsonProduct(pripOdataJsonProducts.getJSONObject(i)));
			}

			final StacItemCollection itemCollection = new StacItemCollection();
			itemCollection.setFeatures(items);

			return itemCollection;
		}

		return null;
	}

	public static StacItem mapFromPripOdataJsonProduct(final JSONObject pripOdataJsonProduct) {
		final StacItem stacItem = new StacItem();

		// ID
		final String productId = pripOdataJsonProduct.getString(Id.name());
		stacItem.setProperty(Id.name(), productId);
		stacItem.setId(productId);

		// geometry and bbox
		final JSONObject footprint = pripOdataJsonProduct.optJSONObject(Footprint.name());
		stacItem.setGeometry(asGeoJson(footprint));

		// if this item has a geometry a bbox is required
		if (null != stacItem.getGeometry()) {
			stacItem.setBbox(getBoundingBox(stacItem.getGeometry()));
		}

		// properties
		// at least 'datetime' or, if a single point in time is not appropriate, 'start_datetime' and 'end_datetime' required
		final JSONObject contentDateJson = pripOdataJsonProduct.getJSONObject(PripOdataEntityProperties.ContentDate.name());
		stacItem.setProperty("datetime", null);
		final String contentDateStartStr = contentDateJson.getString(PripOdataEntityProperties.Start.name());
		stacItem.setProperty("start_datetime", contentDateStartStr);
		final String contentDateEndStr = contentDateJson.getString(PripOdataEntityProperties.End.name());
		stacItem.setProperty("end_datetime", contentDateEndStr);

		final List<String> dontInclude = Arrays.asList(Id.name(), Footprint.name()
				/*, PripOdataEntityProperties.Checksum.name(),
				    PripOdataEntityProperties.ContentDate.name() */);
		for (final String propertyKey : pripOdataJsonProduct.keySet()) {
			if (!propertyKey.startsWith("@odata") && !dontInclude.contains(propertyKey)) {
				// handle complex attribute: Footprint
				if (Footprint.name().equals(propertyKey)) {
					final JSONObject footprintOdataJson = pripOdataJsonProduct.optJSONObject(propertyKey);

					if (!JSONObject.NULL.equals(footprintOdataJson)) {
						stacItem.setProperty(propertyKey, asGeoJson(footprint));
					} else {
						stacItem.setProperty(propertyKey, null);
					}
					continue;
				}
				// handle complex attribute: ContentDate
				if (PripOdataEntityProperties.ContentDate.name().equals(propertyKey)) {
					if (!JSONObject.NULL.equals(contentDateJson)) {
						final ContentDate contentDate = new ContentDate();
						contentDate.setStart(contentDateStartStr);
						contentDate.setEnd(contentDateEndStr);
						stacItem.setProperty(propertyKey, contentDate);
					} else {
						stacItem.setProperty(propertyKey, null);
					}
					continue;
				}
				// handle complex attribute: Checksum[]
				if (PripOdataEntityProperties.Checksum.name().equals(propertyKey)) {
					final JSONArray checksumOdataJsonArray = pripOdataJsonProduct.optJSONArray(propertyKey);

					if (null != checksumOdataJsonArray && checksumOdataJsonArray.length() > 0) {
						final List<Checksum> checksums = new ArrayList<>();

						for (int i = 0; i < checksumOdataJsonArray.length(); i++) {
							final JSONObject checksumJson = checksumOdataJsonArray.getJSONObject(i);

							final Checksum checksum = new Checksum();
							checksum.setAlgorithm(checksumJson.getString(PripOdataEntityProperties.Algorithm.name()));
							checksum.setValue(checksumJson.getString(PripOdataEntityProperties.Value.name()));
							checksum.setDate(checksumJson.getString(PripOdataEntityProperties.ChecksumDate.name()));

							checksums.add(checksum);
						}

						stacItem.setProperty(propertyKey, checksums.stream().toArray(Checksum[]::new));
					}
					continue;
				}

				// all other cases are (expected) to be simple attributes
				stacItem.setProperty(propertyKey, pripOdataJsonProduct.get(propertyKey));
			}
		}
		// TODO: links
		// TODO: assets

		return stacItem;
	}

	private static GeoJsonObject asGeoJson(final JSONObject pripOdataJsonFootprint) {
		if (null != pripOdataJsonFootprint) {
			final String type = pripOdataJsonFootprint.getString("type");

			if (GeoJsonType.Polygon.name().equalsIgnoreCase(type)) {
				final JSONArray coordinates = pripOdataJsonFootprint.getJSONArray("coordinates");
				final List<LngLatAlt> polygonCoordinates = new ArrayList<>();

				for (int i = 0; i < coordinates.length(); i++) {
					final JSONArray polygonArray = coordinates.getJSONArray(i);

					for (int j = 0; j < polygonArray.length(); j++) {
						final JSONArray polygonPoints = polygonArray.getJSONArray(j);

						final double longitude = polygonPoints.getDouble(0);
						final double latitude = polygonPoints.getDouble(1);

						polygonCoordinates.add(new LngLatAlt(longitude, latitude));
					}
				}

				if (!polygonCoordinates.isEmpty()) {
					final Polygon polygon = new Polygon(polygonCoordinates);

					final JSONObject crsObject = pripOdataJsonFootprint.optJSONObject("crs");
					if (null != crsObject) {
						final Crs crs = new Crs();
						crs.setType(CrsType.valueOf(crsObject.getString("type")));

						final JSONObject CrsProperties = crsObject.getJSONObject("properties");
						final Map<String,Object> crsPropertiesMap = new HashMap<>();

						CollectionUtil.nullToEmpty(CrsProperties.keySet())
						.forEach(crsPropKey -> crsPropertiesMap.put(crsPropKey, CrsProperties.get(crsPropKey)));

						if (!crsPropertiesMap.isEmpty()) {
							crs.setProperties(crsPropertiesMap);
						}

						polygon.setCrs(crs);
					}

					return polygon;
				}
			} else {
				throw new IllegalArgumentException(String.format("GeoJSON mapping not implemented for type: %s", type));
			}
		}

		return null;
	}

	private static double[] getBoundingBox(final GeoJsonObject geometry) {
		List<LngLatAlt> coordinates = null;

		if (geometry instanceof Polygon) {
			coordinates = CollectionUtil.nullToEmptyList(((Polygon) geometry).getCoordinates()).stream().flatMap(List::stream).collect(Collectors.toList());

		} else if (geometry instanceof LineString) {
			coordinates = CollectionUtil.nullToEmptyList(((LineString) geometry).getCoordinates());

		} else if (geometry instanceof Point) {
			final LngLatAlt point = ((Point) geometry).getCoordinates();
			if (null != point) {
				coordinates = Collections.singletonList(point);
			}
		} else {
			throw new IllegalArgumentException(
					String.format("minimal bounding rectangle (bbox) algorithm not implemented for type: %s", geometry.getClass().getSimpleName()));
		}

		return getBoundingBox(coordinates);
	}

	private static double[] getBoundingBox(final List<LngLatAlt> coordinates) {
		if (CollectionUtil.isNotEmpty(coordinates)) {
			final List<Double> allXcoords = coordinates.stream().map(lngLat -> lngLat.getLongitude()).collect(Collectors.toList());
			final List<Double> allYcoords = coordinates.stream().map(lngLat -> lngLat.getLatitude()).collect(Collectors.toList());

			final double minX = Collections.min(allXcoords);
			final double minY = Collections.min(allYcoords);
			final double maxX = Collections.max(allXcoords);
			final double maxY = Collections.max(allYcoords);

			// TODO: handle cases which cross the Antimeridian (https://datatracker.ietf.org/doc/html/rfc7946#section-5.2)
			// TODO: handle cases which contains a pole (https://datatracker.ietf.org/doc/html/rfc7946#section-5.3)

			return new double[] { minX, minY, maxX, maxY };
		}

		return null;
	}

	// --------------------------------------------------------------------------

	private PripToStacMapper() {
	}

}
