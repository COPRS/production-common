package de.werum.coprs.nativeapi.service.mapping;

import static de.werum.coprs.nativeapi.service.mapping.PripOdataEntityProperties.Footprint;
import static de.werum.coprs.nativeapi.service.mapping.PripOdataEntityProperties.Id;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.geojson.Crs;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;
import org.geojson.jackson.CrsType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.MediaType;

import de.werum.coprs.nativeapi.rest.model.Checksum;
import de.werum.coprs.nativeapi.rest.model.ContentDate;
import de.werum.coprs.nativeapi.rest.model.stac.GeoJsonBase.GeoJsonType;
import de.werum.coprs.nativeapi.rest.model.stac.StacAsset;
import de.werum.coprs.nativeapi.rest.model.stac.StacItem;
import de.werum.coprs.nativeapi.rest.model.stac.StacItemCollection;
import de.werum.coprs.nativeapi.rest.model.stac.StacLink;
import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.common.utils.StringUtil;

/**
 * Mapping to STAC item/collection (extended GeoJSON feature/collection)
 *
 * https://github.com/radiantearth/stac-api-spec/blob/master/stac-spec/item-spec/item-spec.md
 *
 */
public class PripToStacMapper {

	public static StacItemCollection mapFromPripOdataJson(final JSONObject pripOdataJson, final URI externalPripUrl) throws JSONException, URISyntaxException {
		if (null != pripOdataJson) {
			final JSONArray pripOdataJsonProducts = pripOdataJson.getJSONArray("value");

			final List<StacItem> items = new ArrayList<>();
			for (int i = 0; i < pripOdataJsonProducts.length(); i++) {
				items.add(mapFromPripOdataJsonProduct(pripOdataJsonProducts.getJSONObject(i), externalPripUrl));
			}

			final StacItemCollection itemCollection = new StacItemCollection();
			itemCollection.setFeatures(items);

			return itemCollection;
		}

		return null;
	}

	public static StacItem mapFromPripOdataJsonProduct(final JSONObject pripOdataJsonProduct, final URI externalPripUrl) throws URISyntaxException {
		final StacItem stacItem = new StacItem();

		// ID
		final String productId = pripOdataJsonProduct.getString(Id.name());
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

		final List<String> dontInclude = Arrays.asList(Id.name(), Footprint.name());
		for (final String propertyKey : pripOdataJsonProduct.keySet()) {
			if (!propertyKey.startsWith("@odata") && !dontInclude.contains(propertyKey)) {
				// handle complex attribute: Footprint
				if (Footprint.name().equals(propertyKey)) {
					final JSONObject footprintOdataJson = pripOdataJsonProduct.optJSONObject(propertyKey);

					if (null != footprintOdataJson) {
						stacItem.setProperty(propertyKey, asGeoJson(footprint));
					} else {
						stacItem.setProperty(propertyKey, null);
					}
					continue;
				}
				// handle complex attribute: ContentDate
				if (PripOdataEntityProperties.ContentDate.name().equals(propertyKey)) {
					if (null != contentDateStartStr || null != contentDateEndStr) {
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

		final String filename = pripOdataJsonProduct.optString(PripOdataEntityProperties.Name.name(), null);
		// links (product metadata link) and assets (product download link)
		if (null != externalPripUrl) {
			stacItem.setLinks(Collections.singletonList(createSelfLink(externalPripUrl, productId, filename)));
			stacItem.setAssets(Collections.singletonMap("product", createDownloadAsset(externalPripUrl, productId, filename)));
		}

		return stacItem;
	}

	private static StacLink createSelfLink(final URI externalPripUrl, final String productId, final String filename) throws URISyntaxException {
		final StacLink link = new StacLink();

		final URI pripProductMetadataUrl = Objects.requireNonNull(externalPripUrl, "cannot create metadata link without external PRIP URL")
				.resolve("Products(" + Objects.requireNonNull(productId, "product ID needed to create metadata link") + ")");
		link.setHref(pripProductMetadataUrl.toString());
		link.setType(MediaType.APPLICATION_JSON_VALUE);
		link.setRel("self");

		if (StringUtil.isNotBlank(filename)) {
			link.setTitle("metadata for product " + filename);
		} else {
			link.setTitle("product metadata");
		}

		return link;
	}

	private static StacAsset createDownloadAsset(final URI externalPripUrl, final String productId, final String filename) throws URISyntaxException {
		final StacAsset asset = new StacAsset();

		final URI pripDownloadUrl = Objects.requireNonNull(externalPripUrl, "cannot create download asset without external PRIP URL")
				.resolve("Products(" + Objects.requireNonNull(productId, "product ID needed to create download link") + ")/$value");
		asset.setHref(pripDownloadUrl.toString());
		asset.setTitle(filename);

		if (null != filename && filename.toUpperCase().endsWith(".ZIP")) {
			asset.setType("application/zip");
		}

		asset.setDescription("download link for product data");
		asset.setRoles(Collections.singletonList("data"));

		return asset;
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
